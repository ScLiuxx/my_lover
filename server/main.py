"""
my_lover backend
两个人在同一台服务器上共享一份恋爱数据。

启动:
    uvicorn main:app --host 0.0.0.0 --port 8000
"""

from __future__ import annotations

import hashlib
import json
import os
import secrets
import sqlite3
import string
import threading
import time
import uuid
from collections import defaultdict, deque
from typing import Any, Dict, Optional, Tuple

from fastapi import FastAPI, Header, HTTPException, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "data")
DB_PATH = os.path.join(DATA_DIR, "love.db")

os.makedirs(DATA_DIR, exist_ok=True)

app = FastAPI(title="my_lover API", version="1.0.0")

CRASH_TOKEN = os.environ.get("CRASH_TOKEN", "mylove-crash-7f3a9c")


def check_crash_token(token: Optional[str]) -> None:
    if token != CRASH_TOKEN:
        raise HTTPException(status_code=401, detail="invalid crash token")


def token_hash(token: str) -> str:
    return hashlib.sha256(token.encode("utf-8")).hexdigest()


# 简单的固定窗口限流：(method, path) -> (次数, 秒)
RATE_RULES = {
    ("POST", "/api/couples"): (10, 3600),
    ("POST", "/api/couples/join"): (20, 600),
    ("POST", "/api/crash"): (30, 3600),
}
DEFAULT_RATE = (300, 60)
_rate_lock = threading.Lock()
_rate_buckets: Dict[str, deque] = defaultdict(deque)


@app.middleware("http")
async def rate_limit(request: Request, call_next):
    ip = request.client.host if request.client else "unknown"
    key = f"{ip}:{request.method}:{request.url.path}"
    now = time.time()
    rule = RATE_RULES.get((request.method, request.url.path))
    if rule is None:
        rule = (DEFAULT_RATE[0], DEFAULT_RATE[1])
        key = f"{ip}:all"
    limit, window = rule
    with _rate_lock:
        bucket = _rate_buckets[key]
        while bucket and now - bucket[0] > window:
            bucket.popleft()
        if len(bucket) >= limit:
            return JSONResponse({"detail": "too many requests"}, status_code=429)
        bucket.append(now)
    return await call_next(request)


def db() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH, timeout=10)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL")
    return conn


def init_db() -> None:
    with db() as conn:
        conn.executescript(
            """
            CREATE TABLE IF NOT EXISTS couples (
                id          TEXT PRIMARY KEY,
                invite_code TEXT UNIQUE NOT NULL,
                state       TEXT NOT NULL,
                created_at  INTEGER NOT NULL,
                updated_at  INTEGER NOT NULL
            );
            CREATE TABLE IF NOT EXISTS members (
                token      TEXT PRIMARY KEY,
                couple_id  TEXT NOT NULL,
                role       TEXT NOT NULL,
                name       TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                last_seen  INTEGER NOT NULL
            );
            CREATE INDEX IF NOT EXISTS idx_members_couple ON members(couple_id);
            """
        )
        # 已有库升级：把明文 token 迁移为哈希
        rows = conn.execute("SELECT token FROM members").fetchall()
        for row in rows:
            value = row["token"]
            if len(value) != 64 or any(c not in "0123456789abcdef" for c in value):
                conn.execute(
                    "UPDATE members SET token = ? WHERE token = ?",
                    (token_hash(value), value),
                )
        # 角色唯一约束（并发加入也不会出现两个 b）
        try:
            conn.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_members_role "
                "ON members(couple_id, role)"
            )
        except sqlite3.IntegrityError:
            pass


init_db()


def default_state(name_a: str) -> Dict[str, Any]:
    return {
        "start_date": "2026-04-21",
        "names": {"a": name_a, "b": ""},
        "birthdays": {"a": "--05-09", "b": "--04-21"},
        "pet": {"food": 46, "joy": 58, "energy": 32, "growth": 74, "coins": 114},
        "miss": {"a": 0, "b": 0},
        "moods": {"a": None, "b": None},
        "daily": {"question": "今天最想对我抱怨的？", "a": "", "b": ""},
        "love_note": "",
    }


def new_invite_code(conn: sqlite3.Connection) -> str:
    alphabet = string.ascii_uppercase + string.digits
    for _ in range(50):
        code = "".join(secrets.choice(alphabet) for _ in range(6))
        row = conn.execute("SELECT 1 FROM couples WHERE invite_code = ?", (code,)).fetchone()
        if row is None:
            return code
    raise HTTPException(status_code=500, detail="cannot allocate invite code")


def merge_state(current: Dict[str, Any], patch: Dict[str, Any]) -> Dict[str, Any]:
    for key, value in patch.items():
        if isinstance(value, dict) and isinstance(current.get(key), dict):
            current[key].update(value)
        else:
            current[key] = value
    return current


def authenticate(authorization: Optional[str]) -> Tuple[sqlite3.Row, sqlite3.Row]:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(status_code=401, detail="missing bearer token")
    token = authorization.split(" ", 1)[1].strip()
    conn = db()
    try:
        member = conn.execute(
            "SELECT * FROM members WHERE token = ?", (token_hash(token),)
        ).fetchone()
        if member is None:
            raise HTTPException(status_code=401, detail="invalid token")
        couple = conn.execute(
            "SELECT * FROM couples WHERE id = ?", (member["couple_id"],)
        ).fetchone()
        if couple is None:
            raise HTTPException(status_code=404, detail="couple not found")
        conn.execute(
            "UPDATE members SET last_seen = ? WHERE token = ?", (int(time.time()), token)
        )
        conn.commit()
        return member, couple
    finally:
        conn.close()


class CreateCouple(BaseModel):
    name: str = Field(min_length=1, max_length=24)


class JoinCouple(BaseModel):
    invite_code: str = Field(min_length=4, max_length=12)
    name: str = Field(min_length=1, max_length=24)


class StatePatch(BaseModel):
    state: Dict[str, Any]


ALLOWED_STATE_KEYS = {
    "start_date",
    "names",
    "birthdays",
    "pet",
    "miss",
    "moods",
    "daily",
    "quiz",
    "quiz_custom",
    "notes",
    "period_enc",
    "love_note",
    "love_note_by",
}


class CrashReport(BaseModel):
    device: str = ""
    android: str = ""
    version: str = ""
    stack: str


def couple_payload(couple: sqlite3.Row, role: str) -> Dict[str, Any]:
    return {
        "couple_id": couple["id"],
        "invite_code": couple["invite_code"],
        "role": role,
        "state": json.loads(couple["state"]),
        "updated_at": couple["updated_at"],
    }


@app.get("/")
def root() -> Dict[str, str]:
    return {"service": "my_lover", "status": "ok"}


@app.get("/api/health")
def health() -> Dict[str, Any]:
    with db() as conn:
        couples = conn.execute("SELECT COUNT(*) AS c FROM couples").fetchone()["c"]
    return {"status": "ok", "couples": couples, "time": int(time.time())}


@app.post("/api/crash")
def report_crash(
    body: CrashReport,
    x_crash_token: Optional[str] = Header(default=None),
) -> Dict[str, Any]:
    check_crash_token(x_crash_token)
    record = {
        "time": int(time.time()),
        "device": body.device,
        "android": body.android,
        "version": body.version,
        "stack": body.stack[-6000:],
    }
    with open(os.path.join(DATA_DIR, "crashes.log"), "a", encoding="utf-8") as f:
        f.write(json.dumps(record, ensure_ascii=False) + "\n")
    return {"ok": True}


@app.get("/api/crash/latest")
def latest_crashes(
    x_crash_token: Optional[str] = Header(default=None),
) -> Dict[str, Any]:
    check_crash_token(x_crash_token)
    path = os.path.join(DATA_DIR, "crashes.log")
    if not os.path.exists(path):
        return {"items": []}
    with open(path, "r", encoding="utf-8") as f:
        lines = f.read().splitlines()[-10:]
    items = []
    for line in lines:
        try:
            items.append(json.loads(line))
        except Exception:
            continue
    return {"items": items}


@app.post("/api/couples")
def create_couple(body: CreateCouple) -> Dict[str, Any]:
    now = int(time.time())
    couple_id = uuid.uuid4().hex
    token = secrets.token_urlsafe(32)
    state = default_state(body.name.strip())

    conn = db()
    try:
        code = new_invite_code(conn)
        conn.execute(
            "INSERT INTO couples (id, invite_code, state, created_at, updated_at) "
            "VALUES (?, ?, ?, ?, ?)",
            (couple_id, code, json.dumps(state, ensure_ascii=False), now, now),
        )
        conn.execute(
            "INSERT INTO members (token, couple_id, role, name, created_at, last_seen) "
            "VALUES (?, ?, 'a', ?, ?, ?)",
            (token_hash(token), couple_id, body.name.strip(), now, now),
        )
        conn.commit()
        couple = conn.execute("SELECT * FROM couples WHERE id = ?", (couple_id,)).fetchone()
        payload = couple_payload(couple, "a")
        payload["token"] = token
        return payload
    finally:
        conn.close()


@app.post("/api/couples/join")
def join_couple(body: JoinCouple) -> Dict[str, Any]:
    now = int(time.time())
    code = body.invite_code.strip().upper()
    conn = db()
    try:
        couple = conn.execute(
            "SELECT * FROM couples WHERE invite_code = ?", (code,)
        ).fetchone()
        if couple is None:
            raise HTTPException(status_code=404, detail="invite code not found")

        members = conn.execute(
            "SELECT role FROM members WHERE couple_id = ?", (couple["id"],)
        ).fetchall()
        used = {m["role"] for m in members}
        if "b" in used:
            raise HTTPException(status_code=409, detail="this couple is already full")
        role = "b"

        token = secrets.token_urlsafe(32)
        try:
            conn.execute(
                "INSERT INTO members (token, couple_id, role, name, created_at, last_seen) "
                "VALUES (?, ?, ?, ?, ?, ?)",
                (token_hash(token), couple["id"], role, body.name.strip(), now, now),
            )
        except sqlite3.IntegrityError:
            raise HTTPException(status_code=409, detail="this couple is already full")
        state = json.loads(couple["state"])
        state.setdefault("names", {})
        state["names"]["b"] = body.name.strip()
        conn.execute(
            "UPDATE couples SET state = ?, updated_at = ? WHERE id = ?",
            (json.dumps(state, ensure_ascii=False), now, couple["id"]),
        )
        conn.commit()
        couple = conn.execute(
            "SELECT * FROM couples WHERE id = ?", (couple["id"],)
        ).fetchone()
        payload = couple_payload(couple, role)
        payload["token"] = token
        return payload
    finally:
        conn.close()


@app.get("/api/couples/current")
def get_state(authorization: Optional[str] = Header(default=None)) -> Dict[str, Any]:
    member, couple = authenticate(authorization)
    return couple_payload(couple, member["role"])


@app.put("/api/couples/current/state")
def put_state(
    body: StatePatch,
    authorization: Optional[str] = Header(default=None),
) -> Dict[str, Any]:
    member, couple = authenticate(authorization)
    patch = {key: value for key, value in body.state.items() if key in ALLOWED_STATE_KEYS}
    if len(json.dumps(patch, ensure_ascii=False)) > 64 * 1024:
        raise HTTPException(status_code=413, detail="state too large")
    state = json.loads(couple["state"])
    merge_state(state, patch)
    now = int(time.time())
    conn = db()
    try:
        conn.execute(
            "UPDATE couples SET state = ?, updated_at = ? WHERE id = ?",
            (json.dumps(state, ensure_ascii=False), now, couple["id"]),
        )
        conn.commit()
        couple = conn.execute(
            "SELECT * FROM couples WHERE id = ?", (couple["id"],)
        ).fetchone()
        return couple_payload(couple, member["role"])
    finally:
        conn.close()


@app.post("/api/couples/current/miss")
def miss(authorization: Optional[str] = Header(default=None)) -> Dict[str, Any]:
    member, couple = authenticate(authorization)
    state = json.loads(couple["state"])
    miss_state = state.setdefault("miss", {"a": 0, "b": 0})
    role = member["role"]
    miss_state[role] = int(miss_state.get(role, 0)) + 1
    now = int(time.time())
    conn = db()
    try:
        conn.execute(
            "UPDATE couples SET state = ?, updated_at = ? WHERE id = ?",
            (json.dumps(state, ensure_ascii=False), now, couple["id"]),
        )
        conn.commit()
        couple = conn.execute(
            "SELECT * FROM couples WHERE id = ?", (couple["id"],)
        ).fetchone()
        return couple_payload(couple, role)
    finally:
        conn.close()


@app.delete("/api/members/me")
def revoke_me(authorization: Optional[str] = Header(default=None)) -> Dict[str, Any]:
    """吊销当前设备的 token（退出/换机时用）。"""
    member, _ = authenticate(authorization)
    conn = db()
    try:
        conn.execute("DELETE FROM members WHERE token = ?", (member["token"],))
        conn.commit()
    finally:
        conn.close()
    return {"ok": True}
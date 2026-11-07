#!/usr/bin/env bash
# 启动 / 重启 my_lover 后端
set -e
cd "$(dirname "$0")"
unset LD_LIBRARY_PATH

PORT="${PORT:-80}"
PY=".venv/bin/python"
LOG="server.log"
PID_FILE="server.pid"

if [ ! -x "$PY" ]; then
  echo "缺少虚拟环境，请先执行: python3 -m venv .venv && .venv/bin/pip install -r requirements.txt"
  exit 1
fi

if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  kill "$(cat "$PID_FILE")"
  sleep 1
fi

setsid nohup "$PY" -m uvicorn main:app --host 0.0.0.0 --port "$PORT" >> "$LOG" 2>&1 < /dev/null &
echo $! > "$PID_FILE"
sleep 1
echo "started pid=$(cat "$PID_FILE") port=$PORT"
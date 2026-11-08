#!/usr/bin/env bash
# 把仓库里的后端代码同步到运行目录并重启
set -e
cd "$(dirname "$0")"
install -m 644 main.py requirements.txt /opt/my-lover/
/opt/my-lover/.venv/bin/pip install -q -r requirements.txt
chown mylove:mylove /opt/my-lover/main.py /opt/my-lover/requirements.txt
systemctl restart my-lover.service
systemctl --no-pager --lines=0 status my-lover.service | head -3
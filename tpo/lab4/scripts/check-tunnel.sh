#!/usr/bin/env bash
set -euo pipefail

PORT="${PORT:-8079}"
CONFIG="${1:-1}"
URL="http://localhost:${PORT}/?token=518506673&user=-1355007012&config=${CONFIG}"

echo "Checking tunnel: $URL"
STATUS="$(curl -sS -o /tmp/tpo-lab4-tunnel-check.out -w '%{http_code}' "$URL")"
echo "HTTP status: $STATUS"

if [[ "$STATUS" == "403" ]]; then
  echo "HTTP 403: check token, user, config, and URL parameters." >&2
  exit 1
fi

if [[ "$STATUS" == "000" ]]; then
  echo "No response. Start SSH forwarding with bin/helios-port-forward-foreground or bin/helios-port-forward." >&2
  exit 1
fi

echo "Tunnel check passed."


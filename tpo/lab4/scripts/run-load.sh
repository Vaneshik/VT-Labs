#!/usr/bin/env bash
set -euo pipefail

CONFIG="${1:?Usage: scripts/run-load.sh <1|2|3> [path/to/jmeter]}"
JMETER_BIN="${2:-${JMETER_BIN:-jmeter}}"

case "$CONFIG" in
  1|2|3) ;;
  *) echo "Config must be 1, 2, or 3" >&2; exit 1 ;;
esac

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RESULT_DIR="$ROOT/load/result"
HTML_DIR="$ROOT/load/html-report-config${CONFIG}"
RESULT_FILE="$RESULT_DIR/config${CONFIG}.csv"

mkdir -p "$RESULT_DIR"
rm -rf "$HTML_DIR"
rm -f "$RESULT_FILE"

exec "$JMETER_BIN" \
  -n \
  -t "$ROOT/load/test-plan.jmx" \
  -q "$ROOT/user.properties" \
  -l "$RESULT_FILE" \
  -e \
  -o "$HTML_DIR" \
  -Jtarget_config="$CONFIG"

#!/usr/bin/env bash
set -euo pipefail

JMETER_VERSION="${JMETER_VERSION:-5.6.3}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TOOLS_DIR="$ROOT/.tools"
TARGET_DIR="$TOOLS_DIR/apache-jmeter-${JMETER_VERSION}"
LINK_DIR="$TOOLS_DIR/apache-jmeter"
ARCHIVE="$TOOLS_DIR/apache-jmeter-${JMETER_VERSION}.tgz"
URL="https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz"

mkdir -p "$TOOLS_DIR"

if [[ ! -x "$TARGET_DIR/bin/jmeter" ]]; then
  echo "Downloading Apache JMeter ${JMETER_VERSION}..."
  curl -L "$URL" -o "$ARCHIVE"
  tar -xzf "$ARCHIVE" -C "$TOOLS_DIR"
fi

rm -f "$LINK_DIR"
ln -s "apache-jmeter-${JMETER_VERSION}" "$LINK_DIR"

echo "JMeter installed:"
"$LINK_DIR/bin/jmeter" --version | head -n 1
echo
echo "Use it with:"
echo "JMETER_BIN=$LINK_DIR/bin/jmeter scripts/run-load.sh 1"


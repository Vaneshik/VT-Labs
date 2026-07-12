#!/usr/bin/env bash
set -euo pipefail

echo "[1/2] Building frontend..."
npx shadow-cljs release app

echo "[2/2] Backend is ready to serve resources/public"
echo "Run: clj -M:run"

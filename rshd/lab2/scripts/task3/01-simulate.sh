#!/bin/sh
set -e

PGDATA="$HOME/aoj42"

pg_ctl -D "$PGDATA" status

rm -f "$PGDATA/postgresql.conf" "$PGDATA/postgresql.auto.conf" "$PGDATA/pg_hba.conf"
echo "конфиги удалены"

pg_ctl -D "$PGDATA" -l "$PGDATA/server.log" start || true
sleep 2
tail -3 "$PGDATA/server.log" 2>/dev/null || true

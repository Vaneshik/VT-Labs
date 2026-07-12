#!/bin/sh
set -e

PGDATA="$HOME/aoj42"
PGRUN="$HOME/run"
PGPORT=9748
PGUSER=postgres0

mkdir -p "$HOME/wal_archive" "$HOME/base_backup"

psql -h "$PGRUN" -p "$PGPORT" -U "$PGUSER" -d postgres <<'SQL'
ALTER SYSTEM SET wal_level = 'replica';
ALTER SYSTEM SET archive_mode = 'on';
ALTER SYSTEM SET archive_command = 'test ! -f /var/db/postgres1/wal_archive/%f && cp %p /var/db/postgres1/wal_archive/%f';
SQL

pg_ctl -D "$PGDATA" restart -l "$PGDATA/server.log"
sleep 3

grep -q 'replication' "$PGDATA/pg_hba.conf" || \
  echo "local   replication   postgres0   trust" >> "$PGDATA/pg_hba.conf"
pg_ctl -D "$PGDATA" reload

pg_basebackup -D "$HOME/base_backup" -h "$PGRUN" -p "$PGPORT" -U "$PGUSER" -Ft -Xs -z -P

ls -lh "$HOME/base_backup/"

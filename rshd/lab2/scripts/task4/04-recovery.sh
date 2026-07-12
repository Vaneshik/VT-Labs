#!/bin/sh
set -e

PGDATA="$HOME/aoj42"
WAL_ARCHIVE="$HOME/wal_archive"
RECOVERY_TARGET="2026-05-19 08:26:12+03"

pg_ctl -D "$PGDATA" stop -m fast

rm -rf "$PGDATA"
mkdir "$PGDATA"
chmod 700 "$PGDATA"

tar -xzf "$HOME/base_backup/base.tar.gz" -C "$PGDATA/"

rm -rf "$HOME/ydp10" "$HOME/zcc31"
mkdir -p "$HOME/ydp10" "$HOME/zcc31"
tar -xzf "$HOME/base_backup/16385.tar.gz" -C "$HOME/ydp10/"
tar -xzf "$HOME/base_backup/16386.tar.gz" -C "$HOME/zcc31/"

# симлинки удалились вместе с PGDATA — воссоздаём
ln -sfn "$HOME/ydp10" "$PGDATA/pg_tblspc/16385"
ln -sfn "$HOME/zcc31" "$PGDATA/pg_tblspc/16386"

cat >> "$PGDATA/postgresql.conf" <<EOF
restore_command = 'cp $WAL_ARCHIVE/%f %p'
recovery_target_time = '$RECOVERY_TARGET'
recovery_target_action = 'promote'
EOF

touch "$PGDATA/recovery.signal"

pg_ctl -D "$PGDATA" -l "$PGDATA/server.log" start

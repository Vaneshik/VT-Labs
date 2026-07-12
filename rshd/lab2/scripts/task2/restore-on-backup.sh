#!/bin/sh
set -e

PGDATA="$HOME/aoj42"
PGPORT=9748
PGUSER=postgres0
TS_YDP10="$HOME/ts_ydp10"
TS_ZCC31="$HOME/ts_zcc31"

LATEST="$(ls -dt "$HOME/backups"/backup-* | head -1)"
echo "бэкап: $LATEST"

pg_ctl -D "$PGDATA" stop -m fast 2>/dev/null || true

rm -rf "$PGDATA" "$TS_YDP10" "$TS_ZCC31"
mkdir -p "$PGDATA" "$TS_YDP10" "$TS_ZCC31"
chmod 700 "$PGDATA"

rsync -a "$LATEST/pgdata/"    "$PGDATA/"
rsync -a "$LATEST/ts_ydp10/" "$TS_YDP10/"
rsync -a "$LATEST/ts_zcc31/" "$TS_ZCC31/"

# правим симлинки табличных пространств
ls -la "$PGDATA/pg_tblspc/"
for link in "$PGDATA/pg_tblspc/"*; do
  [ -L "$link" ] || continue
  case "$(readlink "$link")" in
    */ydp10) ln -sfn "$TS_YDP10" "$link" ;;
    */zcc31) ln -sfn "$TS_ZCC31" "$link" ;;
  esac
done
echo "симлинки после:"
ls -la "$PGDATA/pg_tblspc/"

# убираем путь к сокету pg116 — на pg117 он другой
grep -v unix_socket_directories "$PGDATA/postgresql.auto.conf" \
  > "$PGDATA/postgresql.auto.conf.tmp" 2>/dev/null || true
mv "$PGDATA/postgresql.auto.conf.tmp" "$PGDATA/postgresql.auto.conf"

cat >> "$PGDATA/postgresql.conf" <<EOF

listen_addresses = 'localhost'
port = $PGPORT
shared_buffers = '128MB'
autovacuum = off
unix_socket_directories = '$HOME/run'
EOF

mkdir -p "$HOME/run"
pg_ctl -D "$PGDATA" -l "$PGDATA/server.log" start
sleep 3

psql -h "$HOME/run" -p "$PGPORT" -U "$PGUSER" -l

psql -h "$HOME/run" -p "$PGPORT" -U "$PGUSER" -d bestbluemath \
  -c "SELECT 'data_default' AS tbl, count(*) FROM data_default
      UNION ALL SELECT 'data_ydp10', count(*) FROM data_ydp10
      UNION ALL SELECT 'data_zcc31', count(*) FROM data_zcc31"

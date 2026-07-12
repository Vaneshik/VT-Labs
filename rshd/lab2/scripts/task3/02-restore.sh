#!/bin/sh
set -e

PGDATA="$HOME/aoj42"
REMOTE="postgres4@pg117"
LATEST="$(ssh "$REMOTE" 'ls -dt $HOME/backups/backup-* | head -1')"

echo "восстанавливаем из $LATEST"

rsync -a "$REMOTE:$LATEST/pgdata/postgresql.conf"      "$PGDATA/"
rsync -a "$REMOTE:$LATEST/pgdata/postgresql.auto.conf" "$PGDATA/"
rsync -a "$REMOTE:$LATEST/pgdata/pg_hba.conf"          "$PGDATA/"

# табличные пространства переносим в новое место
mkdir -p "$HOME/ts_restore/ydp10" "$HOME/ts_restore/zcc31"
rsync -a "$REMOTE:$LATEST/ts_ydp10/" "$HOME/ts_restore/ydp10/"
rsync -a "$REMOTE:$LATEST/ts_zcc31/" "$HOME/ts_restore/zcc31/"

echo "симлинки до:"
ls -la "$PGDATA/pg_tblspc/"

for link in "$PGDATA/pg_tblspc/"*; do
  [ -L "$link" ] || continue
  case "$(readlink "$link")" in
    */ydp10) ln -sfn "$HOME/ts_restore/ydp10" "$link" ;;
    */zcc31) ln -sfn "$HOME/ts_restore/zcc31" "$link" ;;
  esac
done

echo "симлинки после:"
ls -la "$PGDATA/pg_tblspc/"

pg_ctl -D "$PGDATA" -l "$PGDATA/server.log" start
sleep 3

psql -h "$HOME/run" -p 9748 -U postgres0 -d bestbluemath \
  -c "SELECT 'data_default' AS tbl, count(*) FROM data_default
      UNION ALL SELECT 'data_ydp10', count(*) FROM data_ydp10
      UNION ALL SELECT 'data_zcc31', count(*) FROM data_zcc31"

#!/bin/sh
set -e

PGDATA="$HOME/aoj42"

# убираем параметры восстановления — recovery.signal уже удалён postgres после promote
grep -v -E 'restore_command|recovery_target_time|recovery_target_action' \
  "$PGDATA/postgresql.conf" > "$PGDATA/postgresql.conf.tmp"
mv "$PGDATA/postgresql.conf.tmp" "$PGDATA/postgresql.conf"

pg_ctl -D "$PGDATA" reload

psql -h "$HOME/run" -p 9748 -U postgres0 -d bestbluemath \
  -c "SELECT 'data_default' AS tbl, count(*) FROM data_default
      UNION ALL SELECT 'data_ydp10', count(*) FROM data_ydp10
      UNION ALL SELECT 'data_zcc31', count(*) FROM data_zcc31"

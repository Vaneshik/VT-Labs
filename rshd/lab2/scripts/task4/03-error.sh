#!/bin/sh
set -e
psql -h "$HOME/run" -p 9748 -U postgres0 -d bestbluemath <<'SQL'
SELECT pg_switch_wal();
SELECT now() AS drop_time;
DROP TABLE data_ydp10;
DROP TABLE data_zcc31;
SQL

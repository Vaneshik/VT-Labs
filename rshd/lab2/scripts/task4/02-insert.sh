#!/bin/sh
set -e
psql -h "$HOME/run" -p 9748 -U postgres0 -d bestbluemath <<'SQL'
INSERT INTO data_ydp10  (payload) VALUES ('pitr_row_1'), ('pitr_row_2'), ('pitr_row_3');
INSERT INTO data_zcc31  (payload) VALUES ('pitr_row_1'), ('pitr_row_2'), ('pitr_row_3');
INSERT INTO data_default(payload) VALUES ('pitr_row_1'), ('pitr_row_2'), ('pitr_row_3');

SELECT now() AS recovery_target_time;

SELECT 'data_ydp10'   AS tbl, count(*) FROM data_ydp10
UNION ALL
SELECT 'data_zcc31'   AS tbl, count(*) FROM data_zcc31
UNION ALL
SELECT 'data_default' AS tbl, count(*) FROM data_default;
SQL

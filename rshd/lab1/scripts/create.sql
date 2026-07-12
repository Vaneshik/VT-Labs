-- Создание директорий выполняется в shell до запуска этого скрипта:
-- mkdir -p $HOME/ydp10 $HOME/zcc31

-- Табличные пространства для временных объектов
CREATE TABLESPACE ts_ydp10 LOCATION '/var/db/postgres0/ydp10';
CREATE TABLESPACE ts_zcc31 LOCATION '/var/db/postgres0/zcc31';

-- Настроить temp_tablespaces для использования новых пространств
ALTER SYSTEM SET temp_tablespaces = 'ts_ydp10, ts_zcc31';
SELECT pg_reload_conf();

-- Новая база данных на основе template1
CREATE DATABASE bestbluemath TEMPLATE template1;

-- Новая роль
CREATE ROLE lab1_user WITH LOGIN PASSWORD 'lab1pass';

-- Права подключения к базам
GRANT CONNECT ON DATABASE postgres      TO lab1_user;
GRANT CONNECT ON DATABASE bestbluemath  TO lab1_user;

-- Права на использование табличных пространств
GRANT CREATE ON TABLESPACE ts_ydp10    TO lab1_user;
GRANT CREATE ON TABLESPACE ts_zcc31    TO lab1_user;
GRANT CREATE ON TABLESPACE pg_default  TO lab1_user;

-- Права на схему public в базе postgres
GRANT USAGE  ON SCHEMA public TO lab1_user;
GRANT CREATE ON SCHEMA public TO lab1_user;

-- Права на схему public в базе bestbluemath (запустить подключившись к bestbluemath):
-- psql -U postgres0 -d bestbluemath -p 9748
-- GRANT USAGE  ON SCHEMA public TO lab1_user;
-- GRANT CREATE ON SCHEMA public TO lab1_user;

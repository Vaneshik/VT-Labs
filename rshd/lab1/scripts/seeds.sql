-- Выполнять от имени lab1_user в базе postgres, затем в bestbluemath

-- Таблица в табличном пространстве ts_ydp10
CREATE TABLE data_ydp10 (
    id      serial PRIMARY KEY,
    payload text,
    created_at timestamp DEFAULT now()
) TABLESPACE ts_ydp10;

-- Таблица в табличном пространстве ts_zcc31
CREATE TABLE data_zcc31 (
    id      serial PRIMARY KEY,
    payload text,
    created_at timestamp DEFAULT now()
) TABLESPACE ts_zcc31;

-- Таблица в пространстве по умолчанию
CREATE TABLE data_default (
    id      serial PRIMARY KEY,
    payload text,
    created_at timestamp DEFAULT now()
);

-- Наполнение данными
INSERT INTO data_ydp10 (payload)
    SELECT 'record_' || g FROM generate_series(1, 200) g;

INSERT INTO data_zcc31 (payload)
    SELECT 'record_' || g FROM generate_series(1, 200) g;

INSERT INTO data_default (payload)
    SELECT 'record_' || g FROM generate_series(1, 200) g;

-- Временная таблица (использует temp_tablespaces — ts_ydp10/ts_zcc31)
CREATE TEMP TABLE temp_data AS
    SELECT id, payload FROM data_default LIMIT 50;

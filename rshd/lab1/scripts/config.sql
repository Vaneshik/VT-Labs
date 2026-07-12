-- Принимать подключения только на localhost
ALTER SYSTEM SET listen_addresses = 'localhost';
-- Номер порта по варианту
ALTER SYSTEM SET port = 9748;

-- OLAP: 9 одновременных пользователей, пакетная запись/чтение по 256МБ
-- Сервер: FreeBSD 14.4, RAM 128GB, CPU 16 cores
ALTER SYSTEM SET max_connections = 20;
ALTER SYSTEM SET superuser_reserved_connections = 3;
ALTER SYSTEM SET shared_buffers = '512MB';
ALTER SYSTEM SET temp_buffers = '256MB';
ALTER SYSTEM SET work_mem = '256MB';
ALTER SYSTEM SET effective_cache_size = '4GB';
ALTER SYSTEM SET maintenance_work_mem = '256MB';

ALTER SYSTEM SET checkpoint_timeout = '1h';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET fsync = on;
ALTER SYSTEM SET commit_delay = 0;
ALTER SYSTEM SET commit_siblings = 5;

ALTER SYSTEM SET default_statistics_target = 500;
ALTER SYSTEM SET random_page_cost = 4.0;
ALTER SYSTEM SET effective_io_concurrency = 2;
ALTER SYSTEM SET max_worker_processes = 4;
ALTER SYSTEM SET max_parallel_workers_per_gather = 2;
ALTER SYSTEM SET max_parallel_workers = 4;

ALTER SYSTEM SET log_destination = 'csvlog';
ALTER SYSTEM SET logging_collector = on;
ALTER SYSTEM SET log_filename = 'postgresql-%Y-%m-%d_%H%M%S.csv';
ALTER SYSTEM SET log_min_messages = 'warning';
ALTER SYSTEM SET log_connections = on;
ALTER SYSTEM SET log_disconnections = on;
ALTER SYSTEM SET log_checkpoints = on;
ALTER SYSTEM SET log_lock_waits = on;

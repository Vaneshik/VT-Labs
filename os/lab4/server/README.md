# VTFS HTTP Server

FastAPI сервер для VTFS с PostgreSQL.

## Установка и запуск

```bash
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib python3-pip

sudo -u postgres psql -c "CREATE USER vtfs WITH PASSWORD 'vtfs123';"
sudo -u postgres psql -c "CREATE DATABASE vtfs_db OWNER vtfs;"

pip3 install -r requirements.txt
python3 init_db.py

python3 -m uvicorn app:app --host 127.0.0.1 --port 8000
```

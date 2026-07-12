#!/bin/sh

export PGDATA=$HOME/aoj42
export PGLOCALE=en_US.UTF-8
export PGENCODE=UTF8
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

initdb -D "$PGDATA" \
  --encoding=$PGENCODE \
  --locale=$PGLOCALE \
  --lc-messages=$PGLOCALE \
  --lc-monetary=$PGLOCALE \
  --lc-numeric=$PGLOCALE \
  --lc-time=$PGLOCALE \
  --username=postgres0

# WAL directory created by initdb at $PGDATA/pg_wal by default

# Configure authentication
cat > "$PGDATA/pg_hba.conf" << 'EOF'
# TYPE  DATABASE  USER  ADDRESS        METHOD
# Unix-domain socket - peer authentication
local   all       all                  peer
# TCP/IP - localhost only - MD5
host    all       all   127.0.0.1/32   md5
host    all       all   ::1/128        md5
EOF

pg_ctl -D "$PGDATA" -l "$PGDATA/server.log" start

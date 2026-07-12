#!/bin/bash
# Fix PostgreSQL permissions for vtfs user

echo "Fixing PostgreSQL permissions for vtfs user..."

sudo -u postgres psql -d vtfs_db << 'EOF'
-- Grant all privileges on schema
GRANT ALL ON SCHEMA public TO vtfs;
ALTER SCHEMA public OWNER TO vtfs;

-- Grant all privileges on existing tables
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO vtfs;

-- Grant all privileges on existing sequences
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO vtfs;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO vtfs;

-- Grant default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO vtfs;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO vtfs;

-- Show current privileges
\dp
EOF
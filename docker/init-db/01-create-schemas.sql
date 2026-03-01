-- =============================================================
-- Enterprise Messenger — Schema Initialization
-- Executed on first PostgreSQL boot via docker-entrypoint-initdb.d
-- =============================================================

CREATE SCHEMA IF NOT EXISTS auth_schema;
CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS messaging_schema;

-- Grant usage to the application user
GRANT ALL ON SCHEMA auth_schema TO messenger;
GRANT ALL ON SCHEMA user_schema TO messenger;
GRANT ALL ON SCHEMA messaging_schema TO messenger;

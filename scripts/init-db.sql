-- Create databases for each microservice
CREATE DATABASE flagship_users;
CREATE DATABASE flagship_orders;
CREATE DATABASE flagship_payments;
CREATE DATABASE flagship_inventory;
CREATE DATABASE keycloak_db;

-- Create user (if not exists)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'flagship_user') THEN
        CREATE USER flagship_user WITH PASSWORD 'flagship_password';
    END IF;
END
$$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE flagship_users TO flagship_user;
GRANT ALL PRIVILEGES ON DATABASE flagship_orders TO flagship_user;
GRANT ALL PRIVILEGES ON DATABASE flagship_payments TO flagship_user;
GRANT ALL PRIVILEGES ON DATABASE flagship_inventory TO flagship_user;
GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO flagship_user;

-- Grant schema privileges on each DB
\c flagship_users;
GRANT ALL ON SCHEMA public TO flagship_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO flagship_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO flagship_user;

\c flagship_orders;
GRANT ALL ON SCHEMA public TO flagship_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO flagship_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO flagship_user;

\c flagship_payments;
GRANT ALL ON SCHEMA public TO flagship_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO flagship_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO flagship_user;

\c flagship_inventory;
GRANT ALL ON SCHEMA public TO flagship_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO flagship_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO flagship_user;

\c keycloak_db;
GRANT ALL ON SCHEMA public TO flagship_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO flagship_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO flagship_user;

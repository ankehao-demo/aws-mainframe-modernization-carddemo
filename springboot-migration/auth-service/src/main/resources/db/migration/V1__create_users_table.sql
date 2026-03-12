-- Flyway migration: Create users table
-- Mapped from COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA)
-- Replaces USRSEC VSAM KSDS file

CREATE TABLE IF NOT EXISTS users (
    user_id       VARCHAR(8)   NOT NULL PRIMARY KEY,
    password      VARCHAR(255) NOT NULL,
    first_name    VARCHAR(20),
    last_name     VARCHAR(20),
    user_type     VARCHAR(1)   NOT NULL DEFAULT 'U'
);

COMMENT ON TABLE users IS 'User security data — migrated from USRSEC VSAM file';
COMMENT ON COLUMN users.user_type IS 'A=Admin, U=Regular user — maps to ROLE_ADMIN/ROLE_USER';

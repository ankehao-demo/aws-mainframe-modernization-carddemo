-- Users table: migrated from CSUSR01Y.cpy (SEC-USER-DATA)
CREATE TABLE users (
    usr_id       VARCHAR(8)   NOT NULL,
    first_name   VARCHAR(20),
    last_name    VARCHAR(20),
    password     VARCHAR(8)   NOT NULL,
    user_type    VARCHAR(1)   NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (usr_id)
);

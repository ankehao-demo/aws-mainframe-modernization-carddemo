CREATE TABLE IF NOT EXISTS users (
    user_id    VARCHAR(8)  PRIMARY KEY,
    password   VARCHAR(72) NOT NULL,
    first_name VARCHAR(20),
    last_name  VARCHAR(20),
    user_type  CHAR(1)     NOT NULL DEFAULT 'U' CHECK (user_type IN ('A', 'U'))
);

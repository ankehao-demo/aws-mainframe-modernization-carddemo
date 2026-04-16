CREATE TABLE report_metadata (
    id           BIGSERIAL   PRIMARY KEY,
    report_type  VARCHAR(50),
    status       VARCHAR(20),
    created_at   VARCHAR(30),
    completed_at VARCHAR(30),
    parameters   VARCHAR(500),
    file_path    VARCHAR(255)
);

-- Disclosure groups: migrated from CVTRA02Y.cpy (DIS-GROUP-RECORD)
CREATE TABLE disclosure_groups (
    group_id       VARCHAR(10)   NOT NULL,
    tran_type_cd   VARCHAR(2)    NOT NULL,
    tran_cat_cd    INT           NOT NULL,
    int_rate       DECIMAL(7,3),
    CONSTRAINT pk_disclosure_groups PRIMARY KEY (group_id, tran_type_cd, tran_cat_cd)
);

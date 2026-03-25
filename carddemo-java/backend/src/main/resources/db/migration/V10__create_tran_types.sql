-- Transaction types: migrated from CVTRA03Y.cpy (TRAN-TYPE-RECORD)
CREATE TABLE tran_types (
    type_cd      VARCHAR(2)   NOT NULL,
    type_desc    VARCHAR(50),
    CONSTRAINT pk_tran_types PRIMARY KEY (type_cd)
);

-- Transaction categories: migrated from CVTRA04Y.cpy (TRAN-CAT-RECORD)
CREATE TABLE tran_categories (
    type_cd       VARCHAR(2)   NOT NULL,
    cat_cd        INT          NOT NULL,
    cat_desc      VARCHAR(50),
    CONSTRAINT pk_tran_categories PRIMARY KEY (type_cd, cat_cd)
);

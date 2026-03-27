package com.cardemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Transaction type entity derived from CVTRA03Y.cpy (TRAN-TYPE-RECORD, RECLN 60).
 *
 * COBOL field mappings:
 *   TRAN-TYPE       PIC X(02)  -> String (primary key)
 *   TRAN-TYPE-DESC  PIC X(50)  -> String
 */
@Entity
@Table(name = "transaction_types")
public class TransactionType {

    @Id
    @Column(name = "tran_type", length = 2)
    private String type;

    @Column(name = "tran_type_desc", length = 50)
    private String description;

    public TransactionType() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

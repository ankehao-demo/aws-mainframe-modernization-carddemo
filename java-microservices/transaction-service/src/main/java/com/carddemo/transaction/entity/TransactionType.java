package com.carddemo.transaction.entity;

import jakarta.persistence.*;

/**
 * Transaction type entity - from copybook CVTRA03Y.cpy (60-byte records).
 */
@Entity
@Table(name = "transaction_types")
public class TransactionType {
    @Id
    @Column(name = "type_code", length = 2)
    private String typeCode;

    @Column(name = "type_description", length = 50)
    private String typeDescription;

    public TransactionType() {}
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getTypeDescription() { return typeDescription; }
    public void setTypeDescription(String typeDescription) { this.typeDescription = typeDescription; }
}

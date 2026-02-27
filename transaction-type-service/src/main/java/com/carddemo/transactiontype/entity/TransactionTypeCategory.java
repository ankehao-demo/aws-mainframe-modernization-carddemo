package com.carddemo.transactiontype.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "TRANSACTION_TYPE_CATEGORY", schema = "CARDDEMO")
public class TransactionTypeCategory {

    @EmbeddedId
    private TransactionTypeCategoryId id;

    @Column(name = "TRC_CAT_DATA", length = 50)
    private String trcCatData;

    @ManyToOne
    @JoinColumn(name = "TRC_TYPE_CODE")
    @MapsId("trcTypeCode")
    @JsonIgnore
    private TransactionType transactionType;

    public TransactionTypeCategory() {
    }

    public TransactionTypeCategory(TransactionTypeCategoryId id, String trcCatData,
                                    TransactionType transactionType) {
        this.id = id;
        this.trcCatData = trcCatData;
        this.transactionType = transactionType;
    }

    public TransactionTypeCategoryId getId() {
        return id;
    }

    public void setId(TransactionTypeCategoryId id) {
        this.id = id;
    }

    public String getTrcCatData() {
        return trcCatData;
    }

    public void setTrcCatData(String trcCatData) {
        this.trcCatData = trcCatData;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}

package com.carddemo.transactiontype.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TransactionTypeCategoryId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "TRC_TYPE_CODE", length = 2)
    private String trcTypeCode;

    @Column(name = "TRC_TYPE_CATEGORY", length = 4)
    private String trcTypeCategory;

    public TransactionTypeCategoryId() {
    }

    public TransactionTypeCategoryId(String trcTypeCode, String trcTypeCategory) {
        this.trcTypeCode = trcTypeCode;
        this.trcTypeCategory = trcTypeCategory;
    }

    public String getTrcTypeCode() {
        return trcTypeCode;
    }

    public void setTrcTypeCode(String trcTypeCode) {
        this.trcTypeCode = trcTypeCode;
    }

    public String getTrcTypeCategory() {
        return trcTypeCategory;
    }

    public void setTrcTypeCategory(String trcTypeCategory) {
        this.trcTypeCategory = trcTypeCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionTypeCategoryId that = (TransactionTypeCategoryId) o;
        return Objects.equals(trcTypeCode, that.trcTypeCode)
                && Objects.equals(trcTypeCategory, that.trcTypeCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trcTypeCode, trcTypeCategory);
    }
}

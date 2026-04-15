package com.carddemo.transaction.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Transaction category entity - from copybook CVTRA04Y.cpy (60-byte records).
 */
@Entity
@Table(name = "transaction_categories")
@IdClass(TransactionCategory.TransactionCategoryId.class)
public class TransactionCategory {
    @Id
    @Column(name = "type_code", length = 2)
    private String typeCode;

    @Id
    @Column(name = "category_code")
    private Integer categoryCode;

    @Column(name = "category_description", length = 50)
    private String categoryDescription;

    public TransactionCategory() {}
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public Integer getCategoryCode() { return categoryCode; }
    public void setCategoryCode(Integer categoryCode) { this.categoryCode = categoryCode; }
    public String getCategoryDescription() { return categoryDescription; }
    public void setCategoryDescription(String categoryDescription) { this.categoryDescription = categoryDescription; }

    public static class TransactionCategoryId implements Serializable {
        private String typeCode;
        private Integer categoryCode;
        public TransactionCategoryId() {}
        public TransactionCategoryId(String typeCode, Integer categoryCode) {
            this.typeCode = typeCode; this.categoryCode = categoryCode;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TransactionCategoryId that)) return false;
            return Objects.equals(typeCode, that.typeCode) && Objects.equals(categoryCode, that.categoryCode);
        }
        @Override public int hashCode() { return Objects.hash(typeCode, categoryCode); }
    }
}

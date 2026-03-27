package com.cardemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Transaction category entity derived from CVTRA04Y.cpy (TRAN-CAT-RECORD, RECLN 60).
 *
 * COBOL field mappings (composite key = TRAN-CAT-KEY):
 *   TRAN-TYPE-CD        PIC X(02)  -> String
 *   TRAN-CAT-CD         PIC 9(04)  -> Integer
 *   TRAN-CAT-TYPE-DESC  PIC X(50)  -> String
 */
@Entity
@Table(name = "transaction_categories")
@IdClass(TransactionCategory.TransactionCategoryId.class)
public class TransactionCategory {

    @Id
    @Column(name = "tran_type_cd", length = 2)
    private String typeCd;

    @Id
    @Column(name = "tran_cat_cd")
    private Integer categoryCd;

    @Column(name = "tran_cat_type_desc", length = 50)
    private String description;

    public TransactionCategory() {
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public Integer getCategoryCd() {
        return categoryCd;
    }

    public void setCategoryCd(Integer categoryCd) {
        this.categoryCd = categoryCd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class TransactionCategoryId implements Serializable {
        private String typeCd;
        private Integer categoryCd;

        public TransactionCategoryId() {
        }

        public TransactionCategoryId(String typeCd, Integer categoryCd) {
            this.typeCd = typeCd;
            this.categoryCd = categoryCd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionCategoryId that = (TransactionCategoryId) o;
            return Objects.equals(typeCd, that.typeCd)
                    && Objects.equals(categoryCd, that.categoryCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(typeCd, categoryCd);
        }
    }
}

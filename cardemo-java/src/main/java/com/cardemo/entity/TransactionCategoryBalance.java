package com.cardemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Transaction category balance entity derived from CVTRA01Y.cpy
 * (TRAN-CAT-BAL-RECORD, RECLN 50).
 *
 * COBOL field mappings (composite key = TRAN-CAT-KEY):
 *   TRANCAT-ACCT-ID   PIC 9(11)       -> Long
 *   TRANCAT-TYPE-CD   PIC X(02)       -> String
 *   TRANCAT-CD        PIC 9(04)       -> Integer
 *   TRAN-CAT-BAL      PIC S9(09)V99   -> BigDecimal
 */
@Entity
@Table(name = "transaction_category_balances")
@IdClass(TransactionCategoryBalance.TransactionCategoryBalanceId.class)
public class TransactionCategoryBalance {

    @Id
    @Column(name = "trancat_acct_id")
    private Long acctId;

    @Id
    @Column(name = "trancat_type_cd", length = 2)
    private String typeCd;

    @Id
    @Column(name = "trancat_cd")
    private Integer categoryCd;

    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal balance;

    public TransactionCategoryBalance() {
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public static class TransactionCategoryBalanceId implements Serializable {
        private Long acctId;
        private String typeCd;
        private Integer categoryCd;

        public TransactionCategoryBalanceId() {
        }

        public TransactionCategoryBalanceId(Long acctId, String typeCd, Integer categoryCd) {
            this.acctId = acctId;
            this.typeCd = typeCd;
            this.categoryCd = categoryCd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionCategoryBalanceId that = (TransactionCategoryBalanceId) o;
            return Objects.equals(acctId, that.acctId)
                    && Objects.equals(typeCd, that.typeCd)
                    && Objects.equals(categoryCd, that.categoryCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(acctId, typeCd, categoryCd);
        }
    }
}

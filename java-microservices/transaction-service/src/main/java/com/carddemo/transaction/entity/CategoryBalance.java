package com.carddemo.transaction.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Category balance entity - from copybook CVTRA01Y.cpy (50-byte records).
 */
@Entity
@Table(name = "category_balances")
@IdClass(CategoryBalance.CategoryBalanceId.class)
public class CategoryBalance {
    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Id
    @Column(name = "type_code", length = 2)
    private String typeCode;

    @Id
    @Column(name = "category_code")
    private Integer categoryCode;

    @Column(name = "balance", precision = 11, scale = 2)
    private BigDecimal balance;

    public CategoryBalance() {}
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public Integer getCategoryCode() { return categoryCode; }
    public void setCategoryCode(Integer categoryCode) { this.categoryCode = categoryCode; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public static class CategoryBalanceId implements Serializable {
        private Long accountId;
        private String typeCode;
        private Integer categoryCode;
        public CategoryBalanceId() {}
        public CategoryBalanceId(Long accountId, String typeCode, Integer categoryCode) {
            this.accountId = accountId; this.typeCode = typeCode; this.categoryCode = categoryCode;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CategoryBalanceId that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(typeCode, that.typeCode) && Objects.equals(categoryCode, that.categoryCode);
        }
        @Override public int hashCode() { return Objects.hash(accountId, typeCode, categoryCode); }
    }
}

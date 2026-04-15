package com.carddemo.transaction.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Disclosure group entity - from copybook CVTRA02Y.cpy (50-byte records).
 */
@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroup.DisclosureGroupId.class)
public class DisclosureGroup {
    @Id
    @Column(name = "account_group_id", length = 10)
    private String accountGroupId;

    @Id
    @Column(name = "type_code", length = 2)
    private String typeCode;

    @Id
    @Column(name = "category_code")
    private Integer categoryCode;

    @Column(name = "interest_rate", precision = 6, scale = 2)
    private BigDecimal interestRate;

    public DisclosureGroup() {}
    public String getAccountGroupId() { return accountGroupId; }
    public void setAccountGroupId(String accountGroupId) { this.accountGroupId = accountGroupId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public Integer getCategoryCode() { return categoryCode; }
    public void setCategoryCode(Integer categoryCode) { this.categoryCode = categoryCode; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public static class DisclosureGroupId implements Serializable {
        private String accountGroupId;
        private String typeCode;
        private Integer categoryCode;
        public DisclosureGroupId() {}
        public DisclosureGroupId(String accountGroupId, String typeCode, Integer categoryCode) {
            this.accountGroupId = accountGroupId; this.typeCode = typeCode; this.categoryCode = categoryCode;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DisclosureGroupId that)) return false;
            return Objects.equals(accountGroupId, that.accountGroupId) && Objects.equals(typeCode, that.typeCode) && Objects.equals(categoryCode, that.categoryCode);
        }
        @Override public int hashCode() { return Objects.hash(accountGroupId, typeCode, categoryCode); }
    }
}

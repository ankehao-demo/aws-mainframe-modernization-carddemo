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
 * Disclosure group entity derived from CVTRA02Y.cpy (DIS-GROUP-RECORD, RECLN 50).
 *
 * COBOL field mappings (composite key = DIS-GROUP-KEY):
 *   DIS-ACCT-GROUP-ID   PIC X(10)       -> String
 *   DIS-TRAN-TYPE-CD    PIC X(02)       -> String
 *   DIS-TRAN-CAT-CD     PIC 9(04)       -> Integer
 *   DIS-INT-RATE         PIC S9(04)V99   -> BigDecimal
 */
@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroup.DisclosureGroupId.class)
public class DisclosureGroup {

    @Id
    @Column(name = "dis_acct_group_id", length = 10)
    private String acctGroupId;

    @Id
    @Column(name = "dis_tran_type_cd", length = 2)
    private String tranTypeCd;

    @Id
    @Column(name = "dis_tran_cat_cd")
    private Integer tranCatCd;

    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal interestRate;

    public DisclosureGroup() {
    }

    public String getAcctGroupId() {
        return acctGroupId;
    }

    public void setAcctGroupId(String acctGroupId) {
        this.acctGroupId = acctGroupId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public static class DisclosureGroupId implements Serializable {
        private String acctGroupId;
        private String tranTypeCd;
        private Integer tranCatCd;

        public DisclosureGroupId() {
        }

        public DisclosureGroupId(String acctGroupId, String tranTypeCd, Integer tranCatCd) {
            this.acctGroupId = acctGroupId;
            this.tranTypeCd = tranTypeCd;
            this.tranCatCd = tranCatCd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DisclosureGroupId that = (DisclosureGroupId) o;
            return Objects.equals(acctGroupId, that.acctGroupId)
                    && Objects.equals(tranTypeCd, that.tranTypeCd)
                    && Objects.equals(tranCatCd, that.tranCatCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(acctGroupId, tranTypeCd, tranCatCd);
        }
    }
}

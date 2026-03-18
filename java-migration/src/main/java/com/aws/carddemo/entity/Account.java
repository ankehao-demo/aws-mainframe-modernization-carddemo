package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "acct_id", length = 11)
    private String acctId;

    @Column(name = "acct_active_status", length = 1, nullable = false)
    private String acctActiveStatus;

    @Column(name = "acct_curr_bal", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCurrBal;

    @Column(name = "acct_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCreditLimit;

    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCashCreditLimit;

    @Column(name = "acct_open_date", length = 10)
    private String acctOpenDate;

    @Column(name = "acct_expiration_date", length = 10)
    private String acctExpirationDate;

    @Column(name = "acct_reissue_date", length = 10)
    private String acctReissueDate;

    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCurrCycCredit;

    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCurrCycDebit;

    @Column(name = "acct_addr_zip", length = 10)
    private String acctAddrZip;

    @Column(name = "acct_group_id", length = 10)
    private String acctGroupId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}

package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVACT01Y.cpy (ACCOUNT-RECORD, RECLN 300).
 * Represents the ACCTDATA VSAM KSDS file.
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    /** ACCT-ID PIC 9(11) */
    @Id
    @Column(name = "account_id", nullable = false, length = 11)
    private Long accountId;

    /** ACCT-ACTIVE-STATUS PIC X(01) */
    @Column(name = "account_status", length = 1, nullable = false)
    private String accountStatus;

    /** ACCT-CURR-BAL PIC S9(10)V99 */
    @Column(name = "current_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal currentBalance;

    /** ACCT-CREDIT-LIMIT PIC S9(10)V99 */
    @Column(name = "credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal creditLimit;

    /** ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99 */
    @Column(name = "cash_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal cashCreditLimit;

    /** ACCT-OPEN-DATE PIC X(10) */
    @Column(name = "open_date")
    private LocalDate openDate;

    /** ACCT-EXPIRAION-DATE PIC X(10) */
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    /** ACCT-REISSUE-DATE PIC X(10) */
    @Column(name = "reissue_date")
    private LocalDate reissueDate;

    /** ACCT-CURR-CYC-CREDIT PIC S9(10)V99 */
    @Column(name = "curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal currCycCredit;

    /** ACCT-CURR-CYC-DEBIT PIC S9(10)V99 */
    @Column(name = "curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal currCycDebit;

    /** ACCT-ADDR-ZIP PIC X(10) */
    @Column(name = "address_zip", length = 10)
    private String addressZip;

    /** ACCT-GROUP-ID PIC X(10) */
    @Column(name = "group_id", length = 10)
    private String groupId;
}

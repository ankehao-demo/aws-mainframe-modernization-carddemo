package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * JPA entity mapped from COBOL copybook CVTRA02Y.cpy (DIS-GROUP-RECORD, RECLN 50).
 * Represents the DISCGRP VSAM KSDS file.
 */
@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroup.DisclosureGroupId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisclosureGroup {

    /** DIS-ACCT-GROUP-ID PIC X(10) */
    @Id
    @Column(name = "account_group_id", nullable = false, length = 10)
    private String accountGroupId;

    /** DIS-TRAN-TYPE-CD PIC X(02) */
    @Id
    @Column(name = "transaction_type_code", nullable = false, length = 2)
    private String transactionTypeCode;

    /** DIS-TRAN-CAT-CD PIC 9(04) */
    @Id
    @Column(name = "transaction_category_code", nullable = false)
    private Integer transactionCategoryCode;

    /** DIS-INT-RATE PIC S9(04)V99 */
    @Column(name = "interest_rate", precision = 6, scale = 2)
    private BigDecimal interestRate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisclosureGroupId implements Serializable {
        private String accountGroupId;
        private String transactionTypeCode;
        private Integer transactionCategoryCode;
    }
}

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
 * JPA entity mapped from COBOL copybook CVTRA01Y.cpy (TRAN-CAT-BAL-RECORD, RECLN 50).
 * Represents the TCATBALF VSAM KSDS file.
 */
@Entity
@Table(name = "category_balances")
@IdClass(CategoryBalance.CategoryBalanceId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryBalance {

    /** TRANCAT-ACCT-ID PIC 9(11) */
    @Id
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /** TRANCAT-TYPE-CD PIC X(02) */
    @Id
    @Column(name = "transaction_type_code", nullable = false, length = 2)
    private String transactionTypeCode;

    /** TRANCAT-CD PIC 9(04) */
    @Id
    @Column(name = "category_code", nullable = false, length = 4)
    private String categoryCode;

    /** TRAN-CAT-BAL PIC S9(09)V99 */
    @Column(name = "balance", precision = 11, scale = 2)
    private BigDecimal balance;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBalanceId implements Serializable {
        private Long accountId;
        private String transactionTypeCode;
        private String categoryCode;
    }
}

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
import java.time.LocalDateTime;

/**
 * JPA entity mapped from COBOL copybook CVTRA06Y.cpy (DALYTRAN-RECORD, RECLN 350).
 * Staging table for daily transactions before they are posted to the main transactions table.
 */
@Entity
@Table(name = "daily_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTransaction {

    /** DALYTRAN-ID PIC X(16) */
    @Id
    @Column(name = "transaction_id", nullable = false, length = 16)
    private String transactionId;

    /** DALYTRAN-TYPE-CD PIC X(02) */
    @Column(name = "transaction_type_code", length = 2)
    private String transactionTypeCode;

    /** DALYTRAN-CAT-CD PIC 9(04) */
    @Column(name = "transaction_category_code", length = 4)
    private String transactionCategoryCode;

    /** DALYTRAN-SOURCE PIC X(10) */
    @Column(name = "transaction_source", length = 10)
    private String transactionSource;

    /** DALYTRAN-DESC PIC X(100) */
    @Column(name = "transaction_description", length = 100)
    private String transactionDescription;

    /** DALYTRAN-AMT PIC S9(09)V99 */
    @Column(name = "transaction_amount", precision = 11, scale = 2, nullable = false)
    private BigDecimal transactionAmount;

    /** DALYTRAN-MERCHANT-ID PIC 9(09) */
    @Column(name = "merchant_id", length = 9)
    private String merchantId;

    /** DALYTRAN-MERCHANT-NAME PIC X(50) */
    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    /** DALYTRAN-MERCHANT-CITY PIC X(50) */
    @Column(name = "merchant_city", length = 50)
    private String merchantCity;

    /** DALYTRAN-MERCHANT-ZIP PIC X(10) */
    @Column(name = "merchant_zip", length = 10)
    private String merchantZip;

    /** DALYTRAN-CARD-NUM PIC X(16) */
    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    /** DALYTRAN-ORIG-TS PIC X(26) */
    @Column(name = "transaction_timestamp")
    private LocalDateTime transactionTimestamp;

    /** DALYTRAN-PROC-TS PIC X(26) */
    @Column(name = "processed_timestamp")
    private LocalDateTime processedTimestamp;
}

package com.cardemo.dto;

import java.math.BigDecimal;

/**
 * DTO for transaction list row items.
 * From COTRN00C.cbl POPULATE-TRAN-DATA lines 381-445.
 * Date formatted MM/DD/YY from TRAN-ORIG-TS, amount formatted with sign and decimals
 * from PIC +99999999.99.
 */
public class TransactionListResponse {

    private String transactionId;
    private String date;
    private String description;
    private BigDecimal amount;

    public TransactionListResponse() {
    }

    public TransactionListResponse(String transactionId, String date,
                                   String description, BigDecimal amount) {
        this.transactionId = transactionId;
        this.date = date;
        this.description = description;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

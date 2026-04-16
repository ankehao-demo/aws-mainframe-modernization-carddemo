package com.carddemo.transaction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Daily transaction entity - from copybook CVTRA06Y.cpy (350-byte records).
 * Dataset: AWS.M2.CARDDEMO.DALYTRAN.PS
 */
@Entity
@Table(name = "daily_transactions")
public class DailyTransaction {
    @Id
    @Column(name = "transaction_id", length = 16)
    private String transactionId;

    @Column(name = "type_code", length = 2)
    private String typeCode;

    @Column(name = "category_code")
    private Integer categoryCode;

    @Column(name = "source", length = 10)
    private String source;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "amount", precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    @Column(name = "merchant_city", length = 50)
    private String merchantCity;

    @Column(name = "merchant_zip", length = 10)
    private String merchantZip;

    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "original_timestamp", length = 26)
    private String originalTimestamp;

    @Column(name = "processed_timestamp", length = 26)
    private String processedTimestamp;

    public DailyTransaction() {}
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public Integer getCategoryCode() { return categoryCode; }
    public void setCategoryCode(Integer categoryCode) { this.categoryCode = categoryCode; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String merchantCity) { this.merchantCity = merchantCity; }
    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String merchantZip) { this.merchantZip = merchantZip; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getOriginalTimestamp() { return originalTimestamp; }
    public void setOriginalTimestamp(String originalTimestamp) { this.originalTimestamp = originalTimestamp; }
    public String getProcessedTimestamp() { return processedTimestamp; }
    public void setProcessedTimestamp(String processedTimestamp) { this.processedTimestamp = processedTimestamp; }
}

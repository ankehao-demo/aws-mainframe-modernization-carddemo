package com.carddemo.account.entity;

import jakarta.persistence.*;

/**
 * Card cross-reference entity - from copybook CVACT03Y.cpy (50-byte records).
 * Dataset: AWS.M2.CARDDEMO.CARDXREF.PS
 */
@Entity
@Table(name = "card_xref", indexes = {
    @Index(name = "idx_card_xref_account", columnList = "account_id"),
    @Index(name = "idx_card_xref_customer", columnList = "customer_id")
})
public class CardXref {
    @Id
    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "account_id")
    private Long accountId;

    public CardXref() {}
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
}

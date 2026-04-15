package com.carddemo.card.entity;

import jakarta.persistence.*;

/**
 * Card entity - from copybook CVACT02Y.cpy (150-byte records).
 * Dataset: AWS.M2.CARDDEMO.CARDDATA.PS
 */
@Entity
@Table(name = "cards", indexes = {
    @Index(name = "idx_cards_account", columnList = "account_id")
})
public class Card {
    @Id
    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "cvv_code")
    private Integer cvvCode;

    @Column(name = "embossed_name", length = 50)
    private String embossedName;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "active_status", length = 1)
    private String activeStatus;

    public Card() {}
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public Integer getCvvCode() { return cvvCode; }
    public void setCvvCode(Integer cvvCode) { this.cvvCode = cvvCode; }
    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}

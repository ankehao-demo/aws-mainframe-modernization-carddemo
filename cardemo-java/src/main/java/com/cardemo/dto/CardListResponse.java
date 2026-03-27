package com.cardemo.dto;

/**
 * DTO for card list row items.
 * Mirrors WS-SCREEN-ROWS structure at COCRDLIC.cbl lines 256-260.
 */
public class CardListResponse {

    private Long accountId;
    private String cardNumber;
    private String cardStatus;

    public CardListResponse() {
    }

    public CardListResponse(Long accountId, String cardNumber, String cardStatus) {
        this.accountId = accountId;
        this.cardNumber = cardNumber;
        this.cardStatus = cardStatus;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(String cardStatus) {
        this.cardStatus = cardStatus;
    }
}

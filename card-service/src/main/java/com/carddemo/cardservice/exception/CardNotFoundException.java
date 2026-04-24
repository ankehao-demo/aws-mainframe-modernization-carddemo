package com.carddemo.cardservice.exception;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String cardNum) {
        super("Card not found: " + cardNum);
    }
}

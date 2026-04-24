package com.carddemo.cardservice.exception;

public class CardValidationException extends RuntimeException {

    public CardValidationException(String message) {
        super(message);
    }
}

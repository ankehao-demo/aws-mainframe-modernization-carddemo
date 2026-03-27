package com.cardemo.exception;

/**
 * Base exception class for CardDemo application errors.
 */
public class CardDemoException extends RuntimeException {

    public CardDemoException(String message) {
        super(message);
    }

    public CardDemoException(String message, Throwable cause) {
        super(message, cause);
    }
}

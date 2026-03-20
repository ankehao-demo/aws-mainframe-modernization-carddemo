package com.aws.carddemo.exception;

public class BusinessValidationException extends RuntimeException {

    private final int reasonCode;

    public BusinessValidationException(int reasonCode, String message) {
        super(message);
        this.reasonCode = reasonCode;
    }

    public int getReasonCode() {
        return reasonCode;
    }
}

package com.carddemo.cardservice.exception;

/**
 * Thrown when the version provided in an update request does not match
 * the current version in the database. This replaces the COBOL pattern
 * in COCRDUPC.cbl where CCUP-OLD-CARDDATA is compared to CCUP-NEW-CARDDATA
 * to detect concurrent modifications.
 */
public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String cardNum) {
        super("Card " + cardNum + " was modified by another user. Please refresh and try again.");
    }
}

package com.cardemo.exception;

/**
 * Exception for DFHRESP(NOTFND) conditions mapped to HTTP 404.
 * Used across all Phase 2 read-only programs when a record is not found
 * in VSAM files (ACCTDAT, CUSTDAT, CARDDAT, TRANSACT, CXACAIX).
 */
public class ResourceNotFoundException extends CardDemoException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

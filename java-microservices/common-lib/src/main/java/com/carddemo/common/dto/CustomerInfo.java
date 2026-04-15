package com.carddemo.common.dto;

/**
 * Mirrors CDEMO-CUSTOMER-INFO from COCOM01Y.cpy CARDDEMO-COMMAREA.
 */
public record CustomerInfo(
        long customerId,
        String firstName,
        String middleName,
        String lastName
) {}

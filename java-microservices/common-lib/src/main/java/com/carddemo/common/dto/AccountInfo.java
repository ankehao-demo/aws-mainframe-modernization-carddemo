package com.carddemo.common.dto;

/**
 * Mirrors CDEMO-ACCOUNT-INFO from COCOM01Y.cpy CARDDEMO-COMMAREA.
 */
public record AccountInfo(
        long accountId,
        String accountStatus
) {}

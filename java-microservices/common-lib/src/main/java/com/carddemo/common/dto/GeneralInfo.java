package com.carddemo.common.dto;

/**
 * Mirrors CDEMO-GENERAL-INFO from COCOM01Y.cpy CARDDEMO-COMMAREA.
 */
public record GeneralInfo(
        String fromTranId,
        String fromProgram,
        String toTranId,
        String toProgram,
        String userId,
        String userType,
        int pgmContext
) {}

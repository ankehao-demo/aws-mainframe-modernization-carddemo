package com.carddemo.cardservice.model.dto;

/**
 * Full card detail response, replacing the COCRDSLC.cbl card view transaction (CCDL).
 */
public record CardDetailResponse(
        String cardNum,
        String cardAcctId,
        String cardCvvCd,
        String cardEmbossedName,
        String cardExpirationDate,
        String cardActiveStatus,
        Long version
) {
}

package com.carddemo.cardservice.model.dto;

import java.util.List;

/**
 * Paginated response for the card list endpoint.
 * Replaces the CICS STARTBR/READNEXT browsing from COCRDLIC.cbl.
 */
public record CardListResponse(
        List<CardSummary> cards,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public record CardSummary(
            String cardNum,
            String cardAcctId,
            String cardActiveStatus
    ) {
    }
}

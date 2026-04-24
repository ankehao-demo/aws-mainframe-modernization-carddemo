package com.carddemo.cardservice.model.dto;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * Query parameters for the card list endpoint.
 * Mirrors the filter logic from COCRDLIC.cbl which filters by account and/or card number.
 */
public record CardListRequest(
        @Parameter(description = "Filter by account ID (11 digits)")
        String accountId,

        @Parameter(description = "Filter by card number prefix (up to 16 digits)")
        String cardNum,

        @Parameter(description = "Page number (0-based)", example = "0")
        Integer page,

        @Parameter(description = "Page size (default 7, matching COBOL WS-MAX-SCREEN-LINES)", example = "7")
        Integer size
) {
    public CardListRequest {
        if (page == null) page = 0;
        if (size == null) size = 7;
    }
}

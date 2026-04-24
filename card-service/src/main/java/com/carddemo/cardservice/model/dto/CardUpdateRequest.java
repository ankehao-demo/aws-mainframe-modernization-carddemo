package com.carddemo.cardservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for card update, replacing the COCRDUPC.cbl update transaction (CCUP).
 * Fields mirror the editable fields from the CICS update screen.
 */
@Schema(description = "Card update request payload")
public record CardUpdateRequest(
        @Schema(description = "Account ID, must be 11 numeric digits", example = "00000000001")
        @NotBlank(message = "Account ID is required")
        String cardAcctId,

        @Schema(description = "Embossed name on card, alphabets and spaces only", example = "JOHN DOE")
        @NotBlank(message = "Card embossed name is required")
        String cardEmbossedName,

        @Schema(description = "Card active status: Y or N", example = "Y")
        @NotBlank(message = "Card active status is required")
        String cardActiveStatus,

        @Schema(description = "Expiration date in MM/YYYY format", example = "12/2028")
        @NotBlank(message = "Card expiration date is required")
        String cardExpirationDate,

        @Schema(description = "Optimistic lock version from the last read")
        @NotNull(message = "Version is required for optimistic locking")
        Long version
) {
}

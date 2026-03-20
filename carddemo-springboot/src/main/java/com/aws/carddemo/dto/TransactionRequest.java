package com.aws.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank String typeCd,
        @NotNull Integer catCd,
        String source,
        String description,
        @NotNull BigDecimal amount,
        String merchantId,
        String merchantName,
        String merchantCity,
        String merchantZip,
        @NotBlank String cardNum
) {}

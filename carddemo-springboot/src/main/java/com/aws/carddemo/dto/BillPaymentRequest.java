package com.aws.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record BillPaymentRequest(
        @NotBlank String acctId,
        @NotNull @Positive BigDecimal amount
) {}

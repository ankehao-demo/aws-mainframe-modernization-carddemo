package com.carddemo.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillPaymentRequest {

    @NotNull(message = "Account ID is required")
    @JsonProperty("account_id")
    private Long accountId;

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    @JsonProperty("payment_amount")
    private BigDecimal paymentAmount;

    @JsonProperty("confirm_payment")
    private boolean confirmPayment;
}

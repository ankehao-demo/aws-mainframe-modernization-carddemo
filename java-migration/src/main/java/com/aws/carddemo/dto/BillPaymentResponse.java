package com.aws.carddemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillPaymentResponse {
    private String message;
    private String tranId;
    private BigDecimal tranAmt;
    private BigDecimal newBalance;
    private boolean success;
}

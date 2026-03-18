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
public class AuthorizationResponse {
    private String cardNum;
    private String authId;
    private String responseCode;
    private BigDecimal approvedAmount;
    private String message;
}

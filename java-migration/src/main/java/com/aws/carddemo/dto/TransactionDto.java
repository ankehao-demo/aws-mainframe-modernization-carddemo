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
public class TransactionDto {
    private String tranId;
    private String tranTypeCd;
    private Integer tranCatCd;
    private String tranSource;
    private String tranDesc;
    private BigDecimal tranAmt;
    private Long tranMerchantId;
    private String tranMerchantName;
    private String tranMerchantCity;
    private String tranMerchantZip;
    private String tranCardNum;
    private String tranOrigTs;
    private String tranProcTs;
}

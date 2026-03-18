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
public class AccountDto {
    private String acctId;
    private String acctActiveStatus;
    private BigDecimal acctCurrBal;
    private BigDecimal acctCreditLimit;
    private BigDecimal acctCashCreditLimit;
    private String acctOpenDate;
    private String acctExpirationDate;
    private String acctReissueDate;
    private BigDecimal acctCurrCycCredit;
    private BigDecimal acctCurrCycDebit;
    private String acctAddrZip;
    private String acctGroupId;
    private CustomerDto customer;
}

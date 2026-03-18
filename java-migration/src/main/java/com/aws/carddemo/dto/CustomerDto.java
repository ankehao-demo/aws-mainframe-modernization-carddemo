package com.aws.carddemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    private String custId;
    private String custFirstName;
    private String custMiddleName;
    private String custLastName;
    private String custAddrLine1;
    private String custAddrLine2;
    private String custAddrLine3;
    private String custAddrStateCd;
    private String custAddrCountryCd;
    private String custAddrZip;
    private String custPhoneNum1;
    private String custPhoneNum2;
    private String custSsn;
    private String custGovtIssuedId;
    private String custDobYyyyMmDd;
    private String custEftAccountId;
    private String custPriCardHolderInd;
    private Integer custFicoCreditScore;
}

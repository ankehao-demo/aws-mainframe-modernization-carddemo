package com.aws.carddemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {
    private String cardNum;
    private String cardAcctId;
    private String cardCvvCd;
    private String cardEmbossedName;
    private String cardExpirationDate;
    private String cardActiveStatus;
}

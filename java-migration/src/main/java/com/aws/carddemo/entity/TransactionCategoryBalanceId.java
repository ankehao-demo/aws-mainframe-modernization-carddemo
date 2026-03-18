package com.aws.carddemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryBalanceId implements Serializable {
    private String trancatAcctId;
    private String trancatTypeCd;
    private Integer trancatCd;
}

package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tran_cat_balance")
@IdClass(TransactionCategoryBalanceId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCategoryBalance {

    @Id
    @Column(name = "trancat_acct_id", length = 11)
    private String trancatAcctId;

    @Id
    @Column(name = "trancat_type_cd", length = 2)
    private String trancatTypeCd;

    @Id
    @Column(name = "trancat_cd")
    private Integer trancatCd;

    @Column(name = "tran_cat_bal", precision = 11, scale = 2, nullable = false)
    private BigDecimal tranCatBal;
}

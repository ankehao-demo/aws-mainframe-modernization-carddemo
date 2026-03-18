package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "daily_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dalytran_id", length = 16)
    private String dalytranId;

    @Column(name = "dalytran_type_cd", length = 2)
    private String dalytranTypeCd;

    @Column(name = "dalytran_cat_cd")
    private Integer dalytranCatCd;

    @Column(name = "dalytran_source", length = 10)
    private String dalytranSource;

    @Column(name = "dalytran_desc", length = 100)
    private String dalytranDesc;

    @Column(name = "dalytran_amt", precision = 11, scale = 2)
    private BigDecimal dalytranAmt;

    @Column(name = "dalytran_merchant_id")
    private Long dalytranMerchantId;

    @Column(name = "dalytran_merchant_name", length = 50)
    private String dalytranMerchantName;

    @Column(name = "dalytran_merchant_city", length = 50)
    private String dalytranMerchantCity;

    @Column(name = "dalytran_merchant_zip", length = 10)
    private String dalytranMerchantZip;

    @Column(name = "dalytran_card_num", length = 16)
    private String dalytranCardNum;

    @Column(name = "dalytran_orig_ts", length = 26)
    private String dalytranOrigTs;

    @Column(name = "dalytran_proc_ts", length = 26)
    private String dalytranProcTs;

    @Column(name = "posted", nullable = false)
    private Boolean posted;
}

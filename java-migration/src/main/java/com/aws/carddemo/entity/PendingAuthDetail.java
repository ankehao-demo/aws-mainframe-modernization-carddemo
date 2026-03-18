package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "pending_auth_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingAuthDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", nullable = false)
    @ToString.Exclude
    private PendingAuthSummary summary;

    @Column(name = "pa_card_num", length = 16)
    private String paCardNum;

    @Column(name = "pa_tran_id", length = 20)
    private String paTranId;

    @Column(name = "pa_tran_amt", precision = 12, scale = 2)
    private BigDecimal paTranAmt;

    @Column(name = "pa_merchant_id", length = 20)
    private String paMerchantId;

    @Column(name = "pa_merchant_name", length = 50)
    private String paMerchantName;

    @Column(name = "pa_auth_date", length = 10)
    private String paAuthDate;

    @Column(name = "pa_auth_time", length = 12)
    private String paAuthTime;

    @Column(name = "pa_resp_code", length = 4)
    private String paRespCode;

    @Column(name = "pa_fraud_flag", length = 1)
    private String paFraudFlag;
}

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
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_fraud")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthFraud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "af_card_num", length = 16, nullable = false)
    private String afCardNum;

    @Column(name = "af_tran_id", length = 20)
    private String afTranId;

    @Column(name = "af_tran_amt", precision = 12, scale = 2)
    private BigDecimal afTranAmt;

    @Column(name = "af_merchant_id", length = 20)
    private String afMerchantId;

    @Column(name = "af_merchant_name", length = 50)
    private String afMerchantName;

    @Column(name = "af_fraud_date", nullable = false)
    private LocalDateTime afFraudDate;

    @Column(name = "af_fraud_type", length = 20)
    private String afFraudType;

    @Column(name = "af_notes", length = 200)
    private String afNotes;
}

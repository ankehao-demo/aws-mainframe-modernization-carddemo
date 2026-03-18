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

@Entity
@Table(name = "daily_transaction_reject")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTransactionReject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_transaction_id", nullable = false)
    @ToString.Exclude
    private DailyTransaction dailyTransaction;

    @Column(name = "reject_reason_code", nullable = false)
    private Integer rejectReasonCode;

    @Column(name = "reject_reason_desc", length = 100)
    private String rejectReasonDesc;
}

package com.aws.carddemo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pending_auth_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingAuthSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pa_acct_id", length = 11, nullable = false, unique = true)
    private String paAcctId;

    @Column(name = "pa_approved_count", nullable = false)
    private Integer paApprovedCount;

    @Column(name = "pa_approved_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal paApprovedAmount;

    @Column(name = "pa_declined_count", nullable = false)
    private Integer paDeclinedCount;

    @Column(name = "pa_declined_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal paDeclinedAmount;

    @Column(name = "pa_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal paCreditLimit;

    @Column(name = "pa_credit_available", precision = 12, scale = 2, nullable = false)
    private BigDecimal paCreditAvailable;

    @OneToMany(mappedBy = "summary", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<PendingAuthDetail> details = new ArrayList<>();
}

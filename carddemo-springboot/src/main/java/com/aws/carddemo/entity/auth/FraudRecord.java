package com.aws.carddemo.entity.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fraud record — replaces DB2 AUTHFRDS table from the authorization module.
 * Records fraudulent authorization attempts.
 */
@Entity
@Table(name = "fraud_record")
public class FraudRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fraud_id")
    private Long fraudId;

    @Column(name = "auth_id", length = 16)
    private String authId;

    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "acct_id", length = 11)
    private String acctId;

    @Column(name = "fraud_type", length = 20)
    private String fraudType;

    @Column(name = "fraud_amount", precision = 12, scale = 2)
    private BigDecimal fraudAmount;

    @Column(name = "fraud_description", length = 255)
    private String fraudDescription;

    @Column(name = "reported_date")
    private LocalDateTime reportedDate;

    @Column(name = "status", length = 10)
    private String status;

    public Long getFraudId() { return fraudId; }
    public void setFraudId(Long fraudId) { this.fraudId = fraudId; }
    public String getAuthId() { return authId; }
    public void setAuthId(String authId) { this.authId = authId; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }
    public String getFraudType() { return fraudType; }
    public void setFraudType(String fraudType) { this.fraudType = fraudType; }
    public BigDecimal getFraudAmount() { return fraudAmount; }
    public void setFraudAmount(BigDecimal fraudAmount) { this.fraudAmount = fraudAmount; }
    public String getFraudDescription() { return fraudDescription; }
    public void setFraudDescription(String fraudDescription) { this.fraudDescription = fraudDescription; }
    public LocalDateTime getReportedDate() { return reportedDate; }
    public void setReportedDate(LocalDateTime reportedDate) { this.reportedDate = reportedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

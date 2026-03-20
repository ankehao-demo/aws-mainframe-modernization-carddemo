package com.aws.carddemo.entity.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pending authorization summary — replaces IMS HIDAM root segment.
 * Maps the authorization summary from app-authorization-ims-db2-mq module.
 */
@Entity
@Table(name = "pending_auth_summary")
public class PendingAuthSummary {

    @Id
    @Column(name = "auth_id", length = 16)
    private String authId;

    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "acct_id", length = 11)
    private String acctId;

    @Column(name = "auth_amount", precision = 12, scale = 2)
    private BigDecimal authAmount;

    @Column(name = "auth_status", length = 2)
    private String authStatus;

    @Column(name = "auth_timestamp")
    private LocalDateTime authTimestamp;

    @Column(name = "merchant_id", length = 9)
    private String merchantId;

    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    @OneToMany(mappedBy = "summary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PendingAuthDetail> details = new ArrayList<>();

    public String getAuthId() { return authId; }
    public void setAuthId(String authId) { this.authId = authId; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }
    public BigDecimal getAuthAmount() { return authAmount; }
    public void setAuthAmount(BigDecimal authAmount) { this.authAmount = authAmount; }
    public String getAuthStatus() { return authStatus; }
    public void setAuthStatus(String authStatus) { this.authStatus = authStatus; }
    public LocalDateTime getAuthTimestamp() { return authTimestamp; }
    public void setAuthTimestamp(LocalDateTime authTimestamp) { this.authTimestamp = authTimestamp; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public List<PendingAuthDetail> getDetails() { return details; }
    public void setDetails(List<PendingAuthDetail> details) { this.details = details; }
}

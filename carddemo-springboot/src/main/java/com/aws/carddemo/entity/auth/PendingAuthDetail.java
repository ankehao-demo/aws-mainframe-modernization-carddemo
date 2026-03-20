package com.aws.carddemo.entity.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Pending authorization detail — replaces IMS HIDAM child segment.
 * Maps the authorization detail records under each summary.
 */
@Entity
@Table(name = "pending_auth_detail")
public class PendingAuthDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_id")
    @JsonIgnore
    private PendingAuthSummary summary;

    @Column(name = "detail_type", length = 10)
    private String detailType;

    @Column(name = "detail_message", length = 255)
    private String detailMessage;

    @Column(name = "detail_timestamp")
    private LocalDateTime detailTimestamp;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public PendingAuthSummary getSummary() { return summary; }
    public void setSummary(PendingAuthSummary summary) { this.summary = summary; }
    public String getDetailType() { return detailType; }
    public void setDetailType(String detailType) { this.detailType = detailType; }
    public String getDetailMessage() { return detailMessage; }
    public void setDetailMessage(String detailMessage) { this.detailMessage = detailMessage; }
    public LocalDateTime getDetailTimestamp() { return detailTimestamp; }
    public void setDetailTimestamp(LocalDateTime detailTimestamp) { this.detailTimestamp = detailTimestamp; }
}

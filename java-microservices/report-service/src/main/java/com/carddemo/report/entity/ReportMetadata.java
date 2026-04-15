package com.carddemo.report.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "report_metadata")
public class ReportMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_type", length = 50)
    private String reportType;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "completed_at")
    private String completedAt;

    @Column(name = "parameters", length = 500)
    private String parameters;

    @Column(name = "file_path", length = 255)
    private String filePath;

    public ReportMetadata() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}

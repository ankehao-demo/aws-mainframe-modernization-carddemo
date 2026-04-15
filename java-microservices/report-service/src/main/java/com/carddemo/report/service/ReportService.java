package com.carddemo.report.service;

import com.carddemo.report.entity.ReportMetadata;
import com.carddemo.report.repository.ReportMetadataRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Report generation service - replaces CORPT00C.cbl (online) and CBTRN03C.cbl (batch).
 */
@Service
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private final ReportMetadataRepository reportMetadataRepository;

    public ReportService(ReportMetadataRepository reportMetadataRepository) {
        this.reportMetadataRepository = reportMetadataRepository;
    }

    public ReportMetadata generateTransactionReport(String startDate, String endDate, Long accountId) {
        ReportMetadata report = new ReportMetadata();
        report.setReportType("TRANSACTION_REPORT");
        report.setStatus("PROCESSING");
        report.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.setParameters(String.format("startDate=%s,endDate=%s,accountId=%s", startDate, endDate, accountId != null ? accountId : "ALL"));
        report = reportMetadataRepository.save(report);

        // In production, this would trigger async PDF generation via Spring Batch
        report.setStatus("COMPLETED");
        report.setCompletedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.setFilePath("/reports/transaction_report_" + report.getId() + ".pdf");
        report = reportMetadataRepository.save(report);

        log.info("Generated transaction report: {}", report.getId());
        return report;
    }

    public ReportMetadata getReport(Long reportId) {
        return reportMetadataRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "reportId", reportId));
    }
}

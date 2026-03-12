package com.carddemo.report.controller;

import com.carddemo.common.dto.ReportRequest;
import com.carddemo.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Ports COBOL program CORPT00C.cbl (CRPT transaction).
 * Report generation endpoints.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report generation — replaces CORPT00C/CBSTM03A")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/transactions")
    @Operation(summary = "Generate transaction report", description = "Generate a transaction report for a date range")
    public ResponseEntity<Map<String, Object>> generateTransactionReport(
            @Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.generateTransactionReport(request));
    }

    @PostMapping("/statements")
    @Operation(summary = "Launch statement generation", description = "Launch batch job for PDF statement generation")
    public ResponseEntity<Map<String, String>> launchStatementGeneration() {
        try {
            reportService.launchStatementGeneration();
            return ResponseEntity.ok(Map.of("status", "Statement generation job launched"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "Failed", "error", e.getMessage()));
        }
    }
}

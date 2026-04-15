package com.carddemo.report.controller;

import com.carddemo.report.entity.ReportMetadata;
import com.carddemo.report.service.ReportService;
import com.carddemo.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reporting", description = "Report generation - replaces CORPT00C/CBTRN03C")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/transactions")
    @Operation(summary = "Generate transaction report")
    public ResponseEntity<ApiResponse<ReportMetadata>> generateReport(@RequestBody Map<String, String> params) {
        ReportMetadata report = reportService.generateTransactionReport(
                params.getOrDefault("startDate", ""),
                params.getOrDefault("endDate", ""),
                params.containsKey("accountId") ? Long.parseLong(params.get("accountId")) : null);
        return ResponseEntity.ok(ApiResponse.ok("Report generated", report));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Download report")
    public ResponseEntity<ApiResponse<ReportMetadata>> getReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getReport(reportId)));
    }
}

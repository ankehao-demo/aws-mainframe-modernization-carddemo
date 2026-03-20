package com.aws.carddemo.controller;

import com.aws.carddemo.dto.ReportRequest;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<List<Transaction>> generateTransactionReport(
            @Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.generateTransactionReport(request.startDate(), request.endDate()));
    }
}

package com.aws.carddemo.controller;

import com.aws.carddemo.dto.TransactionDto;
import com.aws.carddemo.service.ReportService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactionReport(
            @RequestParam String cardNum,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(reportService.getTransactionReport(cardNum, pageable));
    }
}

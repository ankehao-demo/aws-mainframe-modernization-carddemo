package com.carddemo.report.service;

import com.carddemo.common.dto.ReportRequest;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ports COBOL program CORPT00C.cbl (CRPT transaction).
 * Generates transaction reports for a date range.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final JobLauncher jobLauncher;
    private final Job statementGenerationJob;

    public Map<String, Object> generateTransactionReport(ReportRequest request) {
        LocalDateTime startDate = request.getStartDate().atStartOfDay();
        LocalDateTime endDate = request.getEndDate().atTime(23, 59, 59);

        List<Transaction> transactions = transactionRepository
                .findByTransactionTimestampBetween(startDate, endDate);

        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getTransactionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        long totalCount = transactions.size();

        Map<String, Long> byType = new HashMap<>();
        Map<String, BigDecimal> amountByType = new HashMap<>();
        for (Transaction t : transactions) {
            String type = t.getTransactionTypeCode() != null ? t.getTransactionTypeCode() : "UNKNOWN";
            byType.merge(type, 1L, Long::sum);
            amountByType.merge(type, t.getTransactionAmount(),
                    (a, b) -> a.add(b).setScale(2, RoundingMode.HALF_UP));
        }

        Map<String, Object> report = new HashMap<>();
        report.put("startDate", request.getStartDate().toString());
        report.put("endDate", request.getEndDate().toString());
        report.put("totalTransactions", totalCount);
        report.put("totalAmount", totalAmount);
        report.put("transactionCountByType", byType);
        report.put("transactionAmountByType", amountByType);
        report.put("generatedAt", LocalDateTime.now().toString());

        log.info("Report generated: {} transactions, total amount: {}",
                totalCount, totalAmount);

        return report;
    }

    public void launchStatementGeneration() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(statementGenerationJob, params);
    }
}

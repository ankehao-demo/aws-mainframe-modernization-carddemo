package com.aws.carddemo.service;

import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Report generation — mirrors CORPT00C.cbl logic.
 * Generates transaction report data for a given date range.
 */
@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> generateTransactionReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return transactionRepository.findByDateRange(start, end);
    }
}

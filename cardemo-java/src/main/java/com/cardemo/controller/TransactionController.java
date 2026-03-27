package com.cardemo.controller;

import com.cardemo.dto.TransactionDetailResponse;
import com.cardemo.dto.TransactionListPageResponse;
import com.cardemo.exception.CardDemoException;
import com.cardemo.service.TransactionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transaction controller converting COTRN00C.cbl (list) and COTRN01C.cbl (detail) logic.
 *
 * COTRN00C transaction list (699 lines):
 *   - Lists transactions with pagination (10 per page in COBOL)
 *   - VSAM browse (STARTBR/READNEXT/READPREV/ENDBR) on TRANSACT file
 *   - Supports selection of a transaction to view detail
 *   - Validation at lines 206-218
 *
 * COTRN01C transaction detail (330 lines):
 *   - Shows full details of a single transaction
 *   - READ-ONLY access (no UPDATE lock)
 *   - Validation at lines 146-156
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<TransactionListPageResponse> listTransactions(
            @RequestParam(required = false) String startTransactionId,
            @PageableDefault(size = 10) Pageable pageable) {

        if (startTransactionId != null && !startTransactionId.isBlank()) {
            if (!startTransactionId.matches("\\d+")) {
                throw new CardDemoException(
                        "Transaction ID filter must be numeric");
            }
        }

        TransactionListPageResponse response = transactionService.listTransactions(
                startTransactionId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDetailResponse> getTransactionDetail(
            @PathVariable String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new CardDemoException("Transaction ID cannot be empty");
        }
        TransactionDetailResponse response = transactionService.getTransactionDetail(
                transactionId);
        return ResponseEntity.ok(response);
    }
}

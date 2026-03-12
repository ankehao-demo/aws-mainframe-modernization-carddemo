package com.carddemo.transaction.controller;

import com.carddemo.common.dto.TransactionDto;
import com.carddemo.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ports COBOL programs:
 * - COTRN00C.cbl (CT00 transaction) → listTransactions
 * - COTRN01C.cbl (CT01 transaction) → getTransaction
 * - COTRN02C.cbl (CT02 transaction) → createTransaction
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "Transaction operations — replaces COTRN00C-02C")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List transactions", description = "List transactions with optional account filter")
    public ResponseEntity<Page<TransactionDto>> listTransactions(
            @RequestParam(required = false) Long accountId,
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.listTransactions(accountId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "View transaction", description = "Get transaction details by ID")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @PostMapping
    @Operation(summary = "Add transaction", description = "Create a new transaction")
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody TransactionDto transactionDto) {
        TransactionDto created = transactionService.createTransaction(transactionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

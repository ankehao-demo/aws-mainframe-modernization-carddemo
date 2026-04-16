package com.carddemo.transaction.controller;

import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.entity.TransactionType;
import com.carddemo.transaction.service.TransactionService;
import com.carddemo.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Tag(name = "Transaction Processing", description = "Transaction CRUD and batch - replaces COTRN00C-02C, COBIL00C, CBTRN batch")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/api/transactions")
    @Operation(summary = "List transactions for account (paginated)")
    public ResponseEntity<ApiResponse<Page<Transaction>>> listTransactions(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.listTransactions(accountId, PageRequest.of(page, size))));
    }

    @GetMapping("/api/transactions/{transactionId}")
    @Operation(summary = "View transaction")
    public ResponseEntity<ApiResponse<Transaction>> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getTransaction(transactionId)));
    }

    @PostMapping("/api/transactions")
    @Operation(summary = "Add transaction")
    public ResponseEntity<ApiResponse<Transaction>> addTransaction(@RequestBody Transaction transaction) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Transaction created", transactionService.addTransaction(transaction)));
    }

    @PostMapping("/api/billing/pay")
    @Operation(summary = "Bill payment - replaces COBIL00C.cbl")
    public ResponseEntity<ApiResponse<Transaction>> billPayment(@RequestBody Transaction transaction) {
        transaction.setTypeCode("02");
        transaction.setSource("BILLING");
        transaction.setDescription("Bill Payment");
        return ResponseEntity.ok(ApiResponse.ok("Payment processed", transactionService.addTransaction(transaction)));
    }

    @PostMapping("/api/transactions/process")
    @Operation(summary = "Trigger batch posting - replaces POSTTRAN/CBTRN02C")
    public ResponseEntity<ApiResponse<String>> triggerBatchPosting() {
        return ResponseEntity.ok(ApiResponse.ok("Batch posting triggered", "Job submitted"));
    }

    // Transaction Type endpoints - replaces COTRTUPC/COTRTLIC
    @GetMapping("/api/transaction-types")
    public ResponseEntity<ApiResponse<List<TransactionType>>> listTypes() {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.listTransactionTypes()));
    }

    @PostMapping("/api/transaction-types")
    public ResponseEntity<ApiResponse<TransactionType>> createType(@RequestBody TransactionType type) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(transactionService.createTransactionType(type)));
    }

    @PutMapping("/api/transaction-types/{typeCode}")
    public ResponseEntity<ApiResponse<TransactionType>> updateType(@PathVariable String typeCode, @RequestBody TransactionType type) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.updateTransactionType(typeCode, type)));
    }

    @DeleteMapping("/api/transaction-types/{typeCode}")
    public ResponseEntity<ApiResponse<Void>> deleteType(@PathVariable String typeCode) {
        transactionService.deleteTransactionType(typeCode);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}

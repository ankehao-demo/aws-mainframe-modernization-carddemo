package com.aws.carddemo.controller;

import com.aws.carddemo.dto.TransactionRequest;
import com.aws.carddemo.entity.DailyTransaction;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.service.TransactionService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<Page<Transaction>> listTransactions(
            @RequestParam(required = false) String acctId,
            @RequestParam(required = false) String cardNum,
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.listTransactions(acctId, cardNum, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable("id") String tranId) {
        return ResponseEntity.ok(transactionService.getTransaction(tranId));
    }

    @PostMapping
    public ResponseEntity<DailyTransaction> addTransaction(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.addTransaction(request));
    }
}

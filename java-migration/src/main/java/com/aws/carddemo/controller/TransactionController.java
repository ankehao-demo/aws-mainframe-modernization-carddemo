package com.aws.carddemo.controller;

import com.aws.carddemo.dto.PagedResponse;
import com.aws.carddemo.dto.TransactionDto;
import com.aws.carddemo.service.TransactionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<PagedResponse<TransactionDto>> listTransactions(
            @RequestParam String cardNum,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(transactionService.listTransactions(cardNum, pageable));
    }

    @GetMapping("/{tranId}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable String tranId) {
        return ResponseEntity.ok(transactionService.getTransaction(tranId));
    }

    @PostMapping
    public ResponseEntity<TransactionDto> addTransaction(@RequestBody TransactionDto transactionDto) {
        return ResponseEntity.ok(transactionService.addTransaction(transactionDto));
    }
}

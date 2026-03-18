package com.aws.carddemo.controller;

import com.aws.carddemo.dto.TransactionTypeDto;
import com.aws.carddemo.service.TransactionTypeService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/transaction-types")
@PreAuthorize("hasRole('ADMIN')")
public class TransactionTypeController {

    private final TransactionTypeService transactionTypeService;

    public TransactionTypeController(TransactionTypeService transactionTypeService) {
        this.transactionTypeService = transactionTypeService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionTypeDto>> listTransactionTypes(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transactionTypeService.listTransactionTypes(pageable));
    }

    @GetMapping("/{typeCode}")
    public ResponseEntity<TransactionTypeDto> getTransactionType(@PathVariable String typeCode) {
        return ResponseEntity.ok(transactionTypeService.getTransactionType(typeCode));
    }

    @PostMapping
    public ResponseEntity<TransactionTypeDto> addTransactionType(@RequestBody TransactionTypeDto dto) {
        return ResponseEntity.ok(transactionTypeService.addTransactionType(dto));
    }

    @PutMapping("/{typeCode}")
    public ResponseEntity<TransactionTypeDto> updateTransactionType(
            @PathVariable String typeCode, @RequestBody TransactionTypeDto dto) {
        return ResponseEntity.ok(transactionTypeService.updateTransactionType(typeCode, dto));
    }
}

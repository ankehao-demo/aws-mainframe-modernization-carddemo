package com.carddemo.transaction.service;

import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.entity.TransactionType;
import com.carddemo.transaction.repository.TransactionRepository;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionTypeRepository transactionTypeRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionTypeRepository = transactionTypeRepository;
    }

    public Page<Transaction> listTransactions(Long accountId, Pageable pageable) {
        return transactionRepository.findByAccountId(accountId, pageable);
    }

    public Transaction getTransaction(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "transactionId", transactionId));
    }

    public Transaction addTransaction(Transaction transaction) {
        if (transaction.getTransactionId() == null || transaction.getTransactionId().isBlank()) {
            transaction.setTransactionId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        }
        if (transaction.getOriginalTimestamp() == null) {
            transaction.setOriginalTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")));
        }
        return transactionRepository.save(transaction);
    }

    // Transaction Type CRUD - replaces COTRTUPC/COTRTLIC
    public java.util.List<TransactionType> listTransactionTypes() {
        return transactionTypeRepository.findAll();
    }

    public TransactionType getTransactionType(String typeCode) {
        return transactionTypeRepository.findById(typeCode)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionType", "typeCode", typeCode));
    }

    public TransactionType createTransactionType(TransactionType type) {
        if (transactionTypeRepository.existsById(type.getTypeCode())) {
            throw new BadRequestException("Transaction type already exists: " + type.getTypeCode());
        }
        return transactionTypeRepository.save(type);
    }

    public TransactionType updateTransactionType(String typeCode, TransactionType updated) {
        TransactionType existing = getTransactionType(typeCode);
        if (updated.getTypeDescription() != null) existing.setTypeDescription(updated.getTypeDescription());
        return transactionTypeRepository.save(existing);
    }

    public void deleteTransactionType(String typeCode) {
        TransactionType type = getTransactionType(typeCode);
        transactionTypeRepository.delete(type);
    }
}

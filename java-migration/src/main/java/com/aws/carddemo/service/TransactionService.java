package com.aws.carddemo.service;

import com.aws.carddemo.dto.PagedResponse;
import com.aws.carddemo.dto.TransactionDto;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.exception.ValidationException;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CardXrefRepository cardXrefRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransactionDto> listTransactions(String cardNum, Pageable pageable) {
        Page<Transaction> page = transactionRepository.findByTranCardNum(cardNum, pageable);
        List<TransactionDto> transactions = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PagedResponse.<TransactionDto>builder()
                .content(transactions)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransaction(String tranId) {
        Transaction transaction = transactionRepository.findById(tranId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + tranId));
        return mapToDto(transaction);
    }

    @Transactional
    public TransactionDto addTransaction(TransactionDto dto) {
        if (dto.getTranCardNum() == null || dto.getTranCardNum().isBlank()) {
            throw new ValidationException("Card number is required");
        }

        // Validate card exists via card_xref
        cardXrefRepository.findById(dto.getTranCardNum())
                .orElseThrow(() -> new ValidationException("Card not found: " + dto.getTranCardNum()));

        // Auto-generate transaction ID (COBOL: READPREV + ADD 1)
        String newTranId = generateNextTranId();

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));

        Transaction transaction = Transaction.builder()
                .tranId(newTranId)
                .tranTypeCd(dto.getTranTypeCd())
                .tranCatCd(dto.getTranCatCd())
                .tranSource(dto.getTranSource())
                .tranDesc(dto.getTranDesc())
                .tranAmt(dto.getTranAmt())
                .tranMerchantId(dto.getTranMerchantId())
                .tranMerchantName(dto.getTranMerchantName())
                .tranMerchantCity(dto.getTranMerchantCity())
                .tranMerchantZip(dto.getTranMerchantZip())
                .tranCardNum(dto.getTranCardNum())
                .tranOrigTs(timestamp)
                .tranProcTs(timestamp)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToDto(saved);
    }

    public String generateNextTranId() {
        String maxId = transactionRepository.findMaxTranId().orElse("0000000000000000");
        long nextId = Long.parseLong(maxId.trim()) + 1;
        return String.format("%016d", nextId);
    }

    private TransactionDto mapToDto(Transaction transaction) {
        return TransactionDto.builder()
                .tranId(transaction.getTranId())
                .tranTypeCd(transaction.getTranTypeCd())
                .tranCatCd(transaction.getTranCatCd())
                .tranSource(transaction.getTranSource())
                .tranDesc(transaction.getTranDesc())
                .tranAmt(transaction.getTranAmt())
                .tranMerchantId(transaction.getTranMerchantId())
                .tranMerchantName(transaction.getTranMerchantName())
                .tranMerchantCity(transaction.getTranMerchantCity())
                .tranMerchantZip(transaction.getTranMerchantZip())
                .tranCardNum(transaction.getTranCardNum())
                .tranOrigTs(transaction.getTranOrigTs())
                .tranProcTs(transaction.getTranProcTs())
                .build();
    }
}

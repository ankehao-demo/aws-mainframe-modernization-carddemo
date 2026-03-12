package com.carddemo.transaction.service;

import com.carddemo.common.dto.TransactionDto;
import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.exception.BusinessException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.mapper.TransactionMapper;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import com.carddemo.common.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ports COBOL programs:
 * - COTRN00C.cbl (CT00) → listTransactions
 * - COTRN01C.cbl (CT01) → getTransaction
 * - COTRN02C.cbl (CT02) → createTransaction
 * Replaces CICS VSAM operations on TRANSACT file.
 * All monetary calculations use BigDecimal with HALF_UP rounding to match COBOL arithmetic.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    public Page<TransactionDto> listTransactions(Long accountId, Pageable pageable) {
        if (accountId != null) {
            return transactionRepository.findByAccountId(accountId, pageable)
                    .map(transactionMapper::toDto);
        }
        return transactionRepository.findAll(pageable).map(transactionMapper::toDto);
    }

    public TransactionDto getTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction", "transactionId", transactionId));
        return transactionMapper.toDto(transaction);
    }

    @Transactional
    public TransactionDto createTransaction(TransactionDto dto) {
        Card card = cardRepository.findById(dto.getCardNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Card", "cardNumber", dto.getCardNumber()));

        Account account = accountRepository.findById(card.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account", "accountId", card.getAccountId()));

        if (!"Y".equals(account.getAccountStatus())) {
            throw new BusinessException("Account is not active");
        }

        BigDecimal newBalance = account.getCurrentBalance()
                .add(dto.getTransactionAmount())
                .setScale(2, RoundingMode.HALF_UP);

        if (newBalance.compareTo(account.getCreditLimit()) > 0) {
            throw new BusinessException("Transaction would exceed credit limit");
        }

        String transactionId = String.format("%016d",
                Math.abs(UUID.randomUUID().getMostSignificantBits() % 10000000000000000L));

        Transaction transaction = transactionMapper.toEntity(dto);
        transaction.setTransactionId(transactionId);
        transaction.setTransactionTimestamp(LocalDateTime.now());
        transaction.setProcessedTimestamp(LocalDateTime.now());

        Transaction saved = transactionRepository.save(transaction);

        account.setCurrentBalance(newBalance);
        account.setCurrCycDebit(
                account.getCurrCycDebit().add(dto.getTransactionAmount())
                        .setScale(2, RoundingMode.HALF_UP));
        accountRepository.save(account);

        return transactionMapper.toDto(saved);
    }
}

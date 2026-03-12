package com.carddemo.billing.service;

import com.carddemo.common.dto.BillPaymentRequest;
import com.carddemo.common.dto.TransactionDto;
import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.exception.BusinessException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.mapper.TransactionMapper;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ports COBOL program COBIL00C.cbl (CBIL transaction).
 * Processes bill payments as credit transactions against an account.
 * All monetary operations use BigDecimal with HALF_UP rounding.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionDto makePayment(BillPaymentRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account", "accountId", request.getAccountId()));

        if (!"Y".equals(account.getAccountStatus())) {
            throw new BusinessException("Account is not active");
        }

        if (request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive");
        }

        BigDecimal newBalance = account.getCurrentBalance()
                .subtract(request.getPaymentAmount())
                .setScale(2, RoundingMode.HALF_UP);

        account.setCurrentBalance(newBalance);
        account.setCurrCycCredit(
                account.getCurrCycCredit().add(request.getPaymentAmount())
                        .setScale(2, RoundingMode.HALF_UP));
        accountRepository.save(account);

        String transactionId = String.format("%016d",
                Math.abs(UUID.randomUUID().getMostSignificantBits() % 10000000000000000L)).substring(0, 16);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setTransactionTypeCode("PM");
        transaction.setTransactionCategoryCode(1);
        transaction.setTransactionSource("ONLINE");
        transaction.setTransactionDescription("Bill Payment");
        transaction.setTransactionAmount(request.getPaymentAmount().negate());
        transaction.setCardNumber("PAYMENT");
        transaction.setTransactionTimestamp(LocalDateTime.now());
        transaction.setProcessedTimestamp(LocalDateTime.now());

        Transaction saved = transactionRepository.save(transaction);
        log.info("Payment processed: account={}, amount={}, newBalance={}",
                account.getAccountId(), request.getPaymentAmount(), newBalance);

        return transactionMapper.toDto(saved);
    }
}

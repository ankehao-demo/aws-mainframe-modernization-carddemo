package com.aws.carddemo.service;

import com.aws.carddemo.dto.BillPaymentRequest;
import com.aws.carddemo.dto.BillPaymentResponse;
import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.CardXref;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.exception.ValidationException;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BillPaymentService {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public BillPaymentService(AccountRepository accountRepository,
                              CardXrefRepository cardXrefRepository,
                              TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public BillPaymentResponse processBillPayment(BillPaymentRequest request) {
        // Validate acctId not empty
        if (request.getAcctId() == null || request.getAcctId().trim().isEmpty()) {
            throw new ValidationException("Acct ID can NOT be empty...");
        }

        // Validate confirm value
        String confirm = request.getConfirm();
        if (confirm == null || (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("N"))) {
            throw new ValidationException("Invalid value. Valid values are (Y/N)...");
        }

        // Look up account
        String acctId = request.getAcctId().trim();
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + acctId));

        // Check balance > 0
        if (account.getAcctCurrBal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("You have nothing to pay...");
        }

        // If confirm=N, return without action
        if (confirm.equalsIgnoreCase("N")) {
            return BillPaymentResponse.builder()
                    .message("Payment declined by user")
                    .success(false)
                    .build();
        }

        // confirm=Y: process payment
        // Look up card via card_xref by acctId
        CardXref cardXref = cardXrefRepository.findFirstByXrefAcctId(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("No card found for account: " + acctId));

        // Generate next transaction ID
        String maxId = transactionRepository.findMaxTranId().orElse("0000000000000000");
        long nextId = Long.parseLong(maxId.trim()) + 1;
        String newTranId = String.format("%016d", nextId);

        BigDecimal paymentAmount = account.getAcctCurrBal();
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .tranId(newTranId)
                .tranTypeCd("02")
                .tranCatCd(2)
                .tranSource("POS TERM")
                .tranDesc("BILL PAYMENT - ONLINE")
                .tranAmt(paymentAmount)
                .tranMerchantId(999999999L)
                .tranMerchantName("BILL PAYMENT")
                .tranMerchantCity("N/A")
                .tranMerchantZip("N/A")
                .tranCardNum(cardXref.getXrefCardNum())
                .tranOrigTs(timestamp)
                .tranProcTs(timestamp)
                .build();

        transactionRepository.save(transaction);

        // Update account: new_bal = old_bal - tran_amt
        BigDecimal newBalance = account.getAcctCurrBal().subtract(paymentAmount);
        account.setAcctCurrBal(newBalance);
        accountRepository.save(account);

        return BillPaymentResponse.builder()
                .message("Payment processed successfully")
                .tranId(newTranId)
                .tranAmt(paymentAmount)
                .newBalance(newBalance)
                .success(true)
                .build();
    }
}

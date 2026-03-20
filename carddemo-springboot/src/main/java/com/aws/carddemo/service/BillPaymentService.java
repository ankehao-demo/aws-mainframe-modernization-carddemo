package com.aws.carddemo.service;

import com.aws.carddemo.entity.Account;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * Bill payment processing — mirrors COBIL00C.cbl logic.
 * Processes payment by reducing account current balance.
 */
@Service
public class BillPaymentService {

    private final AccountRepository accountRepository;

    public BillPaymentService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account processPayment(String acctId, BigDecimal amount) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + acctId));

        // Payment reduces the balance (debit to the account)
        account.setCurrBal(account.getCurrBal().subtract(amount));
        account.setCurrCycDebit(account.getCurrCycDebit().add(amount));

        return accountRepository.save(account);
    }
}

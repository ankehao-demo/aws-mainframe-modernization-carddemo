package com.carddemo.account.service;

import com.carddemo.account.entity.Account;
import com.carddemo.account.entity.Customer;
import com.carddemo.account.entity.CardXref;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.account.repository.CustomerRepository;
import com.carddemo.account.repository.CardXrefRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CardXrefRepository cardXrefRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository, CardXrefRepository cardXrefRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountId", accountId));
    }

    public Account updateAccount(Long accountId, Account updated) {
        Account existing = getAccount(accountId);
        if (updated.getActiveStatus() != null) existing.setActiveStatus(updated.getActiveStatus());
        if (updated.getCreditLimit() != null) existing.setCreditLimit(updated.getCreditLimit());
        if (updated.getCashCreditLimit() != null) existing.setCashCreditLimit(updated.getCashCreditLimit());
        if (updated.getExpirationDate() != null) existing.setExpirationDate(updated.getExpirationDate());
        if (updated.getGroupId() != null) existing.setGroupId(updated.getGroupId());
        return accountRepository.save(existing);
    }

    public List<Customer> getCustomersForAccount(Long accountId) {
        List<CardXref> xrefs = cardXrefRepository.findByAccountId(accountId);
        return xrefs.stream()
                .map(xref -> customerRepository.findById(xref.getCustomerId()).orElse(null))
                .filter(c -> c != null)
                .distinct()
                .toList();
    }
}

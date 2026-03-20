package com.aws.carddemo.service;

import com.aws.carddemo.dto.AccountUpdateRequest;
import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.CardCrossRef;
import com.aws.carddemo.entity.Customer;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardCrossRefRepository;
import com.aws.carddemo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Account management — mirrors COACTVWC.cbl (view) and COACTUPC.cbl (update).
 * Reads account + card_cross_ref + customer data per the VSAM READ logic.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CardCrossRefRepository cardCrossRefRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CardCrossRefRepository cardCrossRefRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.cardCrossRefRepository = cardCrossRefRepository;
        this.customerRepository = customerRepository;
    }

    public Map<String, Object> getAccount(String acctId) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + acctId));

        List<CardCrossRef> crossRefs = cardCrossRefRepository.findByAcctId(acctId);

        Customer customer = null;
        if (!crossRefs.isEmpty()) {
            customer = customerRepository.findById(crossRefs.get(0).getCustId()).orElse(null);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("account", account);
        result.put("crossReferences", crossRefs);
        result.put("customer", customer);
        return result;
    }

    @Transactional
    public Account updateAccount(String acctId, AccountUpdateRequest request) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + acctId));

        if (request.activeStatus() != null) account.setActiveStatus(request.activeStatus());
        if (request.creditLimit() != null) account.setCreditLimit(request.creditLimit());
        if (request.cashCreditLimit() != null) account.setCashCreditLimit(request.cashCreditLimit());
        if (request.expirationDate() != null) account.setExpirationDate(request.expirationDate());
        if (request.reissueDate() != null) account.setReissueDate(request.reissueDate());
        if (request.addrZip() != null) account.setAddrZip(request.addrZip());
        if (request.groupId() != null) account.setGroupId(request.groupId());

        return accountRepository.save(account);
    }
}

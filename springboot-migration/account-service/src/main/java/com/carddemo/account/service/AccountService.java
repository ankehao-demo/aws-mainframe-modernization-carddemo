package com.carddemo.account.service;

import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.mapper.AccountMapper;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Ports COBOL programs:
 * - COACTVWC.cbl (CAVW) → getAccount, listAccounts
 * - COACTUPC.cbl (CAUP) → updateAccount
 * Replaces CICS READ/REWRITE on ACCTDAT VSAM file with JPA operations.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final AccountMapper accountMapper;

    public AccountDto getAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountId", accountId));
        return accountMapper.toDto(account);
    }

    public Page<AccountDto> listAccounts(Long customerId, Pageable pageable) {
        if (customerId != null) {
            List<Card> cards = cardRepository.findByCustomerId(customerId);
            List<Long> accountIds = cards.stream()
                    .map(Card::getAccountId)
                    .distinct()
                    .collect(Collectors.toList());
            return accountRepository.findAllById(accountIds).stream()
                    .map(accountMapper::toDto)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
        }
        return accountRepository.findAll(pageable).map(accountMapper::toDto);
    }

    @Transactional
    public AccountDto updateAccount(Long accountId, AccountDto accountDto) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountId", accountId));
        accountMapper.updateEntityFromDto(accountDto, account);
        Account saved = accountRepository.save(account);
        return accountMapper.toDto(saved);
    }
}

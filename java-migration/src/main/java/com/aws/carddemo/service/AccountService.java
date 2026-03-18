package com.aws.carddemo.service;

import com.aws.carddemo.dto.AccountDto;
import com.aws.carddemo.dto.CustomerDto;
import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.CardXref;
import com.aws.carddemo.entity.Customer;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CardXrefRepository cardXrefRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public AccountDto getAccount(String acctId) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + acctId));

        AccountDto dto = mapToDto(account);

        // Join with customer via card_xref
        cardXrefRepository.findFirstByXrefAcctId(acctId)
                .ifPresent(xref -> {
                    customerRepository.findById(xref.getXrefCustId())
                            .ifPresent(customer -> dto.setCustomer(mapCustomerToDto(customer)));
                });

        return dto;
    }

    @Transactional
    public AccountDto updateAccount(String acctId, AccountDto accountDto) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + acctId));

        if (accountDto.getAcctActiveStatus() != null) {
            account.setAcctActiveStatus(accountDto.getAcctActiveStatus());
        }
        if (accountDto.getAcctCreditLimit() != null) {
            account.setAcctCreditLimit(accountDto.getAcctCreditLimit());
        }
        if (accountDto.getAcctCashCreditLimit() != null) {
            account.setAcctCashCreditLimit(accountDto.getAcctCashCreditLimit());
        }
        if (accountDto.getAcctExpirationDate() != null) {
            account.setAcctExpirationDate(accountDto.getAcctExpirationDate());
        }
        if (accountDto.getAcctReissueDate() != null) {
            account.setAcctReissueDate(accountDto.getAcctReissueDate());
        }
        if (accountDto.getAcctGroupId() != null) {
            account.setAcctGroupId(accountDto.getAcctGroupId());
        }

        Account saved = accountRepository.save(account);
        return mapToDto(saved);
    }

    private AccountDto mapToDto(Account account) {
        return AccountDto.builder()
                .acctId(account.getAcctId())
                .acctActiveStatus(account.getAcctActiveStatus())
                .acctCurrBal(account.getAcctCurrBal())
                .acctCreditLimit(account.getAcctCreditLimit())
                .acctCashCreditLimit(account.getAcctCashCreditLimit())
                .acctOpenDate(account.getAcctOpenDate())
                .acctExpirationDate(account.getAcctExpirationDate())
                .acctReissueDate(account.getAcctReissueDate())
                .acctCurrCycCredit(account.getAcctCurrCycCredit())
                .acctCurrCycDebit(account.getAcctCurrCycDebit())
                .acctAddrZip(account.getAcctAddrZip())
                .acctGroupId(account.getAcctGroupId())
                .build();
    }

    private CustomerDto mapCustomerToDto(Customer customer) {
        return CustomerDto.builder()
                .custId(customer.getCustId())
                .custFirstName(customer.getCustFirstName())
                .custMiddleName(customer.getCustMiddleName())
                .custLastName(customer.getCustLastName())
                .custAddrLine1(customer.getCustAddrLine1())
                .custAddrLine2(customer.getCustAddrLine2())
                .custAddrLine3(customer.getCustAddrLine3())
                .custAddrStateCd(customer.getCustAddrStateCd())
                .custAddrCountryCd(customer.getCustAddrCountryCd())
                .custAddrZip(customer.getCustAddrZip())
                .custPhoneNum1(customer.getCustPhoneNum1())
                .custPhoneNum2(customer.getCustPhoneNum2())
                .custSsn(customer.getCustSsn())
                .custGovtIssuedId(customer.getCustGovtIssuedId())
                .custDobYyyyMmDd(customer.getCustDobYyyyMmDd())
                .custEftAccountId(customer.getCustEftAccountId())
                .custPriCardHolderInd(customer.getCustPriCardHolderInd())
                .custFicoCreditScore(customer.getCustFicoCreditScore())
                .build();
    }
}

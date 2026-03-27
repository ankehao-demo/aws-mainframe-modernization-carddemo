package com.cardemo.service;

import com.cardemo.dto.AccountViewResponse;
import com.cardemo.entity.Account;
import com.cardemo.entity.CardXref;
import com.cardemo.entity.Customer;
import com.cardemo.exception.ResourceNotFoundException;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.CustomerRepository;
import org.springframework.stereotype.Service;

/**
 * Account view service converting COACTVWC.cbl logic.
 *
 * Original COBOL flow (3-file join):
 *   1. READ CXACAIX by account ID to get customer ID and card number
 *   2. READ ACCTDAT by account ID to get account details
 *   3. READ CUSTDAT by customer ID to get customer details
 *
 * SSN formatting (COACTVWC.cbl lines 496-504):
 *   STRING SSN-PART1 DELIMITED BY SIZE '-' SSN-PART2 '-' SSN-PART3
 *   -> XXX-XX-XXXX
 */
@Service
public class AccountService {

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(CardXrefRepository cardXrefRepository,
                          AccountRepository accountRepository,
                          CustomerRepository customerRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public AccountViewResponse getAccountView(long accountId) {
        CardXref xref = cardXrefRepository.findByAcctId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account ID not found in cross-reference: " + accountId));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account ID not found in account master: " + accountId));

        Customer customer = customerRepository.findById(xref.getCustId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer ID not found in customer master: " + xref.getCustId()));

        return buildResponse(account, customer);
    }

    private AccountViewResponse buildResponse(Account account, Customer customer) {
        AccountViewResponse response = new AccountViewResponse();

        // Account fields
        response.setAccountId(account.getAcctId());
        response.setActiveStatus(account.getActiveStatus());
        response.setCurrentBalance(account.getCurrentBalance());
        response.setCreditLimit(account.getCreditLimit());
        response.setCashCreditLimit(account.getCashCreditLimit());
        response.setCurrentCycleCredit(account.getCurrentCycleCredit());
        response.setCurrentCycleDebit(account.getCurrentCycleDebit());
        response.setOpenDate(account.getOpenDate());
        response.setExpirationDate(account.getExpirationDate());
        response.setReissueDate(account.getReissueDate());
        response.setGroupId(account.getGroupId());

        // Customer fields
        response.setCustomerId(customer.getCustId());
        response.setSsn(formatSsn(customer.getSsn()));
        response.setFicoCreditScore(customer.getFicoCreditScore());
        response.setDateOfBirth(customer.getDateOfBirth());
        response.setFirstName(customer.getFirstName());
        response.setMiddleName(customer.getMiddleName());
        response.setLastName(customer.getLastName());
        response.setAddressLine1(customer.getAddrLine1());
        response.setAddressLine2(customer.getAddrLine2());
        response.setCity(customer.getAddrLine3());
        response.setStateCode(customer.getAddrStateCd());
        response.setZipCode(customer.getAddrZip());
        response.setCountryCode(customer.getAddrCountryCd());
        response.setPhone1(customer.getPhoneNum1());
        response.setPhone2(customer.getPhoneNum2());
        response.setGovtIssuedId(customer.getGovtIssuedId());
        response.setEftAccountId(customer.getEftAccountId());
        response.setPrimaryCardHolderInd(customer.getPrimaryCardHolderInd());

        return response;
    }

    /**
     * Formats SSN as XXX-XX-XXXX per COACTVWC.cbl lines 496-504.
     */
    private String formatSsn(Long ssn) {
        if (ssn == null) {
            return null;
        }
        String ssnStr = String.format("%09d", ssn);
        return ssnStr.substring(0, 3) + "-" + ssnStr.substring(3, 5) + "-" + ssnStr.substring(5, 9);
    }
}

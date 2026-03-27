package com.cardemo.controller;

import com.cardemo.dto.AccountViewResponse;
import com.cardemo.exception.CardDemoException;
import com.cardemo.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account controller converting COACTVWC.cbl account view logic.
 *
 * Original COBOL program accepts an account ID and performs a 3-file join:
 *   1. Reads CXACAIX by account ID -> customer ID and card number
 *   2. Reads ACCTDAT by account ID -> account details
 *   3. Reads CUSTDAT by customer ID -> customer details
 *
 * Validation (COACTVWC.cbl lines 649-681):
 *   - Account ID must be a non-zero 11-digit number
 *   - Returns 400 if not numeric or zero, 404 if not found
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountViewResponse> getAccountView(
            @PathVariable String accountId) {
        long acctId = validateAccountId(accountId);
        AccountViewResponse response = accountService.getAccountView(acctId);
        return ResponseEntity.ok(response);
    }

    private long validateAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new CardDemoException("Account ID is required");
        }
        if (!accountId.matches("\\d{11}")) {
            throw new CardDemoException(
                    "Account ID must be an 11-digit number");
        }
        long acctId = Long.parseLong(accountId);
        if (acctId == 0) {
            throw new CardDemoException("Account ID cannot be zero");
        }
        return acctId;
    }
}

package com.carddemo.account.controller;

import com.carddemo.account.service.AccountService;
import com.carddemo.common.dto.AccountDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ports COBOL programs:
 * - COACTVWC.cbl (CAVW transaction) → getAccount, listAccounts
 * - COACTUPC.cbl (CAUP transaction) → updateAccount
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "Account operations — replaces COACTVWC/COACTUPC")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountId}")
    @Operation(summary = "View account", description = "Get account details by ID")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }

    @GetMapping
    @Operation(summary = "List accounts", description = "List accounts with optional customer filter")
    public ResponseEntity<Page<AccountDto>> listAccounts(
            @RequestParam(required = false) Long customerId,
            Pageable pageable) {
        return ResponseEntity.ok(accountService.listAccounts(customerId, pageable));
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "Update account", description = "Update account information")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long accountId,
                                                    @RequestBody AccountDto accountDto) {
        return ResponseEntity.ok(accountService.updateAccount(accountId, accountDto));
    }
}

package com.carddemo.account.controller;

import com.carddemo.account.entity.Account;
import com.carddemo.account.entity.Customer;
import com.carddemo.account.service.AccountService;
import com.carddemo.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account Management", description = "Account view/update - replaces COACTVWC/COACTUPC.cbl")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "View account")
    public ResponseEntity<ApiResponse<Account>> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAccount(accountId)));
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "Update account")
    public ResponseEntity<ApiResponse<Account>> updateAccount(@PathVariable Long accountId, @RequestBody Account account) {
        return ResponseEntity.ok(ApiResponse.ok("Account updated", accountService.updateAccount(accountId, account)));
    }

    @GetMapping("/{accountId}/customers")
    @Operation(summary = "Get customers for account")
    public ResponseEntity<ApiResponse<List<Customer>>> getCustomers(@PathVariable Long accountId) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getCustomersForAccount(accountId)));
    }
}

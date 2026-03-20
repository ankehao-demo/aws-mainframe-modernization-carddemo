package com.aws.carddemo.controller;

import com.aws.carddemo.dto.AccountUpdateRequest;
import com.aws.carddemo.entity.Account;
import com.aws.carddemo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable("id") String acctId) {
        return ResponseEntity.ok(accountService.getAccount(acctId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable("id") String acctId,
                                                  @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(acctId, request));
    }
}

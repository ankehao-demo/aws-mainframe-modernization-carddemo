package com.aws.carddemo.controller;

import com.aws.carddemo.dto.AccountDto;
import com.aws.carddemo.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{acctId}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String acctId) {
        return ResponseEntity.ok(accountService.getAccount(acctId));
    }

    @PutMapping("/{acctId}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable String acctId,
                                                     @RequestBody AccountDto accountDto) {
        return ResponseEntity.ok(accountService.updateAccount(acctId, accountDto));
    }
}

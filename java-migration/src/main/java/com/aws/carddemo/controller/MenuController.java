package com.aws.carddemo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMenu(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", authentication.getName());
        response.put("role", isAdmin ? "ADMIN" : "USER");

        List<Map<String, String>> menuOptions = new ArrayList<>();

        if (isAdmin) {
            menuOptions.add(createMenuItem("1", "User List", "/api/admin/users"));
            menuOptions.add(createMenuItem("2", "User Add", "/api/admin/users"));
            menuOptions.add(createMenuItem("3", "User Update", "/api/admin/users/{userId}"));
            menuOptions.add(createMenuItem("4", "User Delete", "/api/admin/users/{userId}"));
            menuOptions.add(createMenuItem("5", "Transaction Type List", "/api/admin/transaction-types"));
            menuOptions.add(createMenuItem("6", "Transaction Type Maintenance", "/api/admin/transaction-types/{typeCode}"));
        } else {
            menuOptions.add(createMenuItem("1", "Account View", "/api/accounts/{acctId}"));
            menuOptions.add(createMenuItem("2", "Card List", "/api/cards"));
            menuOptions.add(createMenuItem("3", "Transaction List", "/api/transactions"));
            menuOptions.add(createMenuItem("4", "Bill Payment", "/api/bill-payment"));
            menuOptions.add(createMenuItem("5", "Reports", "/api/reports"));
        }

        response.put("menuOptions", menuOptions);
        return ResponseEntity.ok(response);
    }

    private Map<String, String> createMenuItem(String option, String label, String endpoint) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("option", option);
        item.put("label", label);
        item.put("endpoint", endpoint);
        return item;
    }
}

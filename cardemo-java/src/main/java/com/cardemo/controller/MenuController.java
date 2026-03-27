package com.cardemo.controller;

import com.cardemo.dto.MenuResponse;
import com.cardemo.dto.MenuResponse.MenuItem;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Menu controller converting COMEN01C.cbl (regular user menu) and
 * COADM01C.cbl (admin menu) logic.
 *
 * Regular user menu options from COMEN02Y.cpy:
 *   1. Account View        (COACTVWC)
 *   2. Account Update      (COACTUPC)
 *   3. Credit Card List    (COCRDLIC)
 *   4. Credit Card View    (COCRDSLC)
 *   5. Credit Card Update  (COCRDUPC)
 *   6. Transaction List    (COTRN00C)
 *   7. Transaction View    (COTRN01C)
 *   8. Transaction Add     (COTRN02C)
 *   9. Transaction Reports (CORPT00C)
 *  10. Bill Payment        (COBIL00C)
 *  11. Pending Auth View   (COPAUS0C)
 *
 * Admin menu options from COADM02Y.cpy:
 *   1. User List (Security)                    (COUSR00C)
 *   2. User Add (Security)                     (COUSR01C)
 *   3. User Update (Security)                  (COUSR02C)
 *   4. User Delete (Security)                  (COUSR03C)
 *   5. Transaction Type List/Update (Db2)      (COTRTLIC)
 *   6. Transaction Type Maintenance (Db2)      (COTRTUPC)
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @GetMapping
    public ResponseEntity<MenuResponse> getMenu(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (isAdmin) {
            return ResponseEntity.ok(buildAdminMenu());
        }
        return ResponseEntity.ok(buildUserMenu());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuResponse> getAdminMenu() {
        return ResponseEntity.ok(buildAdminMenu());
    }

    private MenuResponse buildUserMenu() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(1, "Account View", "/api/accounts/{id}", "COACTVWC"));
        items.add(new MenuItem(2, "Account Update", "/api/accounts/{id}", "COACTUPC"));
        items.add(new MenuItem(3, "Credit Card List", "/api/cards", "COCRDLIC"));
        items.add(new MenuItem(4, "Credit Card View", "/api/cards/{num}", "COCRDSLC"));
        items.add(new MenuItem(5, "Credit Card Update", "/api/cards/{num}", "COCRDUPC"));
        items.add(new MenuItem(6, "Transaction List", "/api/transactions", "COTRN00C"));
        items.add(new MenuItem(7, "Transaction View", "/api/transactions/{id}", "COTRN01C"));
        items.add(new MenuItem(8, "Transaction Add", "/api/transactions", "COTRN02C"));
        items.add(new MenuItem(9, "Transaction Reports", "/api/reports", "CORPT00C"));
        items.add(new MenuItem(10, "Bill Payment", "/api/payments", "COBIL00C"));
        items.add(new MenuItem(11, "Pending Authorization View", "/api/authorizations", "COPAUS0C"));
        return new MenuResponse("USER", items);
    }

    private MenuResponse buildAdminMenu() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(1, "User List (Security)", "/api/users", "COUSR00C"));
        items.add(new MenuItem(2, "User Add (Security)", "/api/users", "COUSR01C"));
        items.add(new MenuItem(3, "User Update (Security)", "/api/users/{id}", "COUSR02C"));
        items.add(new MenuItem(4, "User Delete (Security)", "/api/users/{id}", "COUSR03C"));
        items.add(new MenuItem(5, "Transaction Type List/Update (Db2)", "/api/transaction-types", "COTRTLIC"));
        items.add(new MenuItem(6, "Transaction Type Maintenance (Db2)", "/api/transaction-types", "COTRTUPC"));
        return new MenuResponse("ADMIN", items);
    }
}

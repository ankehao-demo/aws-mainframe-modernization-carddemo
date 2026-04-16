package com.carddemo.admin.controller;

import com.carddemo.admin.entity.User;
import com.carddemo.admin.service.UserAdminService;
import com.carddemo.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "User Administration", description = "Admin CRUD - replaces COUSR00C-03C.cbl")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    @Operation(summary = "List users (paginated)")
    public ResponseEntity<ApiResponse<Page<User>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(userAdminService.listUsers(PageRequest.of(page, size))));
    }

    @PostMapping
    @Operation(summary = "Add user")
    public ResponseEntity<ApiResponse<User>> addUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User created", userAdminService.createUser(user)));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable String userId, @RequestBody User user) {
        return ResponseEntity.ok(ApiResponse.ok("User updated", userAdminService.updateUser(userId, user)));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        userAdminService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }
}

package com.cardemo.controller;

import com.cardemo.dto.CreateUserRequest;
import com.cardemo.dto.UpdateUserRequest;
import com.cardemo.dto.UserListResponse;
import com.cardemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User management controller converting COUSR00C-03C.cbl logic.
 *
 * COUSR00C: GET  /api/users          - List users with pagination (VSAM browse)
 * COUSR01C: POST /api/users          - Add new user
 * COUSR02C: PUT  /api/users/{userId} - Update existing user
 * COUSR03C: DELETE /api/users/{userId} - Delete user
 *
 * All endpoints restricted to ADMIN role via @PreAuthorize.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserListResponse> listUsers(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserListResponse.UserItem> getUser(
            @PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PostMapping
    public ResponseEntity<UserListResponse.UserItem> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserListResponse.UserItem created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserListResponse.UserItem> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}

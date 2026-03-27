package com.cardemo.service;

import com.cardemo.dto.CreateUserRequest;
import com.cardemo.dto.UpdateUserRequest;
import com.cardemo.dto.UserListResponse;
import com.cardemo.entity.UserSecurity;
import com.cardemo.enums.UserType;
import com.cardemo.exception.CardDemoException;
import com.cardemo.repository.UserSecurityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User management service converting COUSR00C-03C.cbl logic.
 *
 * COUSR00C: List users (VSAM STARTBR/READNEXT/ENDBR -> JPA Pageable)
 * COUSR01C: Add user (CICS WRITE -> JPA save, checks DUPKEY/DUPREC)
 * COUSR02C: Update user (CICS READ for UPDATE, REWRITE -> JPA findById + save)
 * COUSR03C: Delete user (CICS DELETE -> JPA deleteById)
 */
@Service
public class UserService {

    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserSecurityRepository userSecurityRepository,
                       PasswordEncoder passwordEncoder) {
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserListResponse listUsers(Pageable pageable) {
        Page<UserSecurity> page = userSecurityRepository.findAll(pageable);

        var userItems = page.getContent().stream()
                .map(u -> new UserListResponse.UserItem(
                        u.getUserId(),
                        u.getFirstName(),
                        u.getLastName(),
                        UserType.fromCode(u.getUserType())
                ))
                .toList();

        return new UserListResponse(
                userItems,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public UserListResponse.UserItem getUser(String userId) {
        UserSecurity user = userSecurityRepository.findById(userId.toUpperCase())
                .orElseThrow(() -> new CardDemoException(
                        "User not found: " + userId));

        return new UserListResponse.UserItem(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                UserType.fromCode(user.getUserType())
        );
    }

    @Transactional
    public UserListResponse.UserItem createUser(CreateUserRequest request) {
        String userId = request.getUserId().toUpperCase();

        if (userSecurityRepository.existsById(userId)) {
            throw new CardDemoException("User ID already exists: " + userId);
        }

        UserSecurity user = new UserSecurity();
        user.setUserId(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(request.getUserType().toUpperCase());

        userSecurityRepository.save(user);

        return new UserListResponse.UserItem(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                UserType.fromCode(user.getUserType())
        );
    }

    @Transactional
    public UserListResponse.UserItem updateUser(String userId, UpdateUserRequest request) {
        UserSecurity user = userSecurityRepository.findById(userId.toUpperCase())
                .orElseThrow(() -> new CardDemoException(
                        "User not found: " + userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType().toUpperCase());
        }

        userSecurityRepository.save(user);

        return new UserListResponse.UserItem(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                UserType.fromCode(user.getUserType())
        );
    }

    @Transactional
    public void deleteUser(String userId) {
        if (!userSecurityRepository.existsById(userId.toUpperCase())) {
            throw new CardDemoException("User not found: " + userId);
        }
        userSecurityRepository.deleteById(userId.toUpperCase());
    }
}

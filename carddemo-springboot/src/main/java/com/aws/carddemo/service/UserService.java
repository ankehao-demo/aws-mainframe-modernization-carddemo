package com.aws.carddemo.service;

import com.aws.carddemo.dto.UserCreateRequest;
import com.aws.carddemo.dto.UserUpdateRequest;
import com.aws.carddemo.entity.UserSecurity;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.UserSecurityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User management — mirrors COUSR00C.cbl (list), COUSR01C.cbl (add),
 * COUSR02C.cbl (update), COUSR03C.cbl (delete).
 * Full CRUD on user_security table, restricted to admin role.
 */
@Service
public class UserService {

    private final UserSecurityRepository userSecurityRepository;

    public UserService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    public Page<UserSecurity> listUsers(Pageable pageable) {
        return userSecurityRepository.findAll(pageable);
    }

    public UserSecurity getUser(String userId) {
        return userSecurityRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @Transactional
    public UserSecurity createUser(UserCreateRequest request) {
        String userId = request.userId().toUpperCase().trim();
        if (userSecurityRepository.existsById(userId)) {
            throw new IllegalArgumentException("User already exists: " + userId);
        }

        UserSecurity user = new UserSecurity();
        user.setUserId(userId);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPassword(request.password());
        user.setUserType(request.userType().toUpperCase());

        return userSecurityRepository.save(user);
    }

    @Transactional
    public UserSecurity updateUser(String userId, UserUpdateRequest request) {
        UserSecurity user = userSecurityRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.password() != null) user.setPassword(request.password());
        if (request.userType() != null) user.setUserType(request.userType().toUpperCase());

        return userSecurityRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        if (!userSecurityRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        userSecurityRepository.deleteById(userId);
    }
}

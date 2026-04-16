package com.carddemo.admin.service;

import com.carddemo.admin.entity.User;
import com.carddemo.admin.repository.UserRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserAdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUser(String userId) {
        return userRepository.findById(userId.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
    }

    public User createUser(User user) {
        if (userRepository.existsById(user.getUserId().toUpperCase())) {
            throw new BadRequestException("User already exists: " + user.getUserId());
        }
        user.setUserId(user.getUserId().toUpperCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(String userId, User updated) {
        User existing = getUser(userId);
        if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
        if (updated.getUserType() != null) existing.setUserType(updated.getUserType());
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
        return userRepository.save(existing);
    }

    public void deleteUser(String userId) {
        User user = getUser(userId);
        userRepository.delete(user);
    }
}

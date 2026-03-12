package com.carddemo.auth.service;

import com.carddemo.common.dto.CreateUserRequest;
import com.carddemo.common.dto.UpdateUserRequest;
import com.carddemo.common.dto.UserDto;
import com.carddemo.common.entity.User;
import com.carddemo.common.exception.BusinessException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.mapper.UserMapper;
import com.carddemo.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ports COBOL programs COUSR00C (list), COUSR01C (add), COUSR02C (update), COUSR03C (delete).
 * Replaces CICS VSAM file operations on USRSEC with JPA repository operations.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public Page<UserDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        String userId = request.getUserId().toUpperCase();

        if (userRepository.existsById(userId)) {
            throw new BusinessException("User ID already exists: " + userId);
        }

        User user = userMapper.toEntity(request);
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(request.getPassword().toUpperCase()));

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Transactional
    public UserDto updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        userMapper.updateEntityFromRequest(request, user);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword().toUpperCase()));
        }

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId.toUpperCase())) {
            throw new ResourceNotFoundException("User", "userId", userId);
        }
        userRepository.deleteById(userId.toUpperCase());
    }
}

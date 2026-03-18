package com.aws.carddemo.service;

import com.aws.carddemo.dto.UserDto;
import com.aws.carddemo.entity.UserSecurity;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.exception.ValidationException;
import com.aws.carddemo.repository.UserSecurityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAdminService {

    private final UserSecurityRepository userSecurityRepository;

    public UserAdminService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userSecurityRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUser(String userId) {
        UserSecurity user = userSecurityRepository.findById(userId.toUpperCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return mapToDto(user);
    }

    @Transactional
    public UserDto addUser(UserDto userDto) {
        String userId = userDto.getUsrId().toUpperCase().trim();
        if (userSecurityRepository.existsById(userId)) {
            throw new ValidationException("User already exists: " + userId);
        }

        UserSecurity user = UserSecurity.builder()
                .usrId(userId)
                .usrFname(userDto.getUsrFname())
                .usrLname(userDto.getUsrLname())
                .usrPwd(userDto.getUsrPwd().toUpperCase())
                .usrType(userDto.getUsrType().toUpperCase())
                .build();

        UserSecurity saved = userSecurityRepository.save(user);
        return mapToDto(saved);
    }

    @Transactional
    public UserDto updateUser(String userId, UserDto userDto) {
        String normalizedId = userId.toUpperCase().trim();
        UserSecurity user = userSecurityRepository.findById(normalizedId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (userDto.getUsrFname() != null) {
            user.setUsrFname(userDto.getUsrFname());
        }
        if (userDto.getUsrLname() != null) {
            user.setUsrLname(userDto.getUsrLname());
        }
        if (userDto.getUsrPwd() != null) {
            user.setUsrPwd(userDto.getUsrPwd().toUpperCase());
        }
        if (userDto.getUsrType() != null) {
            user.setUsrType(userDto.getUsrType().toUpperCase());
        }

        UserSecurity saved = userSecurityRepository.save(user);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteUser(String userId) {
        String normalizedId = userId.toUpperCase().trim();
        if (!userSecurityRepository.existsById(normalizedId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        userSecurityRepository.deleteById(normalizedId);
    }

    private UserDto mapToDto(UserSecurity user) {
        return UserDto.builder()
                .usrId(user.getUsrId())
                .usrFname(user.getUsrFname())
                .usrLname(user.getUsrLname())
                .usrType(user.getUsrType())
                .build();
    }
}

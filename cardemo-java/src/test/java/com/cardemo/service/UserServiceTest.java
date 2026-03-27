package com.cardemo.service;

import com.cardemo.dto.CreateUserRequest;
import com.cardemo.dto.UpdateUserRequest;
import com.cardemo.dto.UserListResponse;
import com.cardemo.entity.UserSecurity;
import com.cardemo.enums.UserType;
import com.cardemo.exception.CardDemoException;
import com.cardemo.repository.UserSecurityRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserSecurity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserSecurity();
        testUser.setUserId("USER0001");
        testUser.setFirstName("Regular");
        testUser.setLastName("User");
        testUser.setPassword("$2a$10$hashed");
        testUser.setUserType("U");
    }

    @Test
    void listUsers_returnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userSecurityRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(testUser), pageable, 1));

        UserListResponse response = userService.listUsers(pageable);

        assertNotNull(response);
        assertEquals(1, response.getUsers().size());
        assertEquals("USER0001", response.getUsers().get(0).getUserId());
        assertEquals(UserType.USER, response.getUsers().get(0).getUserType());
    }

    @Test
    void getUser_existing_returnsUser() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(testUser));

        UserListResponse.UserItem item = userService.getUser("USER0001");

        assertEquals("USER0001", item.getUserId());
        assertEquals("Regular", item.getFirstName());
    }

    @Test
    void getUser_notFound_throwsException() {
        when(userSecurityRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(CardDemoException.class, () -> userService.getUser("INVALID"));
    }

    @Test
    void createUser_success() {
        when(userSecurityRepository.existsById("NEWUSER1")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
        when(userSecurityRepository.save(any(UserSecurity.class))).thenAnswer(i -> i.getArgument(0));

        CreateUserRequest request = new CreateUserRequest(
                "NEWUSER1", "PASSWORD", "New", "User", "U");
        UserListResponse.UserItem result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("NEWUSER1", result.getUserId());
        assertEquals(UserType.USER, result.getUserType());
    }

    @Test
    void createUser_duplicate_throwsException() {
        when(userSecurityRepository.existsById("USER0001")).thenReturn(true);

        CreateUserRequest request = new CreateUserRequest(
                "USER0001", "PASSWORD", "Dup", "User", "U");

        CardDemoException ex = assertThrows(CardDemoException.class,
                () -> userService.createUser(request));
        assertEquals("User ID already exists: USER0001", ex.getMessage());
    }

    @Test
    void updateUser_success() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        when(userSecurityRepository.save(any(UserSecurity.class))).thenAnswer(i -> i.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest(null, "Updated", "Name", null);
        UserListResponse.UserItem result = userService.updateUser("USER0001", request);

        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
    }

    @Test
    void updateUser_notFound_throwsException() {
        when(userSecurityRepository.findById("INVALID")).thenReturn(Optional.empty());

        UpdateUserRequest request = new UpdateUserRequest(null, "Test", null, null);
        assertThrows(CardDemoException.class, () -> userService.updateUser("INVALID", request));
    }

    @Test
    void deleteUser_success() {
        when(userSecurityRepository.existsById("USER0001")).thenReturn(true);

        userService.deleteUser("USER0001");

        verify(userSecurityRepository).deleteById("USER0001");
    }

    @Test
    void deleteUser_notFound_throwsException() {
        when(userSecurityRepository.existsById("INVALID")).thenReturn(false);

        assertThrows(CardDemoException.class, () -> userService.deleteUser("INVALID"));
    }
}

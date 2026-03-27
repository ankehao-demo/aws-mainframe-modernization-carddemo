package com.cardemo.service;

import com.cardemo.config.JwtTokenProvider;
import com.cardemo.dto.LoginRequest;
import com.cardemo.dto.LoginResponse;
import com.cardemo.entity.UserSecurity;
import com.cardemo.enums.UserType;
import com.cardemo.exception.CardDemoException;
import com.cardemo.repository.UserSecurityRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private UserSecurity adminUser;
    private UserSecurity regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new UserSecurity();
        adminUser.setUserId("ADMIN001");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword("$2a$10$hashedpassword");
        adminUser.setUserType("A");

        regularUser = new UserSecurity();
        regularUser.setUserId("USER0001");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setPassword("$2a$10$hashedpassword");
        regularUser.setUserType("U");
    }

    @Test
    void login_adminUser_returnsAdminToken() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("PASSWORD", adminUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken("ADMIN001", "A")).thenReturn("test-jwt-token");

        LoginResponse response = authService.login(new LoginRequest("ADMIN001", "PASSWORD"));

        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("ADMIN001", response.getUserId());
        assertEquals(UserType.ADMIN, response.getUserType());
    }

    @Test
    void login_regularUser_returnsUserToken() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.matches("PASSWORD", regularUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken("USER0001", "U")).thenReturn("test-jwt-token");

        LoginResponse response = authService.login(new LoginRequest("USER0001", "PASSWORD"));

        assertNotNull(response);
        assertEquals(UserType.USER, response.getUserType());
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userSecurityRepository.findById("INVALID")).thenReturn(Optional.empty());

        CardDemoException ex = assertThrows(CardDemoException.class,
                () -> authService.login(new LoginRequest("INVALID", "PASSWORD")));

        assertEquals("User not found. Try again ...", ex.getMessage());
    }

    @Test
    void login_wrongPassword_throwsException() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches(anyString(), eq(adminUser.getPassword()))).thenReturn(false);

        CardDemoException ex = assertThrows(CardDemoException.class,
                () -> authService.login(new LoginRequest("ADMIN001", "WRONG")));

        assertEquals("Wrong Password. Try again ...", ex.getMessage());
    }

    @Test
    void login_caseInsensitiveUserId() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("PASSWORD", adminUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken("ADMIN001", "A")).thenReturn("test-jwt-token");

        LoginResponse response = authService.login(new LoginRequest("admin001", "PASSWORD"));

        assertEquals("ADMIN001", response.getUserId());
    }
}

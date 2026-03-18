package com.aws.carddemo.service;

import com.aws.carddemo.dto.LoginRequest;
import com.aws.carddemo.dto.LoginResponse;
import com.aws.carddemo.entity.UserSecurity;
import com.aws.carddemo.exception.AuthenticationException;
import com.aws.carddemo.repository.UserSecurityRepository;
import com.aws.carddemo.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private UserSecurity adminUser;
    private UserSecurity regularUser;

    @BeforeEach
    void setUp() {
        adminUser = UserSecurity.builder()
                .usrId("ADMIN001")
                .usrFname("ADMIN")
                .usrLname("USER")
                .usrPwd("PASSWORD")
                .usrType("A")
                .build();

        regularUser = UserSecurity.builder()
                .usrId("USER0001")
                .usrFname("REGULAR")
                .usrLname("USER")
                .usrPwd("PASSWORD")
                .usrType("U")
                .build();
    }

    @Test
    void login_validAdmin_returnsAdminToken() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));
        when(jwtTokenProvider.generateToken("ADMIN001", "A")).thenReturn("test-admin-token");

        LoginRequest request = LoginRequest.builder().userId("admin001").password("password").build();
        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("test-admin-token");
        assertThat(response.getUserType()).isEqualTo("ADMIN");
        assertThat(response.getMessage()).isEqualTo("Login successful");
    }

    @Test
    void login_validUser_returnsUserToken() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(regularUser));
        when(jwtTokenProvider.generateToken("USER0001", "U")).thenReturn("test-user-token");

        LoginRequest request = LoginRequest.builder().userId("USER0001").password("PASSWORD").build();
        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("test-user-token");
        assertThat(response.getUserType()).isEqualTo("USER");
    }

    @Test
    void login_wrongPassword_throwsException() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));

        LoginRequest request = LoginRequest.builder().userId("ADMIN001").password("WRONGPWD").build();

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Wrong Password. Try again ...");
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userSecurityRepository.findById("UNKNOWN1")).thenReturn(Optional.empty());

        LoginRequest request = LoginRequest.builder().userId("UNKNOWN1").password("PASSWORD").build();

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User not found. Try again ...");
    }

    @Test
    void login_caseInsensitive_uppercasesInput() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(regularUser));
        when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("token");

        LoginRequest request = LoginRequest.builder().userId("user0001").password("password").build();
        LoginResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("token");
    }
}

package com.carddemo.service;

import com.carddemo.config.JwtTokenProvider;
import com.carddemo.model.User;
import com.carddemo.repository.UserRepository;
import com.carddemo.service.AuthService.AuthResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfully() {
        User user = new User("USER0001", "John", "Doe", "PASSWORD", "U");
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("USER0001", "U")).thenReturn("test-token");

        AuthResult result = authService.login("user0001", "password");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getToken()).isEqualTo("test-token");
        assertThat(result.getUserType()).isEqualTo("U");
        assertThat(result.getUserId()).isEqualTo("USER0001");
    }

    @Test
    void shouldLoginAdminSuccessfully() {
        User user = new User("ADMIN001", "Admin", "User", "PASSWORD", "A");
        when(userRepository.findById("ADMIN001")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("ADMIN001", "A")).thenReturn("admin-token");

        AuthResult result = authService.login("admin001", "password");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getUserType()).isEqualTo("A");
    }

    @Test
    void shouldFailWithWrongPassword() {
        User user = new User("USER0001", "John", "Doe", "PASSWORD", "U");
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(user));

        AuthResult result = authService.login("user0001", "wrongpass");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Wrong Password. Try again ...");
    }

    @Test
    void shouldFailWithUserNotFound() {
        when(userRepository.findById("NOUSER01")).thenReturn(Optional.empty());

        AuthResult result = authService.login("nouser01", "password");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("User not found. Try again ...");
    }

    @Test
    void shouldFailWithEmptyUserId() {
        AuthResult result = authService.login("", "password");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Please enter User ID ...");
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    void shouldFailWithNullUserId() {
        AuthResult result = authService.login(null, "password");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Please enter User ID ...");
    }

    @Test
    void shouldFailWithEmptyPassword() {
        AuthResult result = authService.login("user0001", "");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Please enter Password ...");
    }

    @Test
    void shouldFailWithNullPassword() {
        AuthResult result = authService.login("user0001", null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Please enter Password ...");
    }

    @Test
    void shouldUppercaseUserId() {
        User user = new User("USER0001", "John", "Doe", "PASSWORD", "U");
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("USER0001", "U")).thenReturn("token");

        authService.login("User0001", "PASSWORD");

        verify(userRepository).findById("USER0001");
    }
}

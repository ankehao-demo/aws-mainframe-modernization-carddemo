package com.aws.carddemo.controller;

import com.aws.carddemo.dto.LoginRequest;
import com.aws.carddemo.dto.LoginResponse;
import com.aws.carddemo.exception.AuthenticationException;
import com.aws.carddemo.config.SecurityConfig;
import com.aws.carddemo.security.CardDemoUserDetailsService;
import com.aws.carddemo.security.JwtAuthenticationFilter;
import com.aws.carddemo.security.JwtTokenProvider;
import com.aws.carddemo.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CardDemoUserDetailsService userDetailsService;

    @Test
    void login_validCredentials_returns200() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .token("test-token")
                .userType("ADMIN")
                .message("Login successful")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder().userId("ADMIN001").password("PASSWORD").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.userType").value("ADMIN"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Wrong Password. Try again ..."));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder().userId("ADMIN001").password("WRONG").build())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Wrong Password. Try again ..."));
    }

    @Test
    void login_userNotFound_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("User not found. Try again ..."));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder().userId("UNKNOWN1").password("PASSWORD").build())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found. Try again ..."));
    }
}

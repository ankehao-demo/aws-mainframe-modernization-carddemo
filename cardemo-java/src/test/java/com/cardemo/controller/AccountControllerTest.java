package com.cardemo.controller;

import com.cardemo.config.JwtAuthenticationFilter;
import com.cardemo.config.JwtTokenProvider;
import com.cardemo.config.SecurityConfig;
import com.cardemo.dto.AccountViewResponse;
import com.cardemo.exception.CardDemoException;
import com.cardemo.exception.ResourceNotFoundException;
import com.cardemo.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "USER")
    void getAccountView_validId_returns200() throws Exception {
        AccountViewResponse response = new AccountViewResponse();
        response.setAccountId(10000000001L);
        response.setActiveStatus("Y");
        response.setCurrentBalance(new BigDecimal("1500.00"));
        response.setCreditLimit(new BigDecimal("5000.00"));
        response.setCustomerId(1000000001L);
        response.setSsn("123-45-6789");
        response.setFirstName("John");
        response.setLastName("Smith");
        response.setFicoCreditScore(750);

        when(accountService.getAccountView(10000000001L)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/10000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(10000000001L))
                .andExpect(jsonPath("$.activeStatus").value("Y"))
                .andExpect(jsonPath("$.currentBalance").value(1500.00))
                .andExpect(jsonPath("$.customerId").value(1000000001L))
                .andExpect(jsonPath("$.ssn").value("123-45-6789"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.ficoCreditScore").value(750));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAccountView_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/api/accounts/ABCDEFGHIJK"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account ID must be an 11-digit number"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAccountView_zeroId_returns400() throws Exception {
        mockMvc.perform(get("/api/accounts/00000000000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account ID cannot be zero"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAccountView_wrongDigitCount_returns400() throws Exception {
        mockMvc.perform(get("/api/accounts/12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account ID must be an 11-digit number"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAccountView_notFound_returns404() throws Exception {
        when(accountService.getAccountView(99999999999L))
                .thenThrow(new ResourceNotFoundException(
                        "Account ID not found in cross-reference: 99999999999"));

        mockMvc.perform(get("/api/accounts/99999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getAccountView_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/accounts/10000000001"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAccountView_adminRole_returns200() throws Exception {
        AccountViewResponse response = new AccountViewResponse();
        response.setAccountId(10000000001L);
        when(accountService.getAccountView(10000000001L)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/10000000001"))
                .andExpect(status().isOk());
    }
}

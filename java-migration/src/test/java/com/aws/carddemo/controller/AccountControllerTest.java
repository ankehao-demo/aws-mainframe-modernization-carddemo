package com.aws.carddemo.controller;

import com.aws.carddemo.dto.AccountDto;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.security.CardDemoUserDetailsService;
import com.aws.carddemo.security.JwtTokenProvider;
import com.aws.carddemo.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CardDemoUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void getAccount_found_returns200() throws Exception {
        AccountDto dto = AccountDto.builder()
                .acctId("00000000001")
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("1500.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .build();

        when(accountService.getAccount("00000000001")).thenReturn(dto);

        mockMvc.perform(get("/api/accounts/00000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctId").value("00000000001"))
                .andExpect(jsonPath("$.acctCurrBal").value(1500.00));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAccount_notFound_returns404() throws Exception {
        when(accountService.getAccount("99999999999"))
                .thenThrow(new ResourceNotFoundException("Account not found: 99999999999"));

        mockMvc.perform(get("/api/accounts/99999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateAccount_returns200() throws Exception {
        AccountDto updateDto = AccountDto.builder()
                .acctActiveStatus("N")
                .build();

        AccountDto result = AccountDto.builder()
                .acctId("00000000001")
                .acctActiveStatus("N")
                .acctCurrBal(new BigDecimal("1500.00"))
                .build();

        when(accountService.updateAccount(eq("00000000001"), any(AccountDto.class))).thenReturn(result);

        mockMvc.perform(put("/api/accounts/00000000001")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctActiveStatus").value("N"));
    }
}

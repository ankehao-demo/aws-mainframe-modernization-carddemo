package com.aws.carddemo.controller;

import com.aws.carddemo.dto.PagedResponse;
import com.aws.carddemo.dto.TransactionDto;
import com.aws.carddemo.security.CardDemoUserDetailsService;
import com.aws.carddemo.security.JwtTokenProvider;
import com.aws.carddemo.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CardDemoUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void listTransactions_returnsPaginated() throws Exception {
        TransactionDto tran = TransactionDto.builder()
                .tranId("0000000000000001")
                .tranDesc("TEST PURCHASE")
                .tranAmt(new BigDecimal("100.00"))
                .build();

        PagedResponse<TransactionDto> response = PagedResponse.<TransactionDto>builder()
                .content(List.of(tran))
                .page(0).size(10).totalElements(1).totalPages(1).last(true)
                .build();

        when(transactionService.listTransactions(eq("1234567890123456"), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions")
                        .param("cardNum", "1234567890123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tranId").value("0000000000000001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listTransactions_emptyResult() throws Exception {
        PagedResponse<TransactionDto> response = PagedResponse.<TransactionDto>builder()
                .content(Collections.emptyList())
                .page(0).size(10).totalElements(0).totalPages(0).last(true)
                .build();

        when(transactionService.listTransactions(eq("0000000000000000"), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions")
                        .param("cardNum", "0000000000000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addTransaction_returns200() throws Exception {
        TransactionDto dto = TransactionDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranAmt(new BigDecimal("50.00"))
                .tranCardNum("1234567890123456")
                .build();

        TransactionDto result = TransactionDto.builder()
                .tranId("0000000000000101")
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranAmt(new BigDecimal("50.00"))
                .tranCardNum("1234567890123456")
                .build();

        when(transactionService.addTransaction(any(TransactionDto.class))).thenReturn(result);

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranId").value("0000000000000101"));
    }
}

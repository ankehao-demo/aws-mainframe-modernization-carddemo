package com.cardemo.controller;

import com.cardemo.config.JwtAuthenticationFilter;
import com.cardemo.config.JwtTokenProvider;
import com.cardemo.config.SecurityConfig;
import com.cardemo.dto.TransactionDetailResponse;
import com.cardemo.dto.TransactionListPageResponse;
import com.cardemo.dto.TransactionListResponse;
import com.cardemo.exception.ResourceNotFoundException;
import com.cardemo.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "USER")
    void listTransactions_noFilter_returns200() throws Exception {
        TransactionListPageResponse response = new TransactionListPageResponse(
                List.of(
                        new TransactionListResponse("0000000000000001", "01/15/24",
                                "Amazon Purchase", new BigDecimal("125.50")),
                        new TransactionListResponse("0000000000000002", "01/16/24",
                                "Grocery Store Purchase", new BigDecimal("67.89"))
                ),
                0, false, false,
                "0000000000000001", "0000000000000002"
        );
        when(transactionService.listTransactions(isNull(), any())).thenReturn(response);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].transactionId").value("0000000000000001"))
                .andExpect(jsonPath("$.content[0].amount").value(125.50))
                .andExpect(jsonPath("$.firstTransactionId").value("0000000000000001"))
                .andExpect(jsonPath("$.lastTransactionId").value("0000000000000002"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listTransactions_withStartId_returns200() throws Exception {
        TransactionListPageResponse response = new TransactionListPageResponse(
                List.of(new TransactionListResponse("0000000000000002", "01/16/24",
                        "Grocery Store Purchase", new BigDecimal("67.89"))),
                0, false, false,
                "0000000000000002", "0000000000000002"
        );
        when(transactionService.listTransactions(eq("0000000000000002"), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions")
                        .param("startTransactionId", "0000000000000002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listTransactions_nonNumericFilter_returns400() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .param("startTransactionId", "ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transaction ID filter must be numeric"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTransactionDetail_valid_returns200() throws Exception {
        TransactionDetailResponse response = new TransactionDetailResponse();
        response.setTransactionId("0000000000000001");
        response.setCardNumber("4111111111111111");
        response.setTypeCode("SA");
        response.setCategoryCode(5001);
        response.setSource("ONLINE");
        response.setAmount(new BigDecimal("125.50"));
        response.setDescription("Amazon Purchase");
        response.setOriginTimestamp("2024-01-15-10.30.00.000000");
        response.setProcessedTimestamp("2024-01-15-10.30.05.000000");
        response.setMerchantId(100000001L);
        response.setMerchantName("Amazon.com");
        response.setMerchantCity("Seattle");
        response.setMerchantZip("98101");

        when(transactionService.getTransactionDetail("0000000000000001"))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions/0000000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("0000000000000001"))
                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.typeCode").value("SA"))
                .andExpect(jsonPath("$.amount").value(125.50))
                .andExpect(jsonPath("$.merchantName").value("Amazon.com"))
                .andExpect(jsonPath("$.merchantCity").value("Seattle"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTransactionDetail_notFound_returns404() throws Exception {
        when(transactionService.getTransactionDetail("9999999999999999"))
                .thenThrow(new ResourceNotFoundException(
                        "Transaction ID NOT found: 9999999999999999"));

        mockMvc.perform(get("/api/transactions/9999999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction ID NOT found: 9999999999999999"));
    }

    @Test
    void listTransactions_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listTransactions_adminRole_returns200() throws Exception {
        TransactionListPageResponse response = new TransactionListPageResponse(
                List.of(), 0, false, false, null, null
        );
        when(transactionService.listTransactions(isNull(), any())).thenReturn(response);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk());
    }
}

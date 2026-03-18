package com.aws.carddemo.controller;

import com.aws.carddemo.dto.BillPaymentRequest;
import com.aws.carddemo.dto.BillPaymentResponse;
import com.aws.carddemo.exception.ValidationException;
import com.aws.carddemo.security.CardDemoUserDetailsService;
import com.aws.carddemo.security.JwtTokenProvider;
import com.aws.carddemo.service.BillPaymentService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillPaymentController.class)
class BillPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillPaymentService billPaymentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CardDemoUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void processBillPayment_success() throws Exception {
        BillPaymentResponse response = BillPaymentResponse.builder()
                .success(true)
                .message("Payment processed successfully")
                .tranId("0000000000000101")
                .tranAmt(new BigDecimal("1500.00"))
                .newBalance(BigDecimal.ZERO)
                .build();

        when(billPaymentService.processBillPayment(any(BillPaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/bill-payment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                BillPaymentRequest.builder().acctId("00000000001").confirm("Y").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tranId").value("0000000000000101"))
                .andExpect(jsonPath("$.newBalance").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void processBillPayment_decline() throws Exception {
        BillPaymentResponse response = BillPaymentResponse.builder()
                .success(false)
                .message("Payment declined by user")
                .build();

        when(billPaymentService.processBillPayment(any(BillPaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/bill-payment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                BillPaymentRequest.builder().acctId("00000000001").confirm("N").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void processBillPayment_emptyAcctId() throws Exception {
        when(billPaymentService.processBillPayment(any(BillPaymentRequest.class)))
                .thenThrow(new ValidationException("Acct ID can NOT be empty..."));

        mockMvc.perform(post("/api/bill-payment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                BillPaymentRequest.builder().acctId("").confirm("Y").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Acct ID can NOT be empty..."));
    }

    @Test
    @WithMockUser(roles = "USER")
    void processBillPayment_invalidConfirm() throws Exception {
        when(billPaymentService.processBillPayment(any(BillPaymentRequest.class)))
                .thenThrow(new ValidationException("Invalid value. Valid values are (Y/N)..."));

        mockMvc.perform(post("/api/bill-payment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                BillPaymentRequest.builder().acctId("00000000001").confirm("X").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value. Valid values are (Y/N)..."));
    }

    @Test
    @WithMockUser(roles = "USER")
    void processBillPayment_zeroBalance() throws Exception {
        when(billPaymentService.processBillPayment(any(BillPaymentRequest.class)))
                .thenThrow(new ValidationException("You have nothing to pay..."));

        mockMvc.perform(post("/api/bill-payment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                BillPaymentRequest.builder().acctId("00000000001").confirm("Y").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You have nothing to pay..."));
    }
}

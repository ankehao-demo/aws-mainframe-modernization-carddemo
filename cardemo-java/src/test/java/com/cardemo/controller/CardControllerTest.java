package com.cardemo.controller;

import com.cardemo.config.JwtAuthenticationFilter;
import com.cardemo.config.JwtTokenProvider;
import com.cardemo.config.SecurityConfig;
import com.cardemo.dto.CardDetailResponse;
import com.cardemo.dto.CardListPageResponse;
import com.cardemo.dto.CardListResponse;
import com.cardemo.exception.ResourceNotFoundException;
import com.cardemo.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "USER")
    void listCards_noFilters_returns200() throws Exception {
        CardListPageResponse response = new CardListPageResponse(
                List.of(new CardListResponse(10000000001L, "4111111111111111", "Y"),
                        new CardListResponse(10000000002L, "4222222222222222", "Y")),
                0, 1, false, false
        );
        when(cardService.listCards(isNull(), isNull(), any())).thenReturn(response);

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listCards_withAccountIdFilter_returns200() throws Exception {
        CardListPageResponse response = new CardListPageResponse(
                List.of(new CardListResponse(10000000001L, "4111111111111111", "Y")),
                0, 1, false, false
        );
        when(cardService.listCards(eq(10000000001L), isNull(), any())).thenReturn(response);

        mockMvc.perform(get("/api/cards")
                        .param("accountId", "10000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listCards_invalidAccountIdFilter_returns400() throws Exception {
        mockMvc.perform(get("/api/cards")
                        .param("accountId", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account ID filter must be an 11-digit number"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listCards_invalidCardNumberFilter_returns400() throws Exception {
        mockMvc.perform(get("/api/cards")
                        .param("cardNumber", "1234"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Card number filter must be a 16-digit number"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listCards_pagination_returns200() throws Exception {
        CardListPageResponse response = new CardListPageResponse(
                List.of(), 1, 2, false, true
        );
        when(cardService.listCards(isNull(), isNull(), any())).thenReturn(response);

        mockMvc.perform(get("/api/cards")
                        .param("page", "1")
                        .param("size", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasPreviousPage").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCardDetail_validNumber_returns200() throws Exception {
        CardDetailResponse response = new CardDetailResponse();
        response.setCardNumber("4111111111111111");
        response.setAccountId(10000000001L);
        response.setCvvCode(123);
        response.setEmbossedName("JOHN A SMITH");
        response.setExpirationDate("2025-01-15");
        response.setExpirationMonth("01");
        response.setExpirationYear("2025");
        response.setActiveStatus("Y");

        when(cardService.getCardDetail("4111111111111111")).thenReturn(response);

        mockMvc.perform(get("/api/cards/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.accountId").value(10000000001L))
                .andExpect(jsonPath("$.cvvCode").value(123))
                .andExpect(jsonPath("$.embossedName").value("JOHN A SMITH"))
                .andExpect(jsonPath("$.expirationMonth").value("01"))
                .andExpect(jsonPath("$.expirationYear").value("2025"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCardDetail_invalidNumber_returns400() throws Exception {
        mockMvc.perform(get("/api/cards/1234"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Card number must be a 16-digit number"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCardDetail_notFound_returns404() throws Exception {
        when(cardService.getCardDetail("9999999999999999"))
                .thenThrow(new ResourceNotFoundException("Card number not found: 9999999999999999"));

        mockMvc.perform(get("/api/cards/9999999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void listCards_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isForbidden());
    }
}

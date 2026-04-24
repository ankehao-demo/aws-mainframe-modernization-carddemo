package com.carddemo.cardservice.controller;

import com.carddemo.cardservice.exception.CardNotFoundException;
import com.carddemo.cardservice.exception.CardValidationException;
import com.carddemo.cardservice.exception.GlobalExceptionHandler;
import com.carddemo.cardservice.exception.OptimisticLockException;
import com.carddemo.cardservice.model.dto.CardDetailResponse;
import com.carddemo.cardservice.model.dto.CardListResponse;
import com.carddemo.cardservice.model.dto.CardListResponse.CardSummary;
import com.carddemo.cardservice.model.dto.CardUpdateRequest;
import com.carddemo.cardservice.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@Import(GlobalExceptionHandler.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardService cardService;

    @TestConfiguration
    static class Config {
        @Bean
        CardService cardService() {
            return mock(CardService.class);
        }
    }

    @Test
    @DisplayName("GET /api/v1/cards - list cards with default pagination")
    void listCards() throws Exception {
        CardListResponse response = new CardListResponse(
                List.of(new CardSummary("4000000000000001", "00000000001", "Y")),
                0, 7, 1, 1, false, false);

        when(cardService.listCards(any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards[0].cardNum").value("4000000000000001"))
                .andExpect(jsonPath("$.size").value(7));
    }

    @Test
    @DisplayName("GET /api/v1/cards - filter by accountId")
    void listCardsFilterByAccount() throws Exception {
        CardListResponse response = new CardListResponse(
                List.of(new CardSummary("4000000000000001", "00000000001", "Y")),
                0, 7, 1, 1, false, false);

        when(cardService.listCards(eq("00000000001"), any(), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/cards")
                        .param("accountId", "00000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].cardAcctId").value("00000000001"));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{cardNum} - get card detail")
    void getCardDetail() throws Exception {
        CardDetailResponse response = new CardDetailResponse(
                "4000000000000001", "00000000001", "123",
                "JOHN DOE", "12/2028", "Y", 0L);

        when(cardService.getCard("4000000000000001")).thenReturn(response);

        mockMvc.perform(get("/api/v1/cards/4000000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4000000000000001"))
                .andExpect(jsonPath("$.cardEmbossedName").value("JOHN DOE"))
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{cardNum} - card not found returns 404")
    void getCardNotFound() throws Exception {
        when(cardService.getCard("9999999999999999"))
                .thenThrow(new CardNotFoundException("9999999999999999"));

        mockMvc.perform(get("/api/v1/cards/9999999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Card Not Found"));
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{cardNum} - successful update")
    void updateCard() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest(
                "00000000001", "JANE DOE", "Y", "06/2030", 0L);

        CardDetailResponse response = new CardDetailResponse(
                "4000000000000001", "00000000001", "123",
                "JANE DOE", "06/2030", "Y", 1L);

        when(cardService.updateCard(eq("4000000000000001"), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/cards/4000000000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardEmbossedName").value("JANE DOE"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{cardNum} - optimistic lock conflict returns 409")
    void updateCardConflict() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest(
                "00000000001", "JANE DOE", "Y", "06/2030", 0L);

        when(cardService.updateCard(eq("4000000000000001"), any()))
                .thenThrow(new OptimisticLockException("4000000000000001"));

        mockMvc.perform(put("/api/v1/cards/4000000000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Concurrent Modification"));
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{cardNum} - validation error returns 400")
    void updateCardValidationError() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest(
                "00000000001", "JOHN DOE", "Y", "12/2028", 0L);

        when(cardService.updateCard(eq("4000000000000001"), any()))
                .thenThrow(new CardValidationException("Account ID must be an 11-digit number"));

        mockMvc.perform(put("/api/v1/cards/4000000000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Card Validation Failed"));
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{cardNum} - missing required fields returns 400")
    void updateCardMissingFields() throws Exception {
        String invalidRequest = """
                {"cardAcctId": "", "cardEmbossedName": "", "cardActiveStatus": "", "cardExpirationDate": ""}
                """;

        mockMvc.perform(put("/api/v1/cards/4000000000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}

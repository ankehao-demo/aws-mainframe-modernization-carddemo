package com.aws.carddemo.controller;

import com.aws.carddemo.dto.CardDto;
import com.aws.carddemo.dto.PagedResponse;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.security.CardDemoUserDetailsService;
import com.aws.carddemo.security.JwtTokenProvider;
import com.aws.carddemo.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CardDemoUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void listCards_returnsPaginated() throws Exception {
        CardDto card = CardDto.builder()
                .cardNum("1234567890123456")
                .cardAcctId("00000000001")
                .cardActiveStatus("Y")
                .build();

        PagedResponse<CardDto> response = PagedResponse.<CardDto>builder()
                .content(List.of(card))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(cardService.listCards(any(Pageable.class))).thenReturn(response);

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardNum").value("1234567890123456"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCard_found() throws Exception {
        CardDto card = CardDto.builder()
                .cardNum("1234567890123456")
                .cardEmbossedName("JOHN DOE")
                .build();

        when(cardService.getCard("1234567890123456")).thenReturn(card);

        mockMvc.perform(get("/api/cards/1234567890123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardEmbossedName").value("JOHN DOE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCard_notFound() throws Exception {
        when(cardService.getCard("0000000000000000"))
                .thenThrow(new ResourceNotFoundException("Card not found"));

        mockMvc.perform(get("/api/cards/0000000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCardsByAccount() throws Exception {
        CardDto card = CardDto.builder()
                .cardNum("1234567890123456")
                .cardAcctId("00000000001")
                .build();

        when(cardService.getCardsByAccount("00000000001")).thenReturn(List.of(card));

        mockMvc.perform(get("/api/cards/by-account/00000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardNum").value("1234567890123456"));
    }
}

package com.carddemo.cardservice.service;

import com.carddemo.cardservice.exception.CardNotFoundException;
import com.carddemo.cardservice.exception.OptimisticLockException;
import com.carddemo.cardservice.model.Card;
import com.carddemo.cardservice.model.dto.CardDetailResponse;
import com.carddemo.cardservice.model.dto.CardListResponse;
import com.carddemo.cardservice.model.dto.CardUpdateRequest;
import com.carddemo.cardservice.repository.CardRepository;
import com.carddemo.cardservice.validation.CardValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardValidator cardValidator;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardRepository, cardValidator);
    }

    @Test
    @DisplayName("List cards returns paginated results")
    void listCardsReturnsPage() {
        Card card = createTestCard();
        Pageable pageable = PageRequest.of(0, 7, Sort.by("cardNum").ascending());
        Page<Card> page = new PageImpl<>(List.of(card), pageable, 1);

        when(cardRepository.findAll(any(Pageable.class))).thenReturn(page);

        CardListResponse response = cardService.listCards(null, null, 0, 7);

        assertEquals(1, response.cards().size());
        assertEquals("4000000000000001", response.cards().get(0).cardNum());
        assertEquals(7, response.size());
    }

    @Test
    @DisplayName("List cards with account filter")
    void listCardsWithAccountFilter() {
        Card card = createTestCard();
        Pageable pageable = PageRequest.of(0, 7, Sort.by("cardNum").ascending());
        Page<Card> page = new PageImpl<>(List.of(card), pageable, 1);

        when(cardRepository.findByCardAcctId(eq("00000000001"), any(Pageable.class)))
                .thenReturn(page);

        CardListResponse response = cardService.listCards("00000000001", null, 0, 7);

        assertEquals(1, response.cards().size());
        assertEquals("00000000001", response.cards().get(0).cardAcctId());
    }

    @Test
    @DisplayName("Get card returns detail response")
    void getCardReturnsDetail() {
        Card card = createTestCard();
        when(cardRepository.findById("4000000000000001")).thenReturn(Optional.of(card));

        CardDetailResponse response = cardService.getCard("4000000000000001");

        assertEquals("4000000000000001", response.cardNum());
        assertEquals("JOHN DOE", response.cardEmbossedName());
    }

    @Test
    @DisplayName("Get card throws CardNotFoundException for missing card")
    void getCardNotFound() {
        when(cardRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.getCard("9999999999999999"));
    }

    @Test
    @DisplayName("Update card saves and returns updated detail")
    void updateCardSuccess() {
        Card card = createTestCard();
        when(cardRepository.findById("4000000000000001")).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardUpdateRequest request = new CardUpdateRequest(
                "00000000001", "JANE DOE", "N", "06/2030", 0L);

        CardDetailResponse response = cardService.updateCard("4000000000000001", request);

        assertNotNull(response);
        verify(cardValidator).validateCardUpdate(request);
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("Update card throws OptimisticLockException for version mismatch")
    void updateCardVersionMismatch() {
        Card card = createTestCard();
        card.setVersion(5L);
        when(cardRepository.findById("4000000000000001")).thenReturn(Optional.of(card));

        CardUpdateRequest request = new CardUpdateRequest(
                "00000000001", "JANE DOE", "N", "06/2030", 0L);

        assertThrows(OptimisticLockException.class,
                () -> cardService.updateCard("4000000000000001", request));
    }

    @Test
    @DisplayName("Update card throws CardNotFoundException for missing card")
    void updateCardNotFound() {
        when(cardRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        CardUpdateRequest request = new CardUpdateRequest(
                "00000000001", "JANE DOE", "N", "06/2030", 0L);

        assertThrows(CardNotFoundException.class,
                () -> cardService.updateCard("9999999999999999", request));
    }

    private Card createTestCard() {
        Card card = new Card("4000000000000001", "00000000001", "123",
                "JOHN DOE", "12/2028", "Y");
        card.setVersion(0L);
        return card;
    }
}

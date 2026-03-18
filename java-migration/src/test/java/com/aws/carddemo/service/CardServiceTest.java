package com.aws.carddemo.service;

import com.aws.carddemo.dto.CardDto;
import com.aws.carddemo.dto.PagedResponse;
import com.aws.carddemo.entity.Card;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.CardRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private CardService cardService;

    private Card testCard;

    @BeforeEach
    void setUp() {
        testCard = Card.builder()
                .cardNum("1234567890123456")
                .cardAcctId("00000000001")
                .cardCvvCd("123")
                .cardEmbossedName("JOHN DOE")
                .cardExpirationDate("2025-12-31")
                .cardActiveStatus("Y")
                .build();
    }

    @Test
    void listCards_returnsPaginatedResults() {
        Page<Card> page = new PageImpl<>(List.of(testCard), PageRequest.of(0, 10), 1);
        when(cardRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<CardDto> response = cardService.listCards(PageRequest.of(0, 10));

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().get(0).getCardNum()).isEqualTo("1234567890123456");
    }

    @Test
    void getCard_found_returnsDto() {
        when(cardRepository.findById("1234567890123456")).thenReturn(Optional.of(testCard));

        CardDto dto = cardService.getCard("1234567890123456");

        assertThat(dto.getCardNum()).isEqualTo("1234567890123456");
        assertThat(dto.getCardEmbossedName()).isEqualTo("JOHN DOE");
    }

    @Test
    void getCard_notFound_throwsException() {
        when(cardRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCard("0000000000000000"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCard_success() {
        when(cardRepository.findById("1234567890123456")).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        CardDto updateDto = CardDto.builder()
                .cardEmbossedName("JANE DOE")
                .cardActiveStatus("N")
                .build();

        CardDto result = cardService.updateCard("1234567890123456", updateDto);
        assertThat(result).isNotNull();
    }

    @Test
    void getCardsByAccount_returnsCards() {
        when(cardRepository.findByCardAcctId("00000000001")).thenReturn(List.of(testCard));

        List<CardDto> cards = cardService.getCardsByAccount("00000000001");

        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getCardAcctId()).isEqualTo("00000000001");
    }
}

package com.aws.carddemo.service;

import com.aws.carddemo.dto.PagedResponse;
import com.aws.carddemo.dto.TransactionDto;
import com.aws.carddemo.entity.CardXref;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.exception.ValidationException;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
                .tranId("0000000000000001")
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranSource("POS TERM")
                .tranDesc("TEST PURCHASE")
                .tranAmt(new BigDecimal("100.00"))
                .tranMerchantId(123456789L)
                .tranMerchantName("TEST MERCHANT")
                .tranMerchantCity("NEW YORK")
                .tranMerchantZip("10001")
                .tranCardNum("1234567890123456")
                .tranOrigTs("2024-01-15 10:30:00.000000")
                .tranProcTs("2024-01-15 10:30:01.000000")
                .build();
    }

    @Test
    void listTransactions_returnsPagedResponse() {
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction), PageRequest.of(0, 10), 1);
        when(transactionRepository.findByTranCardNum("1234567890123456", PageRequest.of(0, 10)))
                .thenReturn(page);

        PagedResponse<TransactionDto> response = transactionService
                .listTransactions("1234567890123456", PageRequest.of(0, 10));

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTranId()).isEqualTo("0000000000000001");
    }

    @Test
    void listTransactions_emptyResult() {
        Page<Transaction> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(transactionRepository.findByTranCardNum("0000000000000000", PageRequest.of(0, 10)))
                .thenReturn(page);

        PagedResponse<TransactionDto> response = transactionService
                .listTransactions("0000000000000000", PageRequest.of(0, 10));

        assertThat(response.getContent()).isEmpty();
    }

    @Test
    void getTransaction_found() {
        when(transactionRepository.findById("0000000000000001")).thenReturn(Optional.of(testTransaction));

        TransactionDto dto = transactionService.getTransaction("0000000000000001");

        assertThat(dto.getTranId()).isEqualTo("0000000000000001");
        assertThat(dto.getTranAmt()).isEqualByComparingTo("100.00");
    }

    @Test
    void getTransaction_notFound() {
        when(transactionRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction("9999999999999999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addTransaction_success() {
        CardXref xref = CardXref.builder()
                .xrefCardNum("1234567890123456")
                .xrefAcctId("00000000001")
                .xrefCustId("000000001")
                .build();

        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(transactionRepository.findMaxTranId()).thenReturn(Optional.of("0000000000000100"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        TransactionDto dto = TransactionDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranSource("POS TERM")
                .tranDesc("NEW PURCHASE")
                .tranAmt(new BigDecimal("50.00"))
                .tranCardNum("1234567890123456")
                .build();

        TransactionDto result = transactionService.addTransaction(dto);

        assertThat(result.getTranId()).isEqualTo("0000000000000101");
    }

    @Test
    void addTransaction_invalidCard_throwsException() {
        when(cardXrefRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        TransactionDto dto = TransactionDto.builder()
                .tranCardNum("0000000000000000")
                .build();

        assertThatThrownBy(() -> transactionService.addTransaction(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addTransaction_missingCardNum_throwsException() {
        TransactionDto dto = TransactionDto.builder().build();

        assertThatThrownBy(() -> transactionService.addTransaction(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Card number is required");
    }

    @Test
    void generateNextTranId_incrementsCorrectly() {
        when(transactionRepository.findMaxTranId()).thenReturn(Optional.of("0000000000000099"));

        String nextId = transactionService.generateNextTranId();

        assertThat(nextId).isEqualTo("0000000000000100");
    }
}

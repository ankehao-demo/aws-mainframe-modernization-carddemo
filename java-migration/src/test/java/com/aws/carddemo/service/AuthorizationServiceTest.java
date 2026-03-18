package com.aws.carddemo.service;

import com.aws.carddemo.dto.AuthorizationRequest;
import com.aws.carddemo.dto.AuthorizationResponse;
import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.CardXref;
import com.aws.carddemo.entity.PendingAuthSummary;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PendingAuthSummaryRepository pendingAuthSummaryRepository;
    @Mock
    private PendingAuthDetailRepository pendingAuthDetailRepository;
    @Mock
    private AuthFraudRepository authFraudRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private CardXref testXref;
    private Account testAccount;
    private PendingAuthSummary testSummary;

    @BeforeEach
    void setUp() {
        testXref = CardXref.builder()
                .xrefCardNum("1234567890123456")
                .xrefAcctId("00000000001")
                .xrefCustId("000000001")
                .build();

        testAccount = Account.builder()
                .acctId("00000000001")
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCurrBal(new BigDecimal("1000.00"))
                .build();

        testSummary = PendingAuthSummary.builder()
                .id(1L)
                .paAcctId("00000000001")
                .paApprovedCount(0)
                .paApprovedAmount(BigDecimal.ZERO)
                .paDeclinedCount(0)
                .paDeclinedAmount(BigDecimal.ZERO)
                .paCreditLimit(new BigDecimal("5000.00"))
                .paCreditAvailable(new BigDecimal("4000.00"))
                .build();
    }

    @Test
    void processAuthorization_approved() {
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(testXref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(pendingAuthSummaryRepository.findByPaAcctId("00000000001")).thenReturn(Optional.of(testSummary));
        when(pendingAuthDetailRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pendingAuthSummaryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AuthorizationRequest request = AuthorizationRequest.builder()
                .cardNum("1234567890123456")
                .amount(new BigDecimal("500.00"))
                .merchantId("MERCH001")
                .merchantName("TEST STORE")
                .build();

        AuthorizationResponse response = authorizationService.processAuthorization(request);

        assertThat(response.getResponseCode()).isEqualTo("0000");
        assertThat(response.getApprovedAmount()).isEqualByComparingTo("500.00");
        assertThat(response.getMessage()).isEqualTo("Approved");
    }

    @Test
    void processAuthorization_declined_overlimit() {
        testSummary.setPaApprovedAmount(new BigDecimal("4500.00"));
        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(testXref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(pendingAuthSummaryRepository.findByPaAcctId("00000000001")).thenReturn(Optional.of(testSummary));
        when(pendingAuthDetailRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pendingAuthSummaryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AuthorizationRequest request = AuthorizationRequest.builder()
                .cardNum("1234567890123456")
                .amount(new BigDecimal("1000.00"))
                .build();

        AuthorizationResponse response = authorizationService.processAuthorization(request);

        assertThat(response.getResponseCode()).isEqualTo("0051");
        assertThat(response.getApprovedAmount()).isEqualByComparingTo("0");
        assertThat(response.getMessage()).contains("Declined");
    }

    @Test
    void processAuthorization_cardNotFound() {
        when(cardXrefRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        AuthorizationRequest request = AuthorizationRequest.builder()
                .cardNum("0000000000000000")
                .amount(new BigDecimal("100.00"))
                .build();

        assertThatThrownBy(() -> authorizationService.processAuthorization(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markFraud_success() {
        when(authFraudRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        authorizationService.markFraud("1234567890123456", "TRAN001", "MERCH001");
    }
}

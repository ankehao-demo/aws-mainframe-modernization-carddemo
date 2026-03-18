package com.aws.carddemo.service;

import com.aws.carddemo.dto.BillPaymentRequest;
import com.aws.carddemo.dto.BillPaymentResponse;
import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.CardXref;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.exception.ValidationException;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.TransactionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillPaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BillPaymentService billPaymentService;

    private Account testAccount;
    private CardXref testXref;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .acctId("00000000001")
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("1500.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("1500.00"))
                .acctCurrCycCredit(BigDecimal.ZERO)
                .acctCurrCycDebit(BigDecimal.ZERO)
                .version(0L)
                .build();

        testXref = CardXref.builder()
                .xrefCardNum("1234567890123456")
                .xrefAcctId("00000000001")
                .xrefCustId("000000001")
                .build();
    }

    @Test
    void processBillPayment_fullPayment_success() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(cardXrefRepository.findFirstByXrefAcctId("00000000001")).thenReturn(Optional.of(testXref));
        when(transactionRepository.findMaxTranId()).thenReturn(Optional.of("0000000000000100"));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BillPaymentRequest request = BillPaymentRequest.builder()
                .acctId("00000000001")
                .confirm("Y")
                .build();

        BillPaymentResponse response = billPaymentService.processBillPayment(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTranAmt()).isEqualByComparingTo("1500.00");
        assertThat(response.getNewBalance()).isEqualByComparingTo("0.00");
        assertThat(response.getTranId()).isEqualTo("0000000000000101");
        verify(transactionRepository).save(any());
        verify(accountRepository).save(any());
    }

    @Test
    void processBillPayment_decline_noTransaction() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));

        BillPaymentRequest request = BillPaymentRequest.builder()
                .acctId("00000000001")
                .confirm("N")
                .build();

        BillPaymentResponse response = billPaymentService.processBillPayment(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Payment declined by user");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processBillPayment_zeroBalance_throwsException() {
        testAccount.setAcctCurrBal(BigDecimal.ZERO);
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));

        BillPaymentRequest request = BillPaymentRequest.builder()
                .acctId("00000000001")
                .confirm("Y")
                .build();

        assertThatThrownBy(() -> billPaymentService.processBillPayment(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("You have nothing to pay...");
    }

    @Test
    void processBillPayment_negativeBalance_throwsException() {
        testAccount.setAcctCurrBal(new BigDecimal("-100.00"));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));

        BillPaymentRequest request = BillPaymentRequest.builder()
                .acctId("00000000001")
                .confirm("Y")
                .build();

        assertThatThrownBy(() -> billPaymentService.processBillPayment(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("You have nothing to pay...");
    }

    @Test
    void processBillPayment_invalidAccount_throwsException() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());

        BillPaymentRequest request = BillPaymentRequest.builder()
                .acctId("99999999999")
                .confirm("Y")
                .build();

        assertThatThrownBy(() -> billPaymentService.processBillPayment(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void processBillPayment_emptyAcctId_throwsException() {
        BillPaymentRequest request = BillPaymentRequest.builder()
                .acctId("")
                .confirm("Y")
                .build();

        assertThatThrownBy(() -> billPaymentService.processBillPayment(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Acct ID can NOT be empty...");
    }

    @Test
    void processBillPayment_nullAcctId_throwsException() {
        BillPaymentRequest request = BillPaymentRequest.builder()
                .acctId(null)
                .confirm("Y")
                .build();

        assertThatThrownBy(() -> billPaymentService.processBillPayment(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Acct ID can NOT be empty...");
    }

    @Test
    void processBillPayment_invalidConfirm_throwsException() {
        BillPaymentRequest request = BillPaymentRequest.builder()
                .acctId("00000000001")
                .confirm("X")
                .build();

        assertThatThrownBy(() -> billPaymentService.processBillPayment(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid value. Valid values are (Y/N)...");
    }

    @Test
    void processBillPayment_transactionFieldValues() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(cardXrefRepository.findFirstByXrefAcctId("00000000001")).thenReturn(Optional.of(testXref));
        when(transactionRepository.findMaxTranId()).thenReturn(Optional.of("0000000000000050"));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BillPaymentRequest request = BillPaymentRequest.builder()
                .acctId("00000000001")
                .confirm("Y")
                .build();

        BillPaymentResponse response = billPaymentService.processBillPayment(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTranId()).isEqualTo("0000000000000051");
    }

    @Test
    void processBillPayment_balanceArithmetic() {
        testAccount.setAcctCurrBal(new BigDecimal("2500.50"));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(cardXrefRepository.findFirstByXrefAcctId("00000000001")).thenReturn(Optional.of(testXref));
        when(transactionRepository.findMaxTranId()).thenReturn(Optional.of("0000000000000001"));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BillPaymentRequest request = BillPaymentRequest.builder()
                .acctId("00000000001")
                .confirm("Y")
                .build();

        BillPaymentResponse response = billPaymentService.processBillPayment(request);

        assertThat(response.getTranAmt()).isEqualByComparingTo("2500.50");
        assertThat(response.getNewBalance()).isEqualByComparingTo("0.00");
    }
}

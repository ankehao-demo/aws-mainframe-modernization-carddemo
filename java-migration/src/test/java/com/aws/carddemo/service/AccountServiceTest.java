package com.aws.carddemo.service;

import com.aws.carddemo.dto.AccountDto;
import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.CardXref;
import com.aws.carddemo.entity.Customer;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.CustomerRepository;
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
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .acctId("00000000001")
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("1500.00"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("1500.00"))
                .acctOpenDate("2020-01-15")
                .acctExpirationDate("2025-01-15")
                .acctReissueDate("2023-01-15")
                .acctCurrCycCredit(new BigDecimal("200.00"))
                .acctCurrCycDebit(new BigDecimal("500.00"))
                .acctAddrZip("10001")
                .acctGroupId("GROUP001")
                .version(0L)
                .build();
    }

    @Test
    void getAccount_found_returnsDto() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(cardXrefRepository.findFirstByXrefAcctId("00000000001")).thenReturn(Optional.empty());

        AccountDto dto = accountService.getAccount("00000000001");

        assertThat(dto.getAcctId()).isEqualTo("00000000001");
        assertThat(dto.getAcctCurrBal()).isEqualByComparingTo("1500.00");
        assertThat(dto.getAcctCreditLimit()).isEqualByComparingTo("5000.00");
    }

    @Test
    void getAccount_withCustomer_includesCustomer() {
        CardXref xref = CardXref.builder()
                .xrefCardNum("1234567890123456")
                .xrefAcctId("00000000001")
                .xrefCustId("000000001")
                .build();

        Customer customer = Customer.builder()
                .custId("000000001")
                .custFirstName("JOHN")
                .custMiddleName("M")
                .custLastName("DOE")
                .build();

        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(cardXrefRepository.findFirstByXrefAcctId("00000000001")).thenReturn(Optional.of(xref));
        when(customerRepository.findById("000000001")).thenReturn(Optional.of(customer));

        AccountDto dto = accountService.getAccount("00000000001");

        assertThat(dto.getCustomer()).isNotNull();
        assertThat(dto.getCustomer().getCustFirstName()).isEqualTo("JOHN");
    }

    @Test
    void getAccount_notFound_throwsException() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount("99999999999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAccount_success() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountDto updateDto = AccountDto.builder()
                .acctActiveStatus("N")
                .acctCreditLimit(new BigDecimal("10000.00"))
                .build();

        AccountDto result = accountService.updateAccount("00000000001", updateDto);
        assertThat(result).isNotNull();
    }

    @Test
    void getAccount_zeroBalance_displaysCorrectly() {
        testAccount.setAcctCurrBal(BigDecimal.ZERO);
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(testAccount));
        when(cardXrefRepository.findFirstByXrefAcctId("00000000001")).thenReturn(Optional.empty());

        AccountDto dto = accountService.getAccount("00000000001");
        assertThat(dto.getAcctCurrBal()).isEqualByComparingTo("0.00");
    }
}

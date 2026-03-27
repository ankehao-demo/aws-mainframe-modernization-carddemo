package com.carddemo.repository;

import com.carddemo.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldSaveAndFindAccount() {
        Account account = createTestAccount(1L);
        entityManager.persistAndFlush(account);

        Optional<Account> found = accountRepository.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getCurrBal()).isEqualByComparingTo(new BigDecimal("194.00"));
        assertThat(found.get().getActiveStatus()).isEqualTo("Y");
    }

    @Test
    void shouldReturnEmptyForNonExistentAccount() {
        Optional<Account> found = accountRepository.findById(99999L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateAccount() {
        Account account = createTestAccount(2L);
        entityManager.persistAndFlush(account);

        account.setCurrBal(new BigDecimal("500.00"));
        accountRepository.save(account);

        Account updated = entityManager.find(Account.class, 2L);
        assertThat(updated.getCurrBal()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldDeleteAccount() {
        Account account = createTestAccount(3L);
        entityManager.persistAndFlush(account);

        accountRepository.deleteById(3L);

        assertThat(accountRepository.findById(3L)).isEmpty();
    }

    @Test
    void shouldHandleBigDecimalPrecision() {
        Account account = createTestAccount(4L);
        account.setCurrBal(new BigDecimal("9999999999.99"));
        entityManager.persistAndFlush(account);

        Optional<Account> found = accountRepository.findById(4L);
        assertThat(found).isPresent();
        assertThat(found.get().getCurrBal()).isEqualByComparingTo(new BigDecimal("9999999999.99"));
    }

    @Test
    void shouldFindAllAccounts() {
        Account a1 = createTestAccount(10L);
        Account a2 = createTestAccount(11L);
        entityManager.persistAndFlush(a1);
        entityManager.persistAndFlush(a2);

        assertThat(accountRepository.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    private Account createTestAccount(Long id) {
        Account a = new Account();
        a.setAcctId(id);
        a.setActiveStatus("Y");
        a.setCurrBal(new BigDecimal("194.00"));
        a.setCreditLimit(new BigDecimal("20200.00"));
        a.setCashCreditLimit(new BigDecimal("10200.00"));
        a.setOpenDate(LocalDate.of(2014, 11, 20));
        a.setExpirationDate(LocalDate.of(2025, 5, 20));
        a.setReissueDate(LocalDate.of(2025, 5, 20));
        a.setCurrCycCredit(BigDecimal.ZERO);
        a.setCurrCycDebit(BigDecimal.ZERO);
        a.setAddrZip("A000000000");
        a.setGroupId("");
        return a;
    }
}

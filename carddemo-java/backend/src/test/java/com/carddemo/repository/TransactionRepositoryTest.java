package com.carddemo.repository;

import com.carddemo.model.Account;
import com.carddemo.model.Card;
import com.carddemo.model.Customer;
import com.carddemo.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        Account a = new Account();
        a.setAcctId(100L);
        a.setActiveStatus("Y");
        a.setCurrBal(BigDecimal.ZERO);
        a.setCreditLimit(BigDecimal.ZERO);
        a.setCashCreditLimit(BigDecimal.ZERO);
        a.setCurrCycCredit(BigDecimal.ZERO);
        a.setCurrCycDebit(BigDecimal.ZERO);
        entityManager.persistAndFlush(a);

        Customer c = new Customer();
        c.setCustId(10L);
        entityManager.persistAndFlush(c);

        Card card = new Card();
        card.setCardNum("4111111111111111");
        card.setAcctId(100L);
        card.setCustId(10L);
        card.setActiveStatus("Y");
        entityManager.persistAndFlush(card);
    }

    @Test
    void shouldFindByCardNumWithPagination() {
        for (int i = 1; i <= 5; i++) {
            Transaction t = new Transaction();
            t.setTranId(String.format("%016d", i));
            t.setTypeCd("01");
            t.setCatCd(1);
            t.setAmount(new BigDecimal("10.00"));
            t.setCardNum("4111111111111111");
            t.setOrigTs(LocalDateTime.now());
            entityManager.persistAndFlush(t);
        }

        Page<Transaction> page = transactionRepository.findByCardNum(
                "4111111111111111", PageRequest.of(0, 3));

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyPageForNonExistentCard() {
        Page<Transaction> page = transactionRepository.findByCardNum(
                "9999999999999999", PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void shouldSaveAndFindTransaction() {
        Transaction t = new Transaction();
        t.setTranId("0000000000000100");
        t.setTypeCd("01");
        t.setCatCd(1);
        t.setSource("POS TERM");
        t.setDescription("Purchase at Store");
        t.setAmount(new BigDecimal("50.47"));
        t.setMerchantId(800000000L);
        t.setMerchantName("Store");
        t.setMerchantCity("City");
        t.setMerchantZip("12345");
        t.setCardNum("4111111111111111");
        t.setOrigTs(LocalDateTime.of(2022, 6, 10, 19, 27, 53));
        entityManager.persistAndFlush(t);

        assertThat(transactionRepository.findById("0000000000000100")).isPresent();
    }

    @Test
    void shouldDeleteTransaction() {
        Transaction t = new Transaction();
        t.setTranId("0000000000000200");
        t.setTypeCd("02");
        t.setCatCd(1);
        t.setAmount(new BigDecimal("25.00"));
        t.setCardNum("4111111111111111");
        entityManager.persistAndFlush(t);

        transactionRepository.deleteById("0000000000000200");
        assertThat(transactionRepository.findById("0000000000000200")).isEmpty();
    }
}

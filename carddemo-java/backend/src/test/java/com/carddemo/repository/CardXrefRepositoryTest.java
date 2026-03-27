package com.carddemo.repository;

import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CardXrefRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @BeforeEach
    void setUp() {
        // Create prerequisite accounts and customers
        Account a1 = new Account();
        a1.setAcctId(50L);
        a1.setActiveStatus("Y");
        a1.setCurrBal(BigDecimal.ZERO);
        a1.setCreditLimit(BigDecimal.ZERO);
        a1.setCashCreditLimit(BigDecimal.ZERO);
        a1.setCurrCycCredit(BigDecimal.ZERO);
        a1.setCurrCycDebit(BigDecimal.ZERO);
        entityManager.persistAndFlush(a1);

        Account a2 = new Account();
        a2.setAcctId(51L);
        a2.setActiveStatus("Y");
        a2.setCurrBal(BigDecimal.ZERO);
        a2.setCreditLimit(BigDecimal.ZERO);
        a2.setCashCreditLimit(BigDecimal.ZERO);
        a2.setCurrCycCredit(BigDecimal.ZERO);
        a2.setCurrCycDebit(BigDecimal.ZERO);
        entityManager.persistAndFlush(a2);

        Customer c1 = new Customer();
        c1.setCustId(5L);
        entityManager.persistAndFlush(c1);

        Customer c2 = new Customer();
        c2.setCustId(6L);
        entityManager.persistAndFlush(c2);
    }

    @Test
    void shouldFindByAcctId() {
        CardXref x1 = new CardXref("4000000000000001", 5L, 50L);
        CardXref x2 = new CardXref("4000000000000002", 6L, 50L);
        CardXref x3 = new CardXref("4000000000000003", 5L, 51L);
        entityManager.persistAndFlush(x1);
        entityManager.persistAndFlush(x2);
        entityManager.persistAndFlush(x3);

        List<CardXref> result = cardXrefRepository.findByAcctId(50L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CardXref::getCardNum)
                .containsExactlyInAnyOrder("4000000000000001", "4000000000000002");
    }

    @Test
    void shouldFindByCardNum() {
        CardXref x1 = new CardXref("5000000000000001", 5L, 50L);
        entityManager.persistAndFlush(x1);

        Optional<CardXref> found = cardXrefRepository.findByCardNum("5000000000000001");

        assertThat(found).isPresent();
        assertThat(found.get().getAcctId()).isEqualTo(50L);
        assertThat(found.get().getCustId()).isEqualTo(5L);
    }

    @Test
    void shouldReturnEmptyForNonExistentCardNum() {
        Optional<CardXref> found = cardXrefRepository.findByCardNum("9999999999999999");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForNonExistentAcctId() {
        List<CardXref> result = cardXrefRepository.findByAcctId(99999L);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldSaveAndDelete() {
        CardXref x = new CardXref("6000000000000001", 5L, 50L);
        entityManager.persistAndFlush(x);

        cardXrefRepository.deleteById("6000000000000001");
        assertThat(cardXrefRepository.findById("6000000000000001")).isEmpty();
    }
}

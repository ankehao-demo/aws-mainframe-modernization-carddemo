package com.carddemo.repository;

import com.carddemo.model.TranCatBalance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TranCatBalanceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TranCatBalanceRepository tranCatBalanceRepository;

    @Test
    void shouldFindByAcctId() {
        TranCatBalance b1 = new TranCatBalance(1L, "01", 1, new BigDecimal("100.00"));
        TranCatBalance b2 = new TranCatBalance(1L, "01", 2, new BigDecimal("200.00"));
        TranCatBalance b3 = new TranCatBalance(2L, "01", 1, new BigDecimal("300.00"));
        entityManager.persistAndFlush(b1);
        entityManager.persistAndFlush(b2);
        entityManager.persistAndFlush(b3);

        List<TranCatBalance> result = tranCatBalanceRepository.findByAcctId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TranCatBalance::getBalance)
                .containsExactlyInAnyOrder(new BigDecimal("100.00"), new BigDecimal("200.00"));
    }

    @Test
    void shouldReturnEmptyForNonExistentAcctId() {
        List<TranCatBalance> result = tranCatBalanceRepository.findByAcctId(99999L);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldSaveAndFindByCompositeKey() {
        TranCatBalance b = new TranCatBalance(10L, "02", 3, new BigDecimal("500.00"));
        tranCatBalanceRepository.save(b);

        assertThat(tranCatBalanceRepository.findAll()).anyMatch(
                tcb -> tcb.getAcctId().equals(10L) &&
                       tcb.getTypeCd().equals("02") &&
                       tcb.getCatCd().equals(3));
    }

    @Test
    void shouldDeleteByCompositeKey() {
        TranCatBalance b = new TranCatBalance(20L, "01", 1, new BigDecimal("50.00"));
        entityManager.persistAndFlush(b);

        long countBefore = tranCatBalanceRepository.count();
        tranCatBalanceRepository.delete(b);
        long countAfter = tranCatBalanceRepository.count();

        assertThat(countAfter).isEqualTo(countBefore - 1);
    }
}

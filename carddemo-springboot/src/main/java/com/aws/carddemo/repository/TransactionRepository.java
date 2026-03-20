package com.aws.carddemo.repository;

import com.aws.carddemo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByCardNum(String cardNum, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.cardNum IN " +
           "(SELECT x.cardNum FROM CardCrossRef x WHERE x.acctId = :acctId)")
    Page<Transaction> findByAcctId(@Param("acctId") String acctId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.origTs BETWEEN :startDate AND :endDate ORDER BY t.cardNum, t.origTs")
    List<Transaction> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

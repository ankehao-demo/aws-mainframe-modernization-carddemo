package com.carddemo.common.repository;

import com.carddemo.common.entity.Transaction;
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

    Page<Transaction> findByCardNumber(String cardNumber, Pageable pageable);

    @Query("SELECT t FROM Transaction t JOIN Card c ON t.cardNumber = c.cardNumber " +
           "WHERE c.accountId = :accountId ORDER BY t.transactionTimestamp DESC")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.transactionTimestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionTimestamp DESC")
    List<Transaction> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<Transaction> findByTransactionTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByCardNumberInAndTransactionTimestampBetween(
            List<String> cardNumbers, LocalDateTime start, LocalDateTime end);
}

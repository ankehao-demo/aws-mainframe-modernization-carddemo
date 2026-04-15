package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);
    Page<Transaction> findByCardNumber(String cardNumber, Pageable pageable);
}

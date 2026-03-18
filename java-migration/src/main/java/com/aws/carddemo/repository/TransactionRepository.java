package com.aws.carddemo.repository;

import com.aws.carddemo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByTranCardNum(String tranCardNum, Pageable pageable);

    @Query("SELECT MAX(t.tranId) FROM Transaction t")
    Optional<String> findMaxTranId();
}

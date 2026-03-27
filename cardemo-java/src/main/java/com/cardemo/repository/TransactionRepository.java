package com.cardemo.repository;

import com.cardemo.entity.Transaction;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByTranId(String tranId);

    Page<Transaction> findByTranIdGreaterThanEqual(String tranId, Pageable pageable);
}

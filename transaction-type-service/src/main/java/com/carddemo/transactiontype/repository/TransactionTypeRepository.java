package com.carddemo.transactiontype.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carddemo.transactiontype.entity.TransactionType;

@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {

    Page<TransactionType> findByTrTypeGreaterThanEqualOrderByTrType(String startKey, Pageable pageable);

    Page<TransactionType> findByTrTypeAndTrDescriptionContaining(
            String trType, String description, Pageable pageable);
}

package com.carddemo.common.repository;

import com.carddemo.common.entity.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCategoryRepository
        extends JpaRepository<TransactionCategory, TransactionCategory.TransactionCategoryId> {
}

package com.aws.carddemo.repository;

import com.aws.carddemo.entity.TransactionCategory;
import com.aws.carddemo.entity.TransactionCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, TransactionCategoryId> {
    List<TransactionCategory> findByTranTypeCd(String tranTypeCd);
}

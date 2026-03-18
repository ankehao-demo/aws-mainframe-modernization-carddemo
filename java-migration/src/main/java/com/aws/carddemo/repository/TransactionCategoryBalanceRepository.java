package com.aws.carddemo.repository;

import com.aws.carddemo.entity.TransactionCategoryBalance;
import com.aws.carddemo.entity.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCategoryBalanceRepository extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {
    List<TransactionCategoryBalance> findByTrancatAcctId(String trancatAcctId);
}

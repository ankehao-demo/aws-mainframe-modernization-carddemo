package com.carddemo.common.repository;

import com.carddemo.common.entity.CategoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryBalanceRepository
        extends JpaRepository<CategoryBalance, CategoryBalance.CategoryBalanceId> {

    List<CategoryBalance> findByAccountId(Long accountId);
}

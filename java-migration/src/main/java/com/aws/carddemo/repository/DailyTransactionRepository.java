package com.aws.carddemo.repository;

import com.aws.carddemo.entity.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, Long> {
    List<DailyTransaction> findByPostedFalse();
}

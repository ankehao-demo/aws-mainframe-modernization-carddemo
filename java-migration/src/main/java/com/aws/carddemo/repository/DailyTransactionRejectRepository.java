package com.aws.carddemo.repository;

import com.aws.carddemo.entity.DailyTransactionReject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyTransactionRejectRepository extends JpaRepository<DailyTransactionReject, Long> {
}

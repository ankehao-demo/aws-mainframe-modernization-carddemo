package com.aws.carddemo.repository;

import com.aws.carddemo.entity.PendingAuthSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingAuthSummaryRepository extends JpaRepository<PendingAuthSummary, Long> {
    Optional<PendingAuthSummary> findByPaAcctId(String paAcctId);
}

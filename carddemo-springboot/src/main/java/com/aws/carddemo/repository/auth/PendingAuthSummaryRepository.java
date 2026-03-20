package com.aws.carddemo.repository.auth;

import com.aws.carddemo.entity.auth.PendingAuthSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingAuthSummaryRepository extends JpaRepository<PendingAuthSummary, String> {
    Page<PendingAuthSummary> findByCardNum(String cardNum, Pageable pageable);
    Page<PendingAuthSummary> findByAcctId(String acctId, Pageable pageable);
    Page<PendingAuthSummary> findByAuthStatus(String authStatus, Pageable pageable);
}

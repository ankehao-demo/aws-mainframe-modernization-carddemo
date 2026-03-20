package com.aws.carddemo.repository.auth;

import com.aws.carddemo.entity.auth.FraudRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudRecordRepository extends JpaRepository<FraudRecord, Long> {
    Page<FraudRecord> findByCardNum(String cardNum, Pageable pageable);
    Page<FraudRecord> findByAcctId(String acctId, Pageable pageable);
}

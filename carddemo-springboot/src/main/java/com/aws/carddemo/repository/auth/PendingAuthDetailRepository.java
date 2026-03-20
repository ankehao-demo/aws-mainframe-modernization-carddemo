package com.aws.carddemo.repository.auth;

import com.aws.carddemo.entity.auth.PendingAuthDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PendingAuthDetailRepository extends JpaRepository<PendingAuthDetail, Long> {
    List<PendingAuthDetail> findBySummaryAuthId(String authId);
}

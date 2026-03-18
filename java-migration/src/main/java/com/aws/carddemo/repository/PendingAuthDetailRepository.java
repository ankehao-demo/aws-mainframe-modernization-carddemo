package com.aws.carddemo.repository;

import com.aws.carddemo.entity.PendingAuthDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PendingAuthDetailRepository extends JpaRepository<PendingAuthDetail, Long> {
    List<PendingAuthDetail> findBySummaryId(Long summaryId);
}

package com.carddemo.report.repository;

import com.carddemo.report.entity.ReportMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportMetadataRepository extends JpaRepository<ReportMetadata, Long> {
}

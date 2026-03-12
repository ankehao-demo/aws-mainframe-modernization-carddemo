package com.carddemo.common.repository;

import com.carddemo.common.entity.DisclosureGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisclosureGroupRepository
        extends JpaRepository<DisclosureGroup, DisclosureGroup.DisclosureGroupId> {

    java.util.List<DisclosureGroup> findByAccountGroupId(String accountGroupId);
}

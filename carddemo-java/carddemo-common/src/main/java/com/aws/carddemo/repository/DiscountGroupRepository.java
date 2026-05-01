package com.aws.carddemo.repository;

import com.aws.carddemo.entity.DiscountGroup;
import com.aws.carddemo.entity.DiscountGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountGroupRepository extends JpaRepository<DiscountGroup, DiscountGroupId> {
}

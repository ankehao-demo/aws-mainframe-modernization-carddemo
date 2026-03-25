package com.carddemo.repository;

import com.carddemo.model.TranCategory;
import com.carddemo.model.TranCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranCategoryRepository extends JpaRepository<TranCategory, TranCategoryId> {
}

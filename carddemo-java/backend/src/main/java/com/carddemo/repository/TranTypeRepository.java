package com.carddemo.repository;

import com.carddemo.model.TranType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranTypeRepository extends JpaRepository<TranType, String> {
}

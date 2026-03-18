package com.aws.carddemo.repository;

import com.aws.carddemo.entity.AuthFraud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthFraudRepository extends JpaRepository<AuthFraud, Long> {
    List<AuthFraud> findByAfCardNum(String afCardNum);
}

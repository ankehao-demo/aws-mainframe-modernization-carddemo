package com.carddemo.common.repository;

import com.carddemo.common.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByAccountId(Long accountId);

    List<Card> findByCustomerId(Long customerId);
}

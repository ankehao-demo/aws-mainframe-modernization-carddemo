package com.aws.carddemo.repository;

import com.aws.carddemo.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByAccount_AcctId(Long acctId);
}

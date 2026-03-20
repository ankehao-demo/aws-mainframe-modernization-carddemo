package com.aws.carddemo.repository;

import com.aws.carddemo.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    Page<Card> findByAcctId(String acctId, Pageable pageable);
}

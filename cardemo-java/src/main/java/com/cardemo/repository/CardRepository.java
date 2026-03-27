package com.cardemo.repository;

import com.cardemo.entity.Card;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    Page<Card> findByAcctId(Long acctId, Pageable pageable);

    Page<Card> findByCardNum(String cardNum, Pageable pageable);

    Page<Card> findByAcctIdAndCardNum(Long acctId, String cardNum, Pageable pageable);

    Optional<Card> findByCardNum(String cardNum);
}

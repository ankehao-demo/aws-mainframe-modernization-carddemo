package com.carddemo.cardservice.repository;

import com.carddemo.cardservice.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository replacing VSAM KSDS CARDDAT file access.
 * The COBOL programs use CICS STARTBR/READNEXT/READPREV for browsing
 * and direct READ for single-record access. Spring Data JPA pagination
 * replaces the VSAM browse operations.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    Page<Card> findByCardAcctId(String cardAcctId, Pageable pageable);

    Page<Card> findByCardNumStartingWith(String cardNumPrefix, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE "
            + "(:accountId IS NULL OR c.cardAcctId = :accountId) AND "
            + "(:cardNum IS NULL OR c.cardNum LIKE :cardNum%)")
    Page<Card> findByFilters(
            @Param("accountId") String accountId,
            @Param("cardNum") String cardNum,
            Pageable pageable);
}

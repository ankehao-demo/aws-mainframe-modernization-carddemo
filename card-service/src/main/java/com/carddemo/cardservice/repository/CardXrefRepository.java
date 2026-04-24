package com.carddemo.cardservice.repository;

import com.carddemo.cardservice.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository replacing VSAM KSDS CCXREF (card cross-reference) file access.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByXrefAcctId(String acctId);

    List<CardXref> findByXrefCustId(String custId);
}

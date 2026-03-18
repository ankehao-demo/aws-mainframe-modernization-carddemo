package com.aws.carddemo.repository;

import com.aws.carddemo.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
    List<CardXref> findByXrefAcctId(String xrefAcctId);
    Optional<CardXref> findFirstByXrefAcctId(String xrefAcctId);
    List<CardXref> findByXrefCustId(String xrefCustId);
}

package com.cardemo.repository;

import com.cardemo.entity.CardXref;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    Optional<CardXref> findByAcctId(Long acctId);
}

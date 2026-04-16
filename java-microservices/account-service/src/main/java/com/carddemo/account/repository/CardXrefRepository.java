package com.carddemo.account.repository;

import com.carddemo.account.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
    List<CardXref> findByAccountId(Long accountId);
}

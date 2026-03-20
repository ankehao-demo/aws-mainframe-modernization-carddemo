package com.aws.carddemo.repository;

import com.aws.carddemo.entity.CardCrossRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CardCrossRefRepository extends JpaRepository<CardCrossRef, String> {

    List<CardCrossRef> findByAcctId(String acctId);

    List<CardCrossRef> findByCustId(String custId);
}

package com.aws.carddemo.repository;

import com.aws.carddemo.entity.TranCatBalance;
import com.aws.carddemo.entity.TranCatBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranCatBalanceRepository extends JpaRepository<TranCatBalance, TranCatBalanceId> {

    List<TranCatBalance> findById_AcctId(Long acctId);
}

package com.carddemo.repository;

import com.carddemo.model.TranCatBalance;
import com.carddemo.model.TranCatBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranCatBalanceRepository extends JpaRepository<TranCatBalance, TranCatBalanceId> {

    List<TranCatBalance> findByAcctId(Long acctId);
}

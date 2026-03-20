package com.aws.carddemo.repository;

import com.aws.carddemo.entity.TranCategoryBalance;
import com.aws.carddemo.entity.TranCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TranCategoryBalanceRepository extends JpaRepository<TranCategoryBalance, TranCategoryBalanceId> {

    List<TranCategoryBalance> findByAcctId(String acctId);
}

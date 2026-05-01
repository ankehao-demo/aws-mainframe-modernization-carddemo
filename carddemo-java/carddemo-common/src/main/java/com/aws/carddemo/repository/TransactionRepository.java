package com.aws.carddemo.repository;

import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.entity.TransactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, TransactionId> {

    List<Transaction> findById_CardNum(String cardNum);
}

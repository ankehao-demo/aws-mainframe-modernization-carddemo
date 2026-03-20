package com.aws.carddemo.service;

import com.aws.carddemo.dto.TransactionRequest;
import com.aws.carddemo.entity.DailyTransaction;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.DailyTransactionRepository;
import com.aws.carddemo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Transaction management — mirrors COTRN00C.cbl (list), COTRN01C.cbl (view), COTRN02C.cbl (add).
 * Uses AIX-equivalent queries (by card_num or acct_id via cross-ref join).
 * New transactions go to daily_transaction staging table per COTRN02C logic.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final DailyTransactionRepository dailyTransactionRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              DailyTransactionRepository dailyTransactionRepository) {
        this.transactionRepository = transactionRepository;
        this.dailyTransactionRepository = dailyTransactionRepository;
    }

    public Page<Transaction> listTransactions(String acctId, String cardNum, Pageable pageable) {
        if (cardNum != null && !cardNum.isBlank()) {
            return transactionRepository.findByCardNum(cardNum, pageable);
        }
        if (acctId != null && !acctId.isBlank()) {
            return transactionRepository.findByAcctId(acctId, pageable);
        }
        return transactionRepository.findAll(pageable);
    }

    public Transaction getTransaction(String tranId) {
        return transactionRepository.findById(tranId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + tranId));
    }

    @Transactional
    public DailyTransaction addTransaction(TransactionRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String tranId = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).substring(0, 16);

        DailyTransaction dt = new DailyTransaction();
        dt.setTranId(tranId);
        dt.setTypeCd(request.typeCd());
        dt.setCatCd(request.catCd());
        dt.setSource(request.source());
        dt.setDescription(request.description());
        dt.setAmount(request.amount());
        dt.setMerchantId(request.merchantId());
        dt.setMerchantName(request.merchantName());
        dt.setMerchantCity(request.merchantCity());
        dt.setMerchantZip(request.merchantZip());
        dt.setCardNum(request.cardNum());
        dt.setOrigTs(now);
        dt.setProcTs(null);

        return dailyTransactionRepository.save(dt);
    }
}

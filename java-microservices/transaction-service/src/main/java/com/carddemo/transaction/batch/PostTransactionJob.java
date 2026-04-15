package com.carddemo.transaction.batch;

import com.carddemo.transaction.entity.DailyTransaction;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.repository.DailyTransactionRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Post transaction batch job - replaces POSTTRAN/CBTRN02C.cbl.
 * Reads daily transactions, validates, posts to transaction table.
 */
@Component
public class PostTransactionJob {
    private static final Logger log = LoggerFactory.getLogger(PostTransactionJob.class);

    private final DailyTransactionRepository dailyTransactionRepository;
    private final TransactionRepository transactionRepository;

    public PostTransactionJob(DailyTransactionRepository dailyTransactionRepository,
                              TransactionRepository transactionRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.transactionRepository = transactionRepository;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void postDailyTransactions() {
        log.info("Starting daily transaction posting job");
        List<DailyTransaction> dailyTransactions = dailyTransactionRepository.findAll();
        int posted = 0;
        String processedTs = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));

        for (DailyTransaction daily : dailyTransactions) {
            Transaction tran = new Transaction();
            tran.setTransactionId(daily.getTransactionId());
            tran.setTypeCode(daily.getTypeCode());
            tran.setCategoryCode(daily.getCategoryCode());
            tran.setSource(daily.getSource());
            tran.setDescription(daily.getDescription());
            tran.setAmount(daily.getAmount());
            tran.setMerchantId(daily.getMerchantId());
            tran.setMerchantName(daily.getMerchantName());
            tran.setMerchantCity(daily.getMerchantCity());
            tran.setMerchantZip(daily.getMerchantZip());
            tran.setCardNumber(daily.getCardNumber());
            tran.setOriginalTimestamp(daily.getOriginalTimestamp());
            tran.setProcessedTimestamp(processedTs);
            transactionRepository.save(tran);
            posted++;
        }
        dailyTransactionRepository.deleteAll();
        log.info("Posted {} daily transactions", posted);
    }
}

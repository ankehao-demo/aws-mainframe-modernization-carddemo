package com.carddemo.transaction.batch;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.DailyTransaction;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Spring Batch job ported from JCL POSTTRAN.jcl + COBOL CBTRN02C.cbl.
 * Reads from daily_transactions staging table, validates, posts to transactions,
 * and updates account balances.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class PostTransactionJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    @Bean
    public Job postTransactionJob(JobRepository jobRepository, Step postTransactionStep) {
        return new JobBuilder("postTransactionJob", jobRepository)
                .start(postTransactionStep)
                .build();
    }

    @Bean
    public Step postTransactionStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
        return new StepBuilder("postTransactionStep", jobRepository)
                .<DailyTransaction, Transaction>chunk(100, transactionManager)
                .reader(dailyTransactionReader())
                .processor(dailyTransactionProcessor())
                .writer(transactionWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<DailyTransaction> dailyTransactionReader() {
        return new JpaPagingItemReaderBuilder<DailyTransaction>()
                .name("dailyTransactionReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DailyTransaction d ORDER BY d.transactionId")
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<DailyTransaction, Transaction> dailyTransactionProcessor() {
        return dailyTransaction -> {
            log.debug("Processing daily transaction: {}", dailyTransaction.getTransactionId());

            var cards = cardRepository.findById(dailyTransaction.getCardNumber());
            if (cards.isEmpty()) {
                log.warn("Card not found for transaction {}: {}",
                        dailyTransaction.getTransactionId(), dailyTransaction.getCardNumber());
                return null;
            }

            var card = cards.get();
            var accountOpt = accountRepository.findById(card.getAccountId());
            if (accountOpt.isEmpty()) {
                log.warn("Account not found for card {}", card.getCardNumber());
                return null;
            }

            Account account = accountOpt.get();
            BigDecimal newBalance = account.getCurrentBalance()
                    .add(dailyTransaction.getTransactionAmount())
                    .setScale(2, RoundingMode.HALF_UP);
            account.setCurrentBalance(newBalance);
            accountRepository.save(account);

            Transaction transaction = new Transaction();
            transaction.setTransactionId(dailyTransaction.getTransactionId());
            transaction.setTransactionTypeCode(dailyTransaction.getTransactionTypeCode());
            transaction.setTransactionCategoryCode(dailyTransaction.getTransactionCategoryCode());
            transaction.setTransactionSource(dailyTransaction.getTransactionSource());
            transaction.setTransactionDescription(dailyTransaction.getTransactionDescription());
            transaction.setTransactionAmount(dailyTransaction.getTransactionAmount());
            transaction.setMerchantId(dailyTransaction.getMerchantId());
            transaction.setMerchantName(dailyTransaction.getMerchantName());
            transaction.setMerchantCity(dailyTransaction.getMerchantCity());
            transaction.setMerchantZip(dailyTransaction.getMerchantZip());
            transaction.setCardNumber(dailyTransaction.getCardNumber());
            transaction.setTransactionTimestamp(dailyTransaction.getTransactionTimestamp());
            transaction.setProcessedTimestamp(LocalDateTime.now());

            return transaction;
        };
    }

    @Bean
    public JpaItemWriter<Transaction> transactionWriter() {
        JpaItemWriter<Transaction> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}

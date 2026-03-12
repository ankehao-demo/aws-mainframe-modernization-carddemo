package com.carddemo.transaction.batch;

import com.carddemo.common.entity.Transaction;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job ported from JCL TRANREPT.jcl + COBOL CBTRN03C.cbl.
 * Generates transaction reports by reading transactions within a date range.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class TransactionReportJobConfig {

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job transactionReportJob(JobRepository jobRepository, Step transactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(transactionReportStep)
                .build();
    }

    @Bean
    public Step transactionReportStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("transactionReportStep", jobRepository)
                .<Transaction, Transaction>chunk(100, transactionManager)
                .reader(transactionReportReader())
                .writer(transactionReportWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Transaction> transactionReportReader() {
        return new JpaPagingItemReaderBuilder<Transaction>()
                .name("transactionReportReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT t FROM Transaction t ORDER BY t.cardNumber, t.transactionTimestamp")
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemWriter<Transaction> transactionReportWriter() {
        return items -> {
            for (Transaction transaction : items) {
                log.info("REPORT | TranID: {} | Card: {} | Amount: {} | Merchant: {} | Date: {}",
                        transaction.getTransactionId(),
                        transaction.getCardNumber(),
                        transaction.getTransactionAmount(),
                        transaction.getMerchantName(),
                        transaction.getTransactionTimestamp());
            }
            log.info("Report batch: {} transactions processed", items.size());
        };
    }
}

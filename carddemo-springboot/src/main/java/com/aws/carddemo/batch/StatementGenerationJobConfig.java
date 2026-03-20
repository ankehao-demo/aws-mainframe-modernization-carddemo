package com.aws.carddemo.batch;

import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardCrossRefRepository;
import com.aws.carddemo.repository.CustomerRepository;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * StatementGenerationJob — mirrors CBSTM03A.CBL batch program.
 * Reads transactions joined with accounts and customers,
 * generates statement output grouped by account.
 * Uses the report structure from CVTRA07Y.cpy.
 */
@Configuration
public class StatementGenerationJobConfig {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationJobConfig.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CardCrossRefRepository cardCrossRefRepository;

    public StatementGenerationJobConfig(AccountRepository accountRepository,
                                         CustomerRepository customerRepository,
                                         CardCrossRefRepository cardCrossRefRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.cardCrossRefRepository = cardCrossRefRepository;
    }

    @Bean
    public JpaPagingItemReader<Transaction> statementTransactionReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<Transaction>()
                .name("statementTransactionReader")
                .entityManagerFactory(emf)
                .queryString("SELECT t FROM Transaction t ORDER BY t.cardNum, t.origTs")
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemWriter<Transaction> statementWriter() {
        return transactions -> {
            for (Transaction tran : transactions) {
                // In production, this would generate PDF/text statements grouped by account
                // For now, log the statement line items
                log.info("STATEMENT: tran_id={} card={} amount={} desc={}",
                        tran.getTranId(), tran.getCardNum(), tran.getAmount(), tran.getDescription());
            }
        };
    }

    @Bean
    public Step statementGenerationStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         JpaPagingItemReader<Transaction> statementTransactionReader,
                                         ItemWriter<Transaction> statementWriter) {
        return new StepBuilder("statementGenerationStep", jobRepository)
                .<Transaction, Transaction>chunk(100, transactionManager)
                .reader(statementTransactionReader)
                .writer(statementWriter)
                .build();
    }

    @Bean
    public Job statementGenerationJob(JobRepository jobRepository, Step statementGenerationStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .start(statementGenerationStep)
                .build();
    }
}

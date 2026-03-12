package com.carddemo.report.batch;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.common.repository.TransactionRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring Batch job ported from JCL CREASTMT.JCL + COBOL CBSTM03A.CBL.
 * Generates customer account statements by iterating accounts,
 * fetching transactions, and producing statement output.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class StatementGenerationJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @Bean
    public Job statementGenerationJob(JobRepository jobRepository, Step statementGenerationStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .start(statementGenerationStep)
                .build();
    }

    @Bean
    public Step statementGenerationStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
        return new StepBuilder("statementGenerationStep", jobRepository)
                .<Account, Map<String, Object>>chunk(50, transactionManager)
                .reader(statementAccountReader())
                .processor(statementProcessor())
                .writer(statementWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Account> statementAccountReader() {
        return new JpaPagingItemReaderBuilder<Account>()
                .name("statementAccountReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT a FROM Account a WHERE a.accountStatus = 'Y' ORDER BY a.accountId")
                .pageSize(50)
                .build();
    }

    @Bean
    public ItemProcessor<Account, Map<String, Object>> statementProcessor() {
        return account -> {
            Map<String, Object> statement = new HashMap<>();
            statement.put("accountId", account.getAccountId());
            statement.put("currentBalance", account.getCurrentBalance());
            statement.put("creditLimit", account.getCreditLimit());
            statement.put("availableCredit",
                    account.getCreditLimit().subtract(account.getCurrentBalance())
                            .setScale(2, RoundingMode.HALF_UP));
            statement.put("cycleCredits", account.getCurrCycCredit());
            statement.put("cycleDebits", account.getCurrCycDebit());

            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Transaction> recentTransactions = transactionRepository
                    .findByTransactionTimestampBetween(thirtyDaysAgo, LocalDateTime.now());

            BigDecimal statementTotal = recentTransactions.stream()
                    .map(Transaction::getTransactionAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            statement.put("statementTotal", statementTotal);
            statement.put("transactionCount", recentTransactions.size());
            statement.put("generatedAt", LocalDateTime.now().toString());

            return statement;
        };
    }

    @Bean
    public ItemWriter<Map<String, Object>> statementWriter() {
        return items -> {
            for (Map<String, Object> statement : items) {
                log.info("STATEMENT | Account: {} | Balance: {} | Available: {} | Transactions: {}",
                        statement.get("accountId"),
                        statement.get("currentBalance"),
                        statement.get("availableCredit"),
                        statement.get("transactionCount"));
            }
            log.info("Statement batch: {} accounts processed", items.size());
        };
    }
}

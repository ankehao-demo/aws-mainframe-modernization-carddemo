package com.aws.carddemo.batch;

import com.aws.carddemo.entity.Account;
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
 * AccountExportJob — mirrors CBACT01C.cbl batch program.
 * Reads accounts and writes to output formats for data compatibility testing.
 */
@Configuration
public class AccountExportJobConfig {

    private static final Logger log = LoggerFactory.getLogger(AccountExportJobConfig.class);

    @Bean
    public JpaPagingItemReader<Account> accountExportReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<Account>()
                .name("accountExportReader")
                .entityManagerFactory(emf)
                .queryString("SELECT a FROM Account a ORDER BY a.acctId")
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemWriter<Account> accountExportWriter() {
        return accounts -> {
            for (Account account : accounts) {
                // Fixed-width format compatible with COBOL record layout (300 bytes)
                String record = String.format("%-11s%1s%012.2f%012.2f%012.2f%-10s%-10s%-10s%012.2f%012.2f%-10s%-10s",
                        account.getAcctId(),
                        account.getActiveStatus(),
                        account.getCurrBal(),
                        account.getCreditLimit(),
                        account.getCashCreditLimit(),
                        account.getOpenDate() != null ? account.getOpenDate().toString() : "",
                        account.getExpirationDate() != null ? account.getExpirationDate().toString() : "",
                        account.getReissueDate() != null ? account.getReissueDate().toString() : "",
                        account.getCurrCycCredit(),
                        account.getCurrCycDebit(),
                        account.getAddrZip() != null ? account.getAddrZip() : "",
                        account.getGroupId() != null ? account.getGroupId() : "");
                log.info("EXPORT: {}", record);
            }
        };
    }

    @Bean
    public Step accountExportStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   JpaPagingItemReader<Account> accountExportReader,
                                   ItemWriter<Account> accountExportWriter) {
        return new StepBuilder("accountExportStep", jobRepository)
                .<Account, Account>chunk(100, transactionManager)
                .reader(accountExportReader)
                .writer(accountExportWriter)
                .build();
    }

    @Bean
    public Job accountExportJob(JobRepository jobRepository, Step accountExportStep) {
        return new JobBuilder("accountExportJob", jobRepository)
                .start(accountExportStep)
                .build();
    }
}

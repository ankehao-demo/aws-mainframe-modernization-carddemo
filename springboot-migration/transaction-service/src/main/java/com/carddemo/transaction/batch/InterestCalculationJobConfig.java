package com.carddemo.transaction.batch;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.DisclosureGroup;
import com.carddemo.common.repository.DisclosureGroupRepository;
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
import java.util.List;

/**
 * Spring Batch job ported from JCL INTCALC.jcl + COBOL CBACT04C.cbl.
 * Iterates all accounts, looks up disclosure group interest rate, and applies interest
 * to the current balance.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class InterestCalculationJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final DisclosureGroupRepository disclosureGroupRepository;

    @Bean
    public Job interestCalculationJob(JobRepository jobRepository, Step interestCalculationStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep)
                .build();
    }

    @Bean
    public Step interestCalculationStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .<Account, Account>chunk(100, transactionManager)
                .reader(accountReader())
                .processor(interestProcessor())
                .writer(accountWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Account> accountReader() {
        return new JpaPagingItemReaderBuilder<Account>()
                .name("accountReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT a FROM Account a WHERE a.accountStatus = 'Y' ORDER BY a.accountId")
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<Account, Account> interestProcessor() {
        return account -> {
            if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }

            BigDecimal interestRate = BigDecimal.ZERO;

            if (account.getGroupId() != null) {
                List<DisclosureGroup> groups =
                        disclosureGroupRepository.findByAccountGroupId(account.getGroupId().trim());
                if (!groups.isEmpty()) {
                    interestRate = groups.get(0).getInterestRate();
                }
            }

            if (interestRate.compareTo(BigDecimal.ZERO) == 0) {
                interestRate = new BigDecimal("19.99");
            }

            BigDecimal monthlyRate = interestRate
                    .divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
            BigDecimal interestAmount = account.getCurrentBalance()
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            log.debug("Account {}: balance={}, rate={}%, interest={}",
                    account.getAccountId(), account.getCurrentBalance(),
                    interestRate, interestAmount);

            account.setCurrentBalance(
                    account.getCurrentBalance().add(interestAmount)
                            .setScale(2, RoundingMode.HALF_UP));

            return account;
        };
    }

    @Bean
    public JpaItemWriter<Account> accountWriter() {
        JpaItemWriter<Account> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}

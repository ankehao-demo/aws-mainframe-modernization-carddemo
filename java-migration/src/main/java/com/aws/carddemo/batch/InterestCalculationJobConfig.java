package com.aws.carddemo.batch;

import com.aws.carddemo.entity.Account;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class InterestCalculationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InterestCalculationProcessor processor;
    private final EntityManagerFactory entityManagerFactory;

    public InterestCalculationJobConfig(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        InterestCalculationProcessor processor,
                                        EntityManagerFactory entityManagerFactory) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.processor = processor;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job interestCalculationJob() {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep())
                .build();
    }

    @Bean
    public Step interestCalculationStep() {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .<Account, Account>chunk(10, transactionManager)
                .reader(activeAccountReader())
                .processor(processor)
                .writer(accountWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<Account> activeAccountReader() {
        JpaCursorItemReader<Account> reader = new JpaCursorItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT a FROM Account a WHERE a.acctActiveStatus = 'Y'");
        return reader;
    }

    @Bean
    public JpaItemWriter<Account> accountWriter() {
        JpaItemWriter<Account> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}

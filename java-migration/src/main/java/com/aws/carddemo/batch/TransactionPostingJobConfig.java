package com.aws.carddemo.batch;

import com.aws.carddemo.entity.DailyTransaction;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.repository.DailyTransactionRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.Map;

@Configuration
public class TransactionPostingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DailyTransactionRepository dailyTransactionRepository;
    private final TransactionPostingProcessor processor;
    private final EntityManagerFactory entityManagerFactory;

    public TransactionPostingJobConfig(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       DailyTransactionRepository dailyTransactionRepository,
                                       TransactionPostingProcessor processor,
                                       EntityManagerFactory entityManagerFactory) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.processor = processor;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job transactionPostingJob() {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(transactionPostingStep())
                .build();
    }

    @Bean
    public Step transactionPostingStep() {
        return new StepBuilder("transactionPostingStep", jobRepository)
                .<DailyTransaction, Transaction>chunk(10, transactionManager)
                .reader(dailyTransactionReader())
                .processor(processor)
                .writer(transactionWriter())
                .build();
    }

    @Bean
    public ItemReader<DailyTransaction> dailyTransactionReader() {
        RepositoryItemReader<DailyTransaction> reader = new RepositoryItemReader<>();
        reader.setRepository(dailyTransactionRepository);
        reader.setMethodName("findByPostedFalse");
        reader.setSort(Map.of("id", Sort.Direction.ASC));
        reader.setArguments(Collections.emptyList());
        reader.setPageSize(10);
        return reader;
    }

    @Bean
    public ItemWriter<Transaction> transactionWriter() {
        JpaItemWriter<Transaction> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}

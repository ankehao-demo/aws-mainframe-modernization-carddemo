package com.carddemo.transactiontype.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch configuration replacing the COBOL batch program COBTUPDT.cbl
 * (JCL job MNTTRDB2).
 *
 * The COBOL batch program reads a sequential flat file where each record has:
 * - Position 1 (1 char): Record type — A (Add), U (Update), D (Delete), or * (comment/skip)
 * - Positions 2-3 (2 chars): Transaction type code (TR_TYPE)
 * - Positions 4-53 (50 chars): Description (TR_DESCRIPTION)
 *
 * This maps to the COBOL structure WS-INPUT-REC with fields INPUT-REC-TYPE,
 * INPUT-REC-NUMBER, and INPUT-REC-DESC.
 */
@Configuration
public class TransactionTypeBatchConfig {

    @Value("${batch.input.file:classpath:batch-input.txt}")
    private Resource inputFile;

    @Bean
    public FlatFileItemReader<TrRecord> transactionTypeReader() {
        return new FlatFileItemReaderBuilder<TrRecord>()
                .name("transactionTypeReader")
                .resource(inputFile)
                .fixedLength()
                .columns(
                        new Range(1, 1),   // Record type (A/U/D/*)
                        new Range(2, 3),   // TR_TYPE (2 chars)
                        new Range(4, 53)   // TR_DESCRIPTION (50 chars)
                )
                .names("recordType", "trType", "trDescription")
                .targetType(TrRecord.class)
                .build();
    }

    @Bean
    public Step processRecords(JobRepository jobRepository,
                               PlatformTransactionManager txManager,
                               FlatFileItemReader<TrRecord> transactionTypeReader,
                               TransactionTypeItemWriter writer) {
        return new StepBuilder("processRecords", jobRepository)
                .<TrRecord, TrRecord>chunk(100, txManager)
                .reader(transactionTypeReader)
                .writer(writer)
                .build();
    }

    @Bean
    public Job maintainTransactionTypes(JobRepository jobRepository, Step processRecords) {
        return new JobBuilder("MNTTRDB2", jobRepository)
                .start(processRecords)
                .build();
    }
}

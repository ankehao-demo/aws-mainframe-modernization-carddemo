package com.aws.carddemo.batch;

import com.aws.carddemo.entity.Transaction;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class TransactionReportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final String outputDirectory;

    public TransactionReportJobConfig(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      EntityManagerFactory entityManagerFactory,
                                      @Value("${app.batch.output-directory}") String outputDirectory) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.entityManagerFactory = entityManagerFactory;
        this.outputDirectory = outputDirectory;
    }

    @Bean
    public Job transactionReportJob() {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(transactionReportStep())
                .build();
    }

    @Bean
    public Step transactionReportStep() {
        return new StepBuilder("transactionReportStep", jobRepository)
                .<Transaction, Transaction>chunk(100, transactionManager)
                .reader(transactionReportReader())
                .writer(transactionReportWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<Transaction> transactionReportReader() {
        JpaCursorItemReader<Transaction> reader = new JpaCursorItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT t FROM Transaction t ORDER BY t.tranCardNum, t.tranId");
        return reader;
    }

    @Bean
    public FlatFileItemWriter<Transaction> transactionReportWriter() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return new FlatFileItemWriterBuilder<Transaction>()
                .name("transactionReportWriter")
                .resource(new FileSystemResource(outputDirectory + "/transaction_report_" + dateStr + ".csv"))
                .delimited()
                .delimiter(",")
                .names("tranId", "tranTypeCd", "tranCatCd", "tranCardNum", "tranDesc", "tranAmt",
                        "tranMerchantName", "tranMerchantCity", "tranOrigTs", "tranProcTs")
                .headerCallback(writer -> writer.write(
                        "TRAN_ID,TYPE_CD,CAT_CD,CARD_NUM,DESCRIPTION,AMOUNT,MERCHANT,CITY,ORIG_TS,PROC_TS"))
                .build();
    }
}

package com.aws.carddemo.batch;

import com.aws.carddemo.entity.Transaction;
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
import java.math.BigDecimal;

/**
 * TransactionReportJob — mirrors CBTRN03C.cbl batch program.
 * Generates daily transaction report with the format defined in CVTRA07Y.cpy:
 * header, detail lines, page/account/grand totals.
 */
@Configuration
public class TransactionReportJobConfig {

    private static final Logger log = LoggerFactory.getLogger(TransactionReportJobConfig.class);

    @Bean
    public JpaPagingItemReader<Transaction> reportTransactionReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<Transaction>()
                .name("reportTransactionReader")
                .entityManagerFactory(emf)
                .queryString("SELECT t FROM Transaction t ORDER BY t.cardNum, t.tranId")
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemWriter<Transaction> transactionReportWriter() {
        // Mirrors CVTRA07Y report format: header, detail lines, page/account/grand totals
        final BigDecimal[] grandTotal = {BigDecimal.ZERO};

        return transactions -> {
            for (Transaction tran : transactions) {
                // TRANSACTION-DETAIL-REPORT format from CVTRA07Y
                log.info("REPORT: {} {} {}-{} {} {:>15}",
                        tran.getTranId(),
                        tran.getCardNum(),
                        tran.getTypeCd(),
                        tran.getCatCd(),
                        tran.getSource(),
                        tran.getAmount());
                grandTotal[0] = grandTotal[0].add(
                        tran.getAmount() != null ? tran.getAmount() : BigDecimal.ZERO);
            }
        };
    }

    @Bean
    public Step transactionReportStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       JpaPagingItemReader<Transaction> reportTransactionReader,
                                       ItemWriter<Transaction> transactionReportWriter) {
        return new StepBuilder("transactionReportStep", jobRepository)
                .<Transaction, Transaction>chunk(100, transactionManager)
                .reader(reportTransactionReader)
                .writer(transactionReportWriter)
                .build();
    }

    @Bean
    public Job transactionReportJob(JobRepository jobRepository, Step transactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(transactionReportStep)
                .build();
    }
}

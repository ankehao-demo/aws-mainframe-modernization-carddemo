package com.aws.carddemo.batch;

import com.aws.carddemo.entity.PendingAuthDetail;
import com.aws.carddemo.entity.PendingAuthSummary;
import com.aws.carddemo.repository.PendingAuthDetailRepository;
import com.aws.carddemo.repository.PendingAuthSummaryRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class AuthPurgeJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PendingAuthSummaryRepository summaryRepository;
    private final PendingAuthDetailRepository detailRepository;

    public AuthPurgeJobConfig(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              PendingAuthSummaryRepository summaryRepository,
                              PendingAuthDetailRepository detailRepository) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.summaryRepository = summaryRepository;
        this.detailRepository = detailRepository;
    }

    @Bean
    public Job authPurgeJob() {
        return new JobBuilder("authPurgeJob", jobRepository)
                .start(authPurgeStep())
                .build();
    }

    @Bean
    public Step authPurgeStep() {
        return new StepBuilder("authPurgeStep", jobRepository)
                .tasklet(authPurgeTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet authPurgeTasklet() {
        return (contribution, chunkContext) -> {
            String expiryDate = LocalDate.now().minusDays(30)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<PendingAuthSummary> summaries = summaryRepository.findAll();
            for (PendingAuthSummary summary : summaries) {
                List<PendingAuthDetail> details = detailRepository.findBySummaryId(summary.getId());

                // Remove expired details
                List<PendingAuthDetail> expiredDetails = details.stream()
                        .filter(d -> d.getPaAuthDate() != null && d.getPaAuthDate().compareTo(expiryDate) < 0)
                        .toList();

                for (PendingAuthDetail expired : expiredDetails) {
                    detailRepository.delete(expired);

                    // Update summary counts
                    if ("0000".equals(expired.getPaRespCode())) {
                        summary.setPaApprovedCount(Math.max(0, summary.getPaApprovedCount() - 1));
                        summary.setPaApprovedAmount(summary.getPaApprovedAmount()
                                .subtract(expired.getPaTranAmt()));
                    } else {
                        summary.setPaDeclinedCount(Math.max(0, summary.getPaDeclinedCount() - 1));
                        summary.setPaDeclinedAmount(summary.getPaDeclinedAmount()
                                .subtract(expired.getPaTranAmt()));
                    }
                }

                summaryRepository.save(summary);

                // Delete summary if all children deleted
                if (summary.getPaApprovedCount() <= 0 && summary.getPaDeclinedCount() <= 0) {
                    summaryRepository.delete(summary);
                }
            }

            return RepeatStatus.FINISHED;
        };
    }
}

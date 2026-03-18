package com.aws.carddemo.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BatchScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job transactionPostingJob;
    private final Job interestCalculationJob;
    private final Job statementGenerationJob;
    private final Job transactionReportJob;
    private final Job authPurgeJob;

    public BatchScheduler(JobLauncher jobLauncher,
                          Job transactionPostingJob,
                          Job interestCalculationJob,
                          Job statementGenerationJob,
                          Job transactionReportJob,
                          Job authPurgeJob) {
        this.jobLauncher = jobLauncher;
        this.transactionPostingJob = transactionPostingJob;
        this.interestCalculationJob = interestCalculationJob;
        this.statementGenerationJob = statementGenerationJob;
        this.transactionReportJob = transactionReportJob;
        this.authPurgeJob = authPurgeJob;
    }

    @Scheduled(cron = "${app.batch.cron.nightly-chain}")
    public void runNightlyBatchChain() {
        logger.info("Starting nightly batch chain...");

        try {
            runJob("transactionPostingJob", transactionPostingJob);
            runJob("interestCalculationJob", interestCalculationJob);
            runJob("statementGenerationJob", statementGenerationJob);
            runJob("transactionReportJob", transactionReportJob);
            runJob("authPurgeJob", authPurgeJob);
            logger.info("Nightly batch chain completed successfully.");
        } catch (Exception e) {
            logger.error("Nightly batch chain failed: {}", e.getMessage(), e);
        }
    }

    private void runJob(String jobName, Job job) throws Exception {
        logger.info("Starting job: {}", jobName);
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(job, params);
        logger.info("Completed job: {}", jobName);
    }
}

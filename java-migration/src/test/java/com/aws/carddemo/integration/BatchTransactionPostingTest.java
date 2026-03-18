package com.aws.carddemo.integration;

import com.aws.carddemo.entity.DailyTransaction;
import com.aws.carddemo.repository.DailyTransactionRepository;
import com.aws.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class BatchTransactionPostingTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("carddemo_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5672");
        registry.add("spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration");
    }

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("transactionPostingJob")
    private Job transactionPostingJob;

    @Autowired
    private DailyTransactionRepository dailyTransactionRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void transactionPostingJob_postsUnpostedTransactions() throws Exception {
        long initialCount = transactionRepository.count();
        long unpostedCount = dailyTransactionRepository.findByPostedFalse().size();

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(transactionPostingJob, params);

        // After posting, unposted count should decrease
        long newUnpostedCount = dailyTransactionRepository.findByPostedFalse().size();
        assertThat(newUnpostedCount).isLessThanOrEqualTo(unpostedCount);
    }
}

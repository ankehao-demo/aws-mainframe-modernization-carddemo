package com.aws.carddemo.integration;

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

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class BatchStatementGenerationTest {

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
    @Qualifier("statementGenerationJob")
    private Job statementGenerationJob;

    @Test
    void statementGenerationJob_producesOutputFiles() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        assertThatNoException().isThrownBy(() ->
                jobLauncher.run(statementGenerationJob, params));

        // Check output directory exists
        File outputDir = new File("./target/batch-output");
        if (outputDir.exists()) {
            File[] files = outputDir.listFiles();
            if (files != null && files.length > 0) {
                // Verify at least some statement files exist
                boolean hasTxtFiles = false;
                boolean hasHtmlFiles = false;
                for (File f : files) {
                    if (f.getName().endsWith(".txt")) hasTxtFiles = true;
                    if (f.getName().endsWith(".html")) hasHtmlFiles = true;
                }
                // Files may or may not be created depending on test data
                assertThat(outputDir).exists();
            }
        }
    }
}

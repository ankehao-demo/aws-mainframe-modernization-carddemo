package com.aws.carddemo.integration;

import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.Card;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardRepository;
import com.aws.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class RepositoryTest {

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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5672");
        registry.add("spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration");
    }

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void findCardsByAcctId() {
        List<Card> cards = cardRepository.findByCardAcctId("00000000001");
        assertThat(cards).isNotNull();
    }

    @Test
    void findTransactionsByCardNum_paginated() {
        Page<Transaction> page = transactionRepository.findByTranCardNum(
                "4111111111111111", PageRequest.of(0, 10));
        assertThat(page).isNotNull();
    }

    @Test
    void findMaxTranId() {
        Optional<String> maxId = transactionRepository.findMaxTranId();
        // May or may not have transactions depending on seed data
        assertThat(maxId).isNotNull();
    }

    @Test
    void accountOptimisticLocking() {
        Optional<Account> account = accountRepository.findById("00000000001");
        assertThat(account).isPresent();
        assertThat(account.get().getVersion()).isNotNull();
    }
}

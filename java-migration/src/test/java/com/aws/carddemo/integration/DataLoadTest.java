package com.aws.carddemo.integration;

import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.Card;
import com.aws.carddemo.entity.CardXref;
import com.aws.carddemo.entity.Customer;
import com.aws.carddemo.entity.TransactionType;
import com.aws.carddemo.entity.UserSecurity;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.CustomerRepository;
import com.aws.carddemo.repository.TransactionTypeRepository;
import com.aws.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class DataLoadTest {

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
    private CustomerRepository customerRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    // TC-DL-001: Verify account records loaded
    @Test
    void verifyAccountRecordsLoaded() {
        long count = accountRepository.count();
        assertThat(count).isGreaterThan(0);
    }

    // TC-DL-002: Verify card records loaded
    @Test
    void verifyCardRecordsLoaded() {
        long count = cardRepository.count();
        assertThat(count).isGreaterThan(0);
    }

    // TC-DL-003: Verify customer records loaded
    @Test
    void verifyCustomerRecordsLoaded() {
        long count = customerRepository.count();
        assertThat(count).isGreaterThan(0);
    }

    // TC-DL-004: Verify card_xref records loaded
    @Test
    void verifyCardXrefRecordsLoaded() {
        long count = cardXrefRepository.count();
        assertThat(count).isGreaterThan(0);
    }

    // TC-DL-005: Verify user security records loaded
    @Test
    void verifyUserSecurityRecordsLoaded() {
        long count = userSecurityRepository.count();
        assertThat(count).isGreaterThan(0);
    }

    // TC-DL-006: Verify account field values
    @Test
    void verifyAccountFieldValues() {
        Optional<Account> account = accountRepository.findById("00000000001");
        assertThat(account).isPresent();
        assertThat(account.get().getAcctActiveStatus()).isEqualTo("Y");
        assertThat(account.get().getAcctCreditLimit()).isNotNull();
    }

    // TC-DL-007: Verify secondary index on card_xref by acct_id
    @Test
    void verifySecondaryIndexOnCardXref() {
        List<CardXref> xrefs = cardXrefRepository.findByXrefAcctId("00000000001");
        assertThat(xrefs).isNotEmpty();
    }

    // TC-DL-008: Verify user security data
    @Test
    void verifyUserSecurityData() {
        Optional<UserSecurity> admin = userSecurityRepository.findById("ADMIN001");
        assertThat(admin).isPresent();
        assertThat(admin.get().getUsrType()).isEqualTo("A");
    }

    // TC-DL-009: Verify transaction type records
    @Test
    void verifyTransactionTypeRecords() {
        long count = transactionTypeRepository.count();
        assertThat(count).isGreaterThan(0);
    }

    // TC-DL-010: Verify decimal precision preserved
    @Test
    void verifyDecimalPrecision() {
        Optional<Account> account = accountRepository.findById("00000000001");
        assertThat(account).isPresent();
        assertThat(account.get().getAcctCurrBal().scale()).isGreaterThanOrEqualTo(2);
    }
}

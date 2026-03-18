package com.aws.carddemo.integration;

import com.aws.carddemo.dto.AccountDto;
import com.aws.carddemo.dto.LoginRequest;
import com.aws.carddemo.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountIntegrationTest {

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
    private TestRestTemplate restTemplate;

    private String authToken;

    @BeforeEach
    void setUp() {
        LoginRequest loginRequest = LoginRequest.builder()
                .userId("USER0001")
                .password("USER0001")
                .build();

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, LoginResponse.class);

        assertThat(loginResponse.getBody()).isNotNull();
        authToken = loginResponse.getBody().getToken();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return headers;
    }

    @Test
    void getAccount_found_returnsAllFields() {
        ResponseEntity<AccountDto> response = restTemplate.exchange(
                "/api/accounts/00000000001", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), AccountDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAcctId()).isEqualTo("00000000001");
        assertThat(response.getBody().getAcctActiveStatus()).isEqualTo("Y");
    }

    @Test
    void getAccount_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/accounts/99999999999", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateAccount_success() {
        AccountDto updateDto = AccountDto.builder()
                .acctActiveStatus("N")
                .build();

        ResponseEntity<AccountDto> response = restTemplate.exchange(
                "/api/accounts/00000000001", HttpMethod.PUT,
                new HttpEntity<>(updateDto, authHeaders()), AccountDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Reset back
        AccountDto resetDto = AccountDto.builder()
                .acctActiveStatus("Y")
                .build();
        restTemplate.exchange("/api/accounts/00000000001", HttpMethod.PUT,
                new HttpEntity<>(resetDto, authHeaders()), AccountDto.class);
    }
}

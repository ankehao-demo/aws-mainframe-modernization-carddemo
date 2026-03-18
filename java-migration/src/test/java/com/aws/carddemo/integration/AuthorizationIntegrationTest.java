package com.aws.carddemo.integration;

import com.aws.carddemo.dto.AuthorizationRequest;
import com.aws.carddemo.dto.AuthorizationResponse;
import com.aws.carddemo.dto.LoginRequest;
import com.aws.carddemo.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthorizationIntegrationTest {

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
        headers.set("Content-Type", "application/json");
        return headers;
    }

    @Test
    void processAuthorization_approved() {
        AuthorizationRequest request = AuthorizationRequest.builder()
                .cardNum("4111111111111111")
                .amount(new BigDecimal("100.00"))
                .merchantId("MERCH001")
                .merchantName("TEST STORE")
                .merchantCity("NEW YORK")
                .merchantZip("10001")
                .build();

        ResponseEntity<AuthorizationResponse> response = restTemplate.exchange(
                "/api/authorization", org.springframework.http.HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()), AuthorizationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResponseCode()).isIn("0000", "0051");
    }

    @Test
    void processAuthorization_cardNotFound_returns404() {
        AuthorizationRequest request = AuthorizationRequest.builder()
                .cardNum("0000000000000000")
                .amount(new BigDecimal("100.00"))
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/authorization", org.springframework.http.HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

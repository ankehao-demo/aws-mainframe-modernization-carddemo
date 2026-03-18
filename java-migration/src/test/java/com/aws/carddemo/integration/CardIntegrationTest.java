package com.aws.carddemo.integration;

import com.aws.carddemo.dto.CardDto;
import com.aws.carddemo.dto.LoginRequest;
import com.aws.carddemo.dto.LoginResponse;
import com.aws.carddemo.dto.PagedResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CardIntegrationTest {

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
    void listCards_returnsPaginated() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/cards?page=0&size=10", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("content")).isNotNull();
    }

    @Test
    void getCardsByAccount_returnsList() {
        ResponseEntity<CardDto[]> response = restTemplate.exchange(
                "/api/cards/by-account/00000000001", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), CardDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getCard_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/cards/0000000000000000", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

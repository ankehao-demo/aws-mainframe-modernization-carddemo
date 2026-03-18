package com.aws.carddemo.integration;

import com.aws.carddemo.dto.LoginRequest;
import com.aws.carddemo.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
class AuthIntegrationTest {

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

    @Test
    void login_validAdmin_returnsToken() {
        LoginRequest request = LoginRequest.builder()
                .userId("ADMIN001")
                .password("ADMIN001")
                .build();

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login", request, LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getUserType()).isEqualTo("ADMIN");
    }

    @Test
    void login_validUser_returnsToken() {
        LoginRequest request = LoginRequest.builder()
                .userId("USER0001")
                .password("USER0001")
                .build();

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login", request, LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserType()).isEqualTo("USER");
    }

    @Test
    void login_wrongPassword_returns401() {
        LoginRequest request = LoginRequest.builder()
                .userId("ADMIN001")
                .password("WRONGPWD")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Wrong Password. Try again ...");
    }

    @Test
    void login_userNotFound_returns401() {
        LoginRequest request = LoginRequest.builder()
                .userId("UNKNOWN1")
                .password("PASSWORD")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("User not found. Try again ...");
    }

    @Test
    void login_caseInsensitive_uppercased() {
        LoginRequest request = LoginRequest.builder()
                .userId("admin001")
                .password("admin001")
                .build();

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/auth/login", request, LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
    }
}

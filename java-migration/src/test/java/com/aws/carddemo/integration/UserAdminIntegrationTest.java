package com.aws.carddemo.integration;

import com.aws.carddemo.dto.LoginRequest;
import com.aws.carddemo.dto.LoginResponse;
import com.aws.carddemo.dto.UserDto;
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
class UserAdminIntegrationTest {

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

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // Login as admin
        LoginRequest adminLogin = LoginRequest.builder()
                .userId("ADMIN001")
                .password("ADMIN001")
                .build();
        ResponseEntity<LoginResponse> adminResponse = restTemplate.postForEntity(
                "/api/auth/login", adminLogin, LoginResponse.class);
        assertThat(adminResponse.getBody()).isNotNull();
        adminToken = adminResponse.getBody().getToken();

        // Login as regular user
        LoginRequest userLogin = LoginRequest.builder()
                .userId("USER0001")
                .password("USER0001")
                .build();
        ResponseEntity<LoginResponse> userResponse = restTemplate.postForEntity(
                "/api/auth/login", userLogin, LoginResponse.class);
        assertThat(userResponse.getBody()).isNotNull();
        userToken = userResponse.getBody().getToken();
    }

    private HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private HttpHeaders userHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    @Test
    void listUsers_asAdmin_returns200() {
        ResponseEntity<UserDto[]> response = restTemplate.exchange(
                "/api/admin/users", HttpMethod.GET,
                new HttpEntity<>(adminHeaders()), UserDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    void listUsers_asRegularUser_returns403() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/admin/users", HttpMethod.GET,
                new HttpEntity<>(userHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteUser_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/admin/users/XXXXXXXX", HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

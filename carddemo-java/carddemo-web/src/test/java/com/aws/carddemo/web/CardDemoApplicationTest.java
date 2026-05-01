package com.aws.carddemo.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class CardDemoApplicationTest {

    @Test
    void contextLoads() {
        // Verifies Spring context starts successfully with H2 and Flyway migrations run
    }
}

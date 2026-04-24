package com.carddemo.cardservice.integration;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Stub implementation for the transition period.
 * Returns empty results until the customer service is modernized or
 * an anti-corruption layer to the mainframe is configured.
 */
@Component
public class StubCustomerServiceClient implements CustomerServiceClient {

    @Override
    public Optional<CustomerInfo> getCustomer(String customerId) {
        return Optional.empty();
    }
}

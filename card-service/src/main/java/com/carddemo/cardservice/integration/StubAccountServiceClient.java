package com.carddemo.cardservice.integration;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Stub implementation for the transition period.
 * Returns empty results until the account service is modernized or
 * an anti-corruption layer to the mainframe is configured.
 */
@Component
public class StubAccountServiceClient implements AccountServiceClient {

    @Override
    public Optional<AccountInfo> getAccount(String accountId) {
        return Optional.empty();
    }
}

package com.carddemo.cardservice.integration;

import java.util.Optional;

/**
 * Interface for account lookups during the transition period.
 * The COBOL card programs read ACCTDAT VSAM via copybook CVACT01Y.
 * Implementations may call the legacy mainframe (via anti-corruption layer)
 * or a modernized account microservice.
 */
public interface AccountServiceClient {

    Optional<AccountInfo> getAccount(String accountId);

    record AccountInfo(
            String acctId,
            String acctActiveStatus,
            String acctCurrBal,
            String acctCreditLimit,
            String acctOpenDate,
            String acctExpirationDate
    ) {
    }
}

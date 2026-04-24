package com.carddemo.cardservice.integration;

import java.util.Optional;

/**
 * Interface for customer lookups during the transition period.
 * The COBOL card programs access CUSTDAT VSAM via copybook CVCUS01Y.
 * Implementations may call the legacy mainframe or a modernized customer microservice.
 */
public interface CustomerServiceClient {

    Optional<CustomerInfo> getCustomer(String customerId);

    record CustomerInfo(
            String custId,
            String custFirstName,
            String custMiddleName,
            String custLastName,
            String custSsn,
            String custCreditScore
    ) {
    }
}

package com.carddemo.cardservice.validation;

import com.carddemo.cardservice.exception.CardValidationException;
import com.carddemo.cardservice.model.dto.CardUpdateRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation rules ported from COCRDUPC.cbl paragraphs 1210-1260.
 * Each method corresponds to a COBOL validation paragraph.
 */
@Component
public class CardValidator {

    /**
     * Validates all fields of a card update request.
     * Collects all validation errors before throwing.
     */
    public void validateCardUpdate(CardUpdateRequest request) {
        List<String> errors = new ArrayList<>();

        validateAccountId(request.cardAcctId(), errors);
        validateCardName(request.cardEmbossedName(), errors);
        validateCardActiveStatus(request.cardActiveStatus(), errors);
        validateExpirationDate(request.cardExpirationDate(), errors);

        if (!errors.isEmpty()) {
            throw new CardValidationException(String.join("; ", errors));
        }
    }

    /**
     * From paragraph 1210-EDIT-ACCOUNT in COCRDUPC.cbl:
     * - Must be numeric
     * - Must be 11 digits
     * - Must not be zero
     */
    public void validateAccountId(String accountId, List<String> errors) {
        if (accountId == null || accountId.isBlank()) {
            errors.add("Account ID is required");
            return;
        }
        if (!accountId.matches("\\d{11}")) {
            errors.add("Account ID must be an 11-digit number");
            return;
        }
        if (accountId.matches("0{11}")) {
            errors.add("Account ID must not be zero");
        }
    }

    /**
     * Validates a 16-digit card number.
     * From paragraph 1220-EDIT-CARD in COCRDUPC.cbl:
     * - Must be numeric
     * - Must be 16 digits
     * - Must not be zero
     */
    public void validateCardNumber(String cardNum, List<String> errors) {
        if (cardNum == null || cardNum.isBlank()) {
            errors.add("Card number is required");
            return;
        }
        if (!cardNum.matches("\\d{16}")) {
            errors.add("Card number must be a 16-digit number");
            return;
        }
        if (cardNum.matches("0{16}")) {
            errors.add("Card number must not be zero");
        }
    }

    /**
     * From paragraph 1230-EDIT-NAME in COCRDUPC.cbl:
     * - Must not be blank
     * - Must contain only alphabets and spaces
     * The COBOL uses INSPECT CONVERTING to strip alphabets, then checks if anything remains.
     */
    public void validateCardName(String name, List<String> errors) {
        if (name == null || name.isBlank()) {
            errors.add("Card embossed name is required");
            return;
        }
        if (!name.matches("[A-Za-z ]+")) {
            errors.add("Card embossed name must contain only alphabets and spaces");
        }
    }

    /**
     * From paragraph 1240-EDIT-CARDSTATUS in COCRDUPC.cbl:
     * - Must be 'Y' or 'N'
     */
    public void validateCardActiveStatus(String status, List<String> errors) {
        if (status == null || status.isBlank()) {
            errors.add("Card active status is required");
            return;
        }
        if (!"Y".equals(status) && !"N".equals(status)) {
            errors.add("Card active status must be 'Y' or 'N'");
        }
    }

    /**
     * Validates expiration date in MM/YYYY format.
     * From paragraphs 1250-EDIT-EXPIRY-MON and 1260-EDIT-EXPIRY-YEAR:
     * - Month must be 1-12 (VALID-MONTH VALUES 1 THRU 12)
     * - Year must be 1950-2099 (VALID-YEAR VALUES 1950 THRU 2099)
     */
    public void validateExpirationDate(String expirationDate, List<String> errors) {
        if (expirationDate == null || expirationDate.isBlank()) {
            errors.add("Card expiration date is required");
            return;
        }
        if (!expirationDate.matches("\\d{2}/\\d{4}")) {
            errors.add("Card expiration date must be in MM/YYYY format");
            return;
        }
        String[] parts = expirationDate.split("/");
        int month;
        int year;
        try {
            month = Integer.parseInt(parts[0]);
            year = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            errors.add("Card expiration date must contain valid numbers");
            return;
        }

        if (month < 1 || month > 12) {
            errors.add("Expiry month must be between 1 and 12");
        }
        if (year < 1950 || year > 2099) {
            errors.add("Expiry year must be between 1950 and 2099");
        }
    }
}

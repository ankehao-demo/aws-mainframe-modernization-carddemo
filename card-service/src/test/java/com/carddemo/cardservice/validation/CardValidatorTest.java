package com.carddemo.cardservice.validation;

import com.carddemo.cardservice.exception.CardValidationException;
import com.carddemo.cardservice.model.dto.CardUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for validation rules ported from COCRDUPC.cbl paragraphs 1210-1260.
 */
class CardValidatorTest {

    private CardValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CardValidator();
    }

    @Nested
    @DisplayName("1210-EDIT-ACCOUNT: Account ID validation")
    class AccountIdValidation {

        @Test
        @DisplayName("Valid 11-digit account ID")
        void validAccountId() {
            List<String> errors = new ArrayList<>();
            validator.validateAccountId("00000000001", errors);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Null account ID")
        void nullAccountId() {
            List<String> errors = new ArrayList<>();
            validator.validateAccountId(null, errors);
            assertFalse(errors.isEmpty());
        }

        @Test
        @DisplayName("Blank account ID")
        void blankAccountId() {
            List<String> errors = new ArrayList<>();
            validator.validateAccountId("   ", errors);
            assertFalse(errors.isEmpty());
        }

        @Test
        @DisplayName("Non-numeric account ID")
        void nonNumericAccountId() {
            List<String> errors = new ArrayList<>();
            validator.validateAccountId("0000000000A", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("11-digit"));
        }

        @Test
        @DisplayName("Too short account ID")
        void tooShortAccountId() {
            List<String> errors = new ArrayList<>();
            validator.validateAccountId("12345", errors);
            assertEquals(1, errors.size());
        }

        @Test
        @DisplayName("All zeros account ID")
        void allZerosAccountId() {
            List<String> errors = new ArrayList<>();
            validator.validateAccountId("00000000000", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("not be zero"));
        }
    }

    @Nested
    @DisplayName("1220-EDIT-CARD: Card number validation")
    class CardNumberValidation {

        @Test
        @DisplayName("Valid 16-digit card number")
        void validCardNumber() {
            List<String> errors = new ArrayList<>();
            validator.validateCardNumber("4000000000000001", errors);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Null card number")
        void nullCardNumber() {
            List<String> errors = new ArrayList<>();
            validator.validateCardNumber(null, errors);
            assertFalse(errors.isEmpty());
        }

        @Test
        @DisplayName("Non-numeric card number")
        void nonNumericCardNumber() {
            List<String> errors = new ArrayList<>();
            validator.validateCardNumber("400000000000000A", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("16-digit"));
        }

        @Test
        @DisplayName("All zeros card number")
        void allZerosCardNumber() {
            List<String> errors = new ArrayList<>();
            validator.validateCardNumber("0000000000000000", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("not be zero"));
        }

        @Test
        @DisplayName("Too short card number")
        void tooShortCardNumber() {
            List<String> errors = new ArrayList<>();
            validator.validateCardNumber("400000", errors);
            assertEquals(1, errors.size());
        }
    }

    @Nested
    @DisplayName("1230-EDIT-NAME: Card name validation")
    class CardNameValidation {

        @Test
        @DisplayName("Valid alphabetic name with spaces")
        void validName() {
            List<String> errors = new ArrayList<>();
            validator.validateCardName("JOHN DOE", errors);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Null name")
        void nullName() {
            List<String> errors = new ArrayList<>();
            validator.validateCardName(null, errors);
            assertFalse(errors.isEmpty());
        }

        @Test
        @DisplayName("Blank name")
        void blankName() {
            List<String> errors = new ArrayList<>();
            validator.validateCardName("   ", errors);
            assertFalse(errors.isEmpty());
        }

        @Test
        @DisplayName("Name with numbers")
        void nameWithNumbers() {
            List<String> errors = new ArrayList<>();
            validator.validateCardName("JOHN DOE 3RD", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("alphabets and spaces"));
        }

        @Test
        @DisplayName("Name with special characters")
        void nameWithSpecialChars() {
            List<String> errors = new ArrayList<>();
            validator.validateCardName("O'BRIEN", errors);
            assertEquals(1, errors.size());
        }
    }

    @Nested
    @DisplayName("1240-EDIT-CARDSTATUS: Card active status validation")
    class CardStatusValidation {

        @Test
        @DisplayName("Valid status Y")
        void validStatusY() {
            List<String> errors = new ArrayList<>();
            validator.validateCardActiveStatus("Y", errors);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Valid status N")
        void validStatusN() {
            List<String> errors = new ArrayList<>();
            validator.validateCardActiveStatus("N", errors);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Invalid status")
        void invalidStatus() {
            List<String> errors = new ArrayList<>();
            validator.validateCardActiveStatus("X", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("'Y' or 'N'"));
        }

        @Test
        @DisplayName("Null status")
        void nullStatus() {
            List<String> errors = new ArrayList<>();
            validator.validateCardActiveStatus(null, errors);
            assertFalse(errors.isEmpty());
        }
    }

    @Nested
    @DisplayName("1250/1260-EDIT-EXPIRY: Expiration date validation")
    class ExpirationDateValidation {

        @Test
        @DisplayName("Valid expiration date")
        void validDate() {
            List<String> errors = new ArrayList<>();
            validator.validateExpirationDate("12/2028", errors);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Month below range")
        void monthBelowRange() {
            List<String> errors = new ArrayList<>();
            validator.validateExpirationDate("00/2028", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("month"));
        }

        @Test
        @DisplayName("Month above range")
        void monthAboveRange() {
            List<String> errors = new ArrayList<>();
            validator.validateExpirationDate("13/2028", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("month"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"01/1950", "06/2000", "12/2099"})
        @DisplayName("Valid year boundaries")
        void validYearBoundaries(String date) {
            List<String> errors = new ArrayList<>();
            validator.validateExpirationDate(date, errors);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Year below range")
        void yearBelowRange() {
            List<String> errors = new ArrayList<>();
            validator.validateExpirationDate("06/1949", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("year"));
        }

        @Test
        @DisplayName("Year above range")
        void yearAboveRange() {
            List<String> errors = new ArrayList<>();
            validator.validateExpirationDate("06/2100", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("year"));
        }

        @Test
        @DisplayName("Invalid format")
        void invalidFormat() {
            List<String> errors = new ArrayList<>();
            validator.validateExpirationDate("2028-12", errors);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains("MM/YYYY"));
        }

        @Test
        @DisplayName("Null expiration date")
        void nullDate() {
            List<String> errors = new ArrayList<>();
            validator.validateExpirationDate(null, errors);
            assertFalse(errors.isEmpty());
        }
    }

    @Nested
    @DisplayName("Full update validation")
    class FullUpdateValidation {

        @Test
        @DisplayName("Valid full update request")
        void validUpdateRequest() {
            CardUpdateRequest request = new CardUpdateRequest(
                    "00000000001", "JOHN DOE", "Y", "12/2028", 0L);
            assertDoesNotThrow(() -> validator.validateCardUpdate(request));
        }

        @Test
        @DisplayName("Multiple validation errors collected")
        void multipleErrors() {
            CardUpdateRequest request = new CardUpdateRequest(
                    "abc", "J0HN", "X", "13/2100", 0L);
            CardValidationException ex = assertThrows(
                    CardValidationException.class,
                    () -> validator.validateCardUpdate(request));
            String msg = ex.getMessage();
            assertTrue(msg.contains("Account ID"));
            assertTrue(msg.contains("alphabets"));
            assertTrue(msg.contains("'Y' or 'N'"));
        }
    }
}

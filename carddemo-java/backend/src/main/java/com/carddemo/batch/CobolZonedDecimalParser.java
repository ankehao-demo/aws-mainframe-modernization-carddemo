package com.carddemo.batch;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Parses COBOL zoned decimal encoding where the last digit encodes the sign.
 * Positive: { = +0, A = +1, B = +2, C = +3, D = +4, E = +5, F = +6, G = +7, H = +8, I = +9
 * Negative: } = -0, J = -1, K = -2, L = -3, M = -4, N = -5, O = -6, P = -7, Q = -8, R = -9
 */
public class CobolZonedDecimalParser {

    private static final String POSITIVE_CHARS = "{ABCDEFGHI";
    private static final String NEGATIVE_CHARS = "}JKLMNOPQR";

    private CobolZonedDecimalParser() {
    }

    /**
     * Parse a COBOL zoned decimal string into a long value.
     * The last character encodes the sign and last digit.
     */
    public static long parseLong(String raw) {
        if (raw == null || raw.isEmpty()) {
            return 0L;
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return 0L;
        }

        char lastChar = trimmed.charAt(trimmed.length() - 1);
        String prefix = trimmed.substring(0, trimmed.length() - 1);

        int positiveIndex = POSITIVE_CHARS.indexOf(lastChar);
        if (positiveIndex >= 0) {
            String numStr = prefix + positiveIndex;
            return Long.parseLong(numStr);
        }

        int negativeIndex = NEGATIVE_CHARS.indexOf(lastChar);
        if (negativeIndex >= 0) {
            String numStr = prefix + negativeIndex;
            long value = Long.parseLong(numStr);
            return -value;
        }

        // If the last character is a regular digit, just parse normally
        if (Character.isDigit(lastChar)) {
            return Long.parseLong(trimmed);
        }

        throw new IllegalArgumentException("Invalid zoned decimal character: " + lastChar + " in value: " + raw);
    }

    /**
     * Parse a COBOL zoned decimal string with implied decimal places.
     * For PIC S9(10)V99, decimalPlaces = 2.
     */
    public static BigDecimal parseBigDecimal(String raw, int decimalPlaces) {
        long longValue = parseLong(raw);
        return BigDecimal.valueOf(longValue, decimalPlaces);
    }

    /**
     * Parse a COBOL PIC S9(10)V99 field (implied 2 decimal places) into BigDecimal.
     */
    public static BigDecimal parseMoney(String raw) {
        return parseBigDecimal(raw, 2);
    }

    /**
     * Parse a COBOL PIC S9(04)V99 field (implied 2 decimal places) into BigDecimal.
     */
    public static BigDecimal parseRate(String raw) {
        return parseBigDecimal(raw, 2);
    }
}

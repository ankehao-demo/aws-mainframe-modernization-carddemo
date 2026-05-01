package com.aws.carddemo.migration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Parses fixed-width record fields from legacy ASCII data files.
 * Handles EBCDIC sign-overpunch encoding for signed numeric fields.
 */
public class FixedWidthParser {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private FixedWidthParser() {
    }

    /**
     * Extracts and trims a string field from a fixed-width record.
     *
     * @param record the full record string
     * @param start  0-based start position
     * @param length field length
     * @return trimmed string value, or null if the field is blank
     */
    public static String parseString(String record, int start, int length) {
        if (record == null || start + length > record.length()) {
            return null;
        }
        String value = record.substring(start, start + length).trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * Extracts an unsigned numeric field as a long.
     *
     * @param record the full record string
     * @param start  0-based start position
     * @param length field length
     * @return parsed long value, or null if the field is blank or non-numeric
     */
    public static Long parseLong(String record, int start, int length) {
        String value = parseString(record, start, length);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Extracts a signed decimal field with EBCDIC sign-overpunch handling.
     * <p>
     * In ASCII data files converted from EBCDIC, signed numeric fields use
     * overpunch encoding where the last character encodes both a digit and the sign:
     * <ul>
     *   <li>Positive: { = +0, A = +1, B = +2, C = +3, D = +4, E = +5, F = +6, G = +7, H = +8, I = +9</li>
     *   <li>Negative: } = -0, J = -1, K = -2, L = -3, M = -4, N = -5, O = -6, P = -7, Q = -8, R = -9</li>
     * </ul>
     *
     * @param record the full record string
     * @param start  0-based start position
     * @param length field length (including the overpunch character)
     * @param scale  number of implied decimal places
     * @return parsed BigDecimal value, or null if the field is blank
     */
    public static BigDecimal parseSignedDecimal(String record, int start, int length, int scale) {
        if (record == null || start + length > record.length()) {
            return null;
        }
        String raw = record.substring(start, start + length);
        if (raw.isBlank()) {
            return null;
        }

        String digits = raw.substring(0, raw.length() - 1);
        char lastChar = raw.charAt(raw.length() - 1);

        int lastDigit;
        boolean negative;

        switch (lastChar) {
            case '{' -> { lastDigit = 0; negative = false; }
            case 'A' -> { lastDigit = 1; negative = false; }
            case 'B' -> { lastDigit = 2; negative = false; }
            case 'C' -> { lastDigit = 3; negative = false; }
            case 'D' -> { lastDigit = 4; negative = false; }
            case 'E' -> { lastDigit = 5; negative = false; }
            case 'F' -> { lastDigit = 6; negative = false; }
            case 'G' -> { lastDigit = 7; negative = false; }
            case 'H' -> { lastDigit = 8; negative = false; }
            case 'I' -> { lastDigit = 9; negative = false; }
            case '}' -> { lastDigit = 0; negative = true; }
            case 'J' -> { lastDigit = 1; negative = true; }
            case 'K' -> { lastDigit = 2; negative = true; }
            case 'L' -> { lastDigit = 3; negative = true; }
            case 'M' -> { lastDigit = 4; negative = true; }
            case 'N' -> { lastDigit = 5; negative = true; }
            case 'O' -> { lastDigit = 6; negative = true; }
            case 'P' -> { lastDigit = 7; negative = true; }
            case 'Q' -> { lastDigit = 8; negative = true; }
            case 'R' -> { lastDigit = 9; negative = true; }
            default -> {
                // No overpunch — treat last char as a regular digit
                try {
                    long numericValue = Long.parseLong(raw);
                    return BigDecimal.valueOf(numericValue, scale);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        try {
            long numericValue = Long.parseLong(digits) * 10 + lastDigit;
            if (negative) {
                numericValue = -numericValue;
            }
            return BigDecimal.valueOf(numericValue, scale);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a date string in YYYY-MM-DD format.
     *
     * @param record the full record string
     * @param start  0-based start position
     * @param length field length (typically 10)
     * @return parsed LocalDate, or null if blank or invalid
     */
    public static LocalDate parseDate(String record, int start, int length) {
        String value = parseString(record, start, length);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a timestamp string in YYYY-MM-DD HH:mm:ss.SSSSSS format.
     *
     * @param record the full record string
     * @param start  0-based start position
     * @param length field length (typically 26)
     * @return parsed LocalDateTime, or null if blank or invalid
     */
    public static LocalDateTime parseTimestamp(String record, int start, int length) {
        String value = parseString(record, start, length);
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, TIMESTAMP_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses an unsigned integer field.
     *
     * @param record the full record string
     * @param start  0-based start position
     * @param length field length
     * @return parsed Integer value, or null if the field is blank
     */
    public static Integer parseInt(String record, int start, int length) {
        String value = parseString(record, start, length);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

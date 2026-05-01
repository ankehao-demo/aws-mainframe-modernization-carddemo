package com.aws.carddemo.migration;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FixedWidthParserTest {

    @Test
    void parseString_extractsAndTrims() {
        String record = "Hello     World     ";
        assertEquals("Hello", FixedWidthParser.parseString(record, 0, 10));
        assertEquals("World", FixedWidthParser.parseString(record, 10, 10));
    }

    @Test
    void parseString_returnsNullForBlank() {
        String record = "          ";
        assertNull(FixedWidthParser.parseString(record, 0, 10));
    }

    @Test
    void parseString_returnsNullWhenRecordTooShort() {
        assertNull(FixedWidthParser.parseString("short", 0, 10));
        assertNull(FixedWidthParser.parseString(null, 0, 5));
    }

    @Test
    void parseLong_parsesUnsignedNumber() {
        String record = "00000000001";
        assertEquals(1L, FixedWidthParser.parseLong(record, 0, 11));
    }

    @Test
    void parseLong_returnsNullForBlanks() {
        assertNull(FixedWidthParser.parseLong("           ", 0, 11));
    }

    @Test
    void parseSignedDecimal_positiveWithBrace() {
        // 00000001940{ means +19400 with scale 2 = 194.00
        assertEquals(new BigDecimal("194.00"),
                FixedWidthParser.parseSignedDecimal("00000001940{", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_positiveWithLetterA() {
        // 00000001940A means +19401 with scale 2 = 194.01
        assertEquals(new BigDecimal("194.01"),
                FixedWidthParser.parseSignedDecimal("00000001940A", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_positiveWithLetterI() {
        // 00000001940I means +19409 with scale 2 = 194.09
        assertEquals(new BigDecimal("194.09"),
                FixedWidthParser.parseSignedDecimal("00000001940I", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_negativeWithBrace() {
        // 00000001940} means -19400 with scale 2 = -194.00
        assertEquals(new BigDecimal("-194.00"),
                FixedWidthParser.parseSignedDecimal("00000001940}", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_negativeWithLetterJ() {
        // 00000001940J means -19401 with scale 2 = -194.01
        assertEquals(new BigDecimal("-194.01"),
                FixedWidthParser.parseSignedDecimal("00000001940J", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_negativeWithLetterR() {
        // 00000001940R means -19409 with scale 2 = -194.09
        assertEquals(new BigDecimal("-194.09"),
                FixedWidthParser.parseSignedDecimal("00000001940R", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_allZeros() {
        assertEquals(new BigDecimal("0.00"),
                FixedWidthParser.parseSignedDecimal("00000000000{", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_negativeZero() {
        assertEquals(new BigDecimal("0.00"),
                FixedWidthParser.parseSignedDecimal("00000000000}", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_returnsNullForBlanks() {
        assertNull(FixedWidthParser.parseSignedDecimal("            ", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_returnsNullForNull() {
        assertNull(FixedWidthParser.parseSignedDecimal(null, 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_largePositiveValue() {
        // 99999999999{ = +999999999990 scale 2 = 9999999999.90
        assertEquals(new BigDecimal("9999999999.90"),
                FixedWidthParser.parseSignedDecimal("99999999999{", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_embeddedInRecord() {
        String record = "00000000001Y00000001940{00000020200{";
        assertEquals(new BigDecimal("194.00"),
                FixedWidthParser.parseSignedDecimal(record, 12, 12, 2));
        assertEquals(new BigDecimal("2020.00"),
                FixedWidthParser.parseSignedDecimal(record, 24, 12, 2));
    }

    @Test
    void parseSignedDecimal_allOverpunchPositiveChars() {
        String prefix = "00000000000";
        assertEquals(new BigDecimal("0.00"), FixedWidthParser.parseSignedDecimal(prefix + "{", 0, 12, 2));
        assertEquals(new BigDecimal("0.01"), FixedWidthParser.parseSignedDecimal(prefix + "A", 0, 12, 2));
        assertEquals(new BigDecimal("0.02"), FixedWidthParser.parseSignedDecimal(prefix + "B", 0, 12, 2));
        assertEquals(new BigDecimal("0.03"), FixedWidthParser.parseSignedDecimal(prefix + "C", 0, 12, 2));
        assertEquals(new BigDecimal("0.04"), FixedWidthParser.parseSignedDecimal(prefix + "D", 0, 12, 2));
        assertEquals(new BigDecimal("0.05"), FixedWidthParser.parseSignedDecimal(prefix + "E", 0, 12, 2));
        assertEquals(new BigDecimal("0.06"), FixedWidthParser.parseSignedDecimal(prefix + "F", 0, 12, 2));
        assertEquals(new BigDecimal("0.07"), FixedWidthParser.parseSignedDecimal(prefix + "G", 0, 12, 2));
        assertEquals(new BigDecimal("0.08"), FixedWidthParser.parseSignedDecimal(prefix + "H", 0, 12, 2));
        assertEquals(new BigDecimal("0.09"), FixedWidthParser.parseSignedDecimal(prefix + "I", 0, 12, 2));
    }

    @Test
    void parseSignedDecimal_allOverpunchNegativeChars() {
        String prefix = "00000000000";
        assertEquals(new BigDecimal("0.00"), FixedWidthParser.parseSignedDecimal(prefix + "}", 0, 12, 2));
        assertEquals(new BigDecimal("-0.01"), FixedWidthParser.parseSignedDecimal(prefix + "J", 0, 12, 2));
        assertEquals(new BigDecimal("-0.02"), FixedWidthParser.parseSignedDecimal(prefix + "K", 0, 12, 2));
        assertEquals(new BigDecimal("-0.03"), FixedWidthParser.parseSignedDecimal(prefix + "L", 0, 12, 2));
        assertEquals(new BigDecimal("-0.04"), FixedWidthParser.parseSignedDecimal(prefix + "M", 0, 12, 2));
        assertEquals(new BigDecimal("-0.05"), FixedWidthParser.parseSignedDecimal(prefix + "N", 0, 12, 2));
        assertEquals(new BigDecimal("-0.06"), FixedWidthParser.parseSignedDecimal(prefix + "O", 0, 12, 2));
        assertEquals(new BigDecimal("-0.07"), FixedWidthParser.parseSignedDecimal(prefix + "P", 0, 12, 2));
        assertEquals(new BigDecimal("-0.08"), FixedWidthParser.parseSignedDecimal(prefix + "Q", 0, 12, 2));
        assertEquals(new BigDecimal("-0.09"), FixedWidthParser.parseSignedDecimal(prefix + "R", 0, 12, 2));
    }

    @Test
    void parseDate_parsesValidDate() {
        assertEquals(LocalDate.of(2023, 3, 9),
                FixedWidthParser.parseDate("2023-03-09", 0, 10));
    }

    @Test
    void parseDate_returnsNullForInvalid() {
        assertNull(FixedWidthParser.parseDate("not-a-date", 0, 10));
    }

    @Test
    void parseDate_returnsNullForBlank() {
        assertNull(FixedWidthParser.parseDate("          ", 0, 10));
    }

    @Test
    void parseTimestamp_parsesValidTimestamp() {
        assertEquals(LocalDateTime.of(2022, 6, 10, 19, 27, 53, 0),
                FixedWidthParser.parseTimestamp("2022-06-10 19:27:53.000000", 0, 26));
    }

    @Test
    void parseTimestamp_returnsNullForBlank() {
        assertNull(FixedWidthParser.parseTimestamp("                          ", 0, 26));
    }

    @Test
    void parseInt_parsesUnsignedInteger() {
        assertEquals(123, FixedWidthParser.parseInt("0123", 0, 4));
    }

    @Test
    void parseInt_returnsNullForBlank() {
        assertNull(FixedWidthParser.parseInt("    ", 0, 4));
    }
}

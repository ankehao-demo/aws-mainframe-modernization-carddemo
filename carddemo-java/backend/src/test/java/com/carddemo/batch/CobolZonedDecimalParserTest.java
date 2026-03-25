package com.carddemo.batch;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CobolZonedDecimalParserTest {

    @Test
    void shouldParsePositiveZero() {
        // { = +0
        assertThat(CobolZonedDecimalParser.parseLong("00000001940{")).isEqualTo(19400L);
    }

    @Test
    void shouldParsePositiveDigits() {
        // A = +1
        assertThat(CobolZonedDecimalParser.parseLong("0000000001A")).isEqualTo(11L);
        // B = +2
        assertThat(CobolZonedDecimalParser.parseLong("0000000001B")).isEqualTo(12L);
        // I = +9
        assertThat(CobolZonedDecimalParser.parseLong("0000000001I")).isEqualTo(19L);
    }

    @Test
    void shouldParseNegativeZero() {
        // } = -0
        assertThat(CobolZonedDecimalParser.parseLong("00000000000}")).isEqualTo(0L);
    }

    @Test
    void shouldParseNegativeDigits() {
        // J = -1
        assertThat(CobolZonedDecimalParser.parseLong("0000000001J")).isEqualTo(-11L);
        // R = -9
        assertThat(CobolZonedDecimalParser.parseLong("0000000001R")).isEqualTo(-19L);
    }

    @Test
    void shouldParseRegularDigit() {
        assertThat(CobolZonedDecimalParser.parseLong("00000019400")).isEqualTo(19400L);
    }

    @Test
    void shouldParseMoney() {
        // 00000001940{ → 19400 → $194.00
        BigDecimal result = CobolZonedDecimalParser.parseMoney("00000001940{");
        assertThat(result).isEqualByComparingTo(new BigDecimal("194.00"));
    }

    @Test
    void shouldParseMoneyWithPositiveSign() {
        // 00000020200{ → 202000 → $2020.00
        BigDecimal result = CobolZonedDecimalParser.parseMoney("00000020200{");
        assertThat(result).isEqualByComparingTo(new BigDecimal("2020.00"));
    }

    @Test
    void shouldParseDailyTransactionAmount() {
        // 0000005047G → 50477 → $504.77
        BigDecimal result = CobolZonedDecimalParser.parseBigDecimal("0000005047G", 2);
        assertThat(result).isEqualByComparingTo(new BigDecimal("504.77"));
    }

    @Test
    void shouldParseNegativeAmount() {
        // 0000009190} → } = -0, so value = -91900 → -$919.00
        BigDecimal result = CobolZonedDecimalParser.parseBigDecimal("0000009190}", 2);
        assertThat(result).isEqualByComparingTo(new BigDecimal("-919.00"));
    }

    @Test
    void shouldParseNegativeNonZero() {
        // 0000009190J → -91901 → -$919.01
        BigDecimal result = CobolZonedDecimalParser.parseBigDecimal("0000009190J", 2);
        assertThat(result).isEqualByComparingTo(new BigDecimal("-919.01"));
    }

    @Test
    void shouldHandleEmptyString() {
        assertThat(CobolZonedDecimalParser.parseLong("")).isEqualTo(0L);
    }

    @Test
    void shouldHandleNull() {
        assertThat(CobolZonedDecimalParser.parseLong(null)).isEqualTo(0L);
    }

    @Test
    void shouldHandleBlankString() {
        assertThat(CobolZonedDecimalParser.parseLong("   ")).isEqualTo(0L);
    }

    @Test
    void shouldThrowOnInvalidChar() {
        assertThatThrownBy(() -> CobolZonedDecimalParser.parseLong("0000000001Z"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldParseRate() {
        // 00150{ → 1500 → 15.00
        BigDecimal result = CobolZonedDecimalParser.parseRate("00150{");
        assertThat(result).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    void shouldParseAllPositiveChars() {
        assertThat(CobolZonedDecimalParser.parseLong("{")).isEqualTo(0L);
        assertThat(CobolZonedDecimalParser.parseLong("A")).isEqualTo(1L);
        assertThat(CobolZonedDecimalParser.parseLong("B")).isEqualTo(2L);
        assertThat(CobolZonedDecimalParser.parseLong("C")).isEqualTo(3L);
        assertThat(CobolZonedDecimalParser.parseLong("D")).isEqualTo(4L);
        assertThat(CobolZonedDecimalParser.parseLong("E")).isEqualTo(5L);
        assertThat(CobolZonedDecimalParser.parseLong("F")).isEqualTo(6L);
        assertThat(CobolZonedDecimalParser.parseLong("G")).isEqualTo(7L);
        assertThat(CobolZonedDecimalParser.parseLong("H")).isEqualTo(8L);
        assertThat(CobolZonedDecimalParser.parseLong("I")).isEqualTo(9L);
    }

    @Test
    void shouldParseAllNegativeChars() {
        assertThat(CobolZonedDecimalParser.parseLong("}")).isEqualTo(0L);
        assertThat(CobolZonedDecimalParser.parseLong("J")).isEqualTo(-1L);
        assertThat(CobolZonedDecimalParser.parseLong("K")).isEqualTo(-2L);
        assertThat(CobolZonedDecimalParser.parseLong("L")).isEqualTo(-3L);
        assertThat(CobolZonedDecimalParser.parseLong("M")).isEqualTo(-4L);
        assertThat(CobolZonedDecimalParser.parseLong("N")).isEqualTo(-5L);
        assertThat(CobolZonedDecimalParser.parseLong("O")).isEqualTo(-6L);
        assertThat(CobolZonedDecimalParser.parseLong("P")).isEqualTo(-7L);
        assertThat(CobolZonedDecimalParser.parseLong("Q")).isEqualTo(-8L);
        assertThat(CobolZonedDecimalParser.parseLong("R")).isEqualTo(-9L);
    }
}

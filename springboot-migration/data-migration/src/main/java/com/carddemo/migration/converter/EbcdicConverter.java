package com.carddemo.migration.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.Charset;

/**
 * Converts EBCDIC-encoded mainframe data to Java types.
 * Handles:
 * - EBCDIC character strings → UTF-8 strings
 * - COMP-3 (packed decimal) → BigDecimal
 * - Zoned decimal → BigDecimal
 * - Binary (COMP) → long/int
 */
@Component
@Slf4j
public class EbcdicConverter {

    private static final Charset EBCDIC_CHARSET;

    static {
        Charset charset;
        try {
            charset = Charset.forName("IBM037");
        } catch (Exception e) {
            charset = Charset.forName("Cp037");
        }
        EBCDIC_CHARSET = charset;
    }

    /**
     * Convert EBCDIC bytes to UTF-8 string.
     */
    public String ebcdicToString(byte[] data, int offset, int length) {
        if (data == null || offset + length > data.length) {
            return "";
        }
        byte[] slice = new byte[length];
        System.arraycopy(data, offset, slice, 0, length);
        return new String(slice, EBCDIC_CHARSET).trim();
    }

    /**
     * Convert COMP-3 (packed decimal) bytes to BigDecimal.
     * Packed decimal stores two digits per byte, with the last nibble as sign.
     * Sign nibbles: C/A/F/E = positive, D/B = negative.
     */
    public BigDecimal comp3ToDecimal(byte[] data, int offset, int length, int scale) {
        if (data == null || offset + length > data.length) {
            return BigDecimal.ZERO;
        }

        StringBuilder digits = new StringBuilder();
        boolean negative = false;

        for (int i = offset; i < offset + length; i++) {
            int b = data[i] & 0xFF;
            int highNibble = (b >> 4) & 0x0F;
            int lowNibble = b & 0x0F;

            if (i < offset + length - 1) {
                digits.append(highNibble);
                digits.append(lowNibble);
            } else {
                digits.append(highNibble);
                negative = (lowNibble == 0x0D || lowNibble == 0x0B);
            }
        }

        if (digits.length() == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = new BigDecimal(digits.toString());
        if (scale > 0) {
            result = result.movePointLeft(scale);
        }
        if (negative) {
            result = result.negate();
        }

        return result;
    }

    /**
     * Convert zoned decimal bytes to BigDecimal.
     * In zoned decimal, each byte has zone nibble (F) and digit nibble.
     * The last byte's zone nibble indicates sign (C=positive, D=negative).
     */
    public BigDecimal zonedToDecimal(byte[] data, int offset, int length, int scale) {
        if (data == null || offset + length > data.length) {
            return BigDecimal.ZERO;
        }

        StringBuilder digits = new StringBuilder();
        boolean negative = false;

        for (int i = offset; i < offset + length; i++) {
            int b = data[i] & 0xFF;
            int digit = b & 0x0F;
            digits.append(digit);

            if (i == offset + length - 1) {
                int zone = (b >> 4) & 0x0F;
                negative = (zone == 0x0D || zone == 0x0B);
            }
        }

        if (digits.length() == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = new BigDecimal(digits.toString());
        if (scale > 0) {
            result = result.movePointLeft(scale);
        }
        if (negative) {
            result = result.negate();
        }

        return result;
    }

    /**
     * Convert COMP (binary) bytes to long.
     */
    public long compToLong(byte[] data, int offset, int length) {
        if (data == null || offset + length > data.length) {
            return 0L;
        }

        long result = 0;
        for (int i = offset; i < offset + length; i++) {
            result = (result << 8) | (data[i] & 0xFF);
        }
        return result;
    }
}

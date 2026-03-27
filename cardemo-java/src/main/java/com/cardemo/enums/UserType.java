package com.cardemo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * User type enum mapping to COBOL 88-level conditions in COCOM01Y.cpy:
 *   88 CDEMO-USRTYP-ADMIN VALUE 'A'.
 *   88 CDEMO-USRTYP-USER  VALUE 'U'.
 */
public enum UserType {

    ADMIN('A'),
    USER('U');

    private final char code;

    UserType(char code) {
        this.code = code;
    }

    @JsonValue
    public char getCode() {
        return code;
    }

    @JsonCreator
    public static UserType fromCode(char code) {
        for (UserType type : values()) {
            if (type.code == Character.toUpperCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid user type code: " + code);
    }

    public static UserType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("User type code cannot be null or empty");
        }
        return fromCode(code.charAt(0));
    }
}

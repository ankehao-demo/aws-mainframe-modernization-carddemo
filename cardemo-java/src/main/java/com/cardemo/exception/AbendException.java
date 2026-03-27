package com.cardemo.exception;

/**
 * Exception replacing the COBOL ABEND pattern from CSMSG02Y.cpy (lines 21-29).
 *
 * COBOL field mappings:
 *   ABEND-CODE      PIC X(4)   -> abendCode
 *   ABEND-CULPRIT   PIC X(8)   -> abendCulprit
 *   ABEND-REASON    PIC X(50)  -> abendReason
 *   ABEND-MSG       PIC X(72)  -> abendMsg
 */
public class AbendException extends CardDemoException {

    private final String abendCode;
    private final String abendCulprit;
    private final String abendReason;
    private final String abendMsg;

    public AbendException(String abendCode, String abendCulprit,
                          String abendReason, String abendMsg) {
        super(abendMsg);
        this.abendCode = abendCode;
        this.abendCulprit = abendCulprit;
        this.abendReason = abendReason;
        this.abendMsg = abendMsg;
    }

    public String getAbendCode() {
        return abendCode;
    }

    public String getAbendCulprit() {
        return abendCulprit;
    }

    public String getAbendReason() {
        return abendReason;
    }

    public String getAbendMsg() {
        return abendMsg;
    }
}

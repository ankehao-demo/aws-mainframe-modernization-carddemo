package com.carddemo.transactiontype.batch;

/**
 * DTO representing a single record from the batch input flat file.
 *
 * Maps to the COBOL structure WS-INPUT-REC in COBTUPDT.cbl (lines 71-120):
 * - Position 1 (1 char): Record type — A (Add), U (Update), D (Delete), or * (comment/skip)
 * - Positions 2-3 (2 chars): Transaction type code (TR_TYPE)
 * - Positions 4-53 (50 chars): Description (TR_DESCRIPTION)
 */
public class TrRecord {

    private String recordType;
    private String trType;
    private String trDescription;

    public TrRecord() {
    }

    public TrRecord(String recordType, String trType, String trDescription) {
        this.recordType = recordType;
        this.trType = trType;
        this.trDescription = trDescription;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getTrType() {
        return trType;
    }

    public void setTrType(String trType) {
        this.trType = trType;
    }

    public String getTrDescription() {
        return trDescription;
    }

    public void setTrDescription(String trDescription) {
        this.trDescription = trDescription;
    }
}

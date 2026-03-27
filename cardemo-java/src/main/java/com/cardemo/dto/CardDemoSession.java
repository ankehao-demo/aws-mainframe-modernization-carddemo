package com.cardemo.dto;

import com.cardemo.enums.UserType;

/**
 * Session/context DTO replacing CICS COMMAREA from COCOM01Y.cpy (lines 19-45).
 * In the Java app this is represented via JWT claims.
 *
 * COBOL field mappings:
 *   CDEMO-FROM-TRANID      PIC X(04)  -> fromTranId
 *   CDEMO-FROM-PROGRAM     PIC X(08)  -> fromProgram
 *   CDEMO-TO-TRANID        PIC X(04)  -> toTranId
 *   CDEMO-TO-PROGRAM       PIC X(08)  -> toProgram
 *   CDEMO-USER-ID          PIC X(08)  -> userId
 *   CDEMO-USER-TYPE        PIC X(01)  -> userType (ADMIN/USER)
 *   CDEMO-PGM-CONTEXT      PIC 9(01)  -> pgmContext (ENTER/REENTER)
 *   CDEMO-CUST-ID          PIC 9(09)  -> custId
 *   CDEMO-CUST-FNAME       PIC X(25)  -> custFirstName
 *   CDEMO-CUST-MNAME       PIC X(25)  -> custMiddleName
 *   CDEMO-CUST-LNAME       PIC X(25)  -> custLastName
 *   CDEMO-ACCT-ID          PIC 9(11)  -> acctId
 *   CDEMO-ACCT-STATUS      PIC X(01)  -> acctStatus
 *   CDEMO-CARD-NUM         PIC 9(16)  -> cardNum
 *   CDEMO-LAST-MAP         PIC X(07)  -> lastMap
 *   CDEMO-LAST-MAPSET      PIC X(07)  -> lastMapset
 */
public class CardDemoSession {

    public enum PgmContext {
        ENTER,
        REENTER
    }

    private String fromTranId;
    private String fromProgram;
    private String toTranId;
    private String toProgram;
    private String userId;
    private UserType userType;
    private PgmContext pgmContext;
    private Long custId;
    private String custFirstName;
    private String custMiddleName;
    private String custLastName;
    private Long acctId;
    private String acctStatus;
    private Long cardNum;
    private String lastMap;
    private String lastMapset;

    public CardDemoSession() {
    }

    public String getFromTranId() {
        return fromTranId;
    }

    public void setFromTranId(String fromTranId) {
        this.fromTranId = fromTranId;
    }

    public String getFromProgram() {
        return fromProgram;
    }

    public void setFromProgram(String fromProgram) {
        this.fromProgram = fromProgram;
    }

    public String getToTranId() {
        return toTranId;
    }

    public void setToTranId(String toTranId) {
        this.toTranId = toTranId;
    }

    public String getToProgram() {
        return toProgram;
    }

    public void setToProgram(String toProgram) {
        this.toProgram = toProgram;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public PgmContext getPgmContext() {
        return pgmContext;
    }

    public void setPgmContext(PgmContext pgmContext) {
        this.pgmContext = pgmContext;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public String getCustFirstName() {
        return custFirstName;
    }

    public void setCustFirstName(String custFirstName) {
        this.custFirstName = custFirstName;
    }

    public String getCustMiddleName() {
        return custMiddleName;
    }

    public void setCustMiddleName(String custMiddleName) {
        this.custMiddleName = custMiddleName;
    }

    public String getCustLastName() {
        return custLastName;
    }

    public void setCustLastName(String custLastName) {
        this.custLastName = custLastName;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public String getAcctStatus() {
        return acctStatus;
    }

    public void setAcctStatus(String acctStatus) {
        this.acctStatus = acctStatus;
    }

    public Long getCardNum() {
        return cardNum;
    }

    public void setCardNum(Long cardNum) {
        this.cardNum = cardNum;
    }

    public String getLastMap() {
        return lastMap;
    }

    public void setLastMap(String lastMap) {
        this.lastMap = lastMap;
    }

    public String getLastMapset() {
        return lastMapset;
    }

    public void setLastMapset(String lastMapset) {
        this.lastMapset = lastMapset;
    }
}

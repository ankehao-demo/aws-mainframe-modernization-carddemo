package com.cardemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Card entity derived from CVACT02Y.cpy (CARD-RECORD, RECLN 150).
 *
 * COBOL field mappings:
 *   CARD-NUM              PIC X(16)  -> String (primary key)
 *   CARD-ACCT-ID          PIC 9(11)  -> Long
 *   CARD-CVV-CD           PIC 9(03)  -> Integer
 *   CARD-EMBOSSED-NAME    PIC X(50)  -> String
 *   CARD-EXPIRAION-DATE   PIC X(10)  -> String
 *   CARD-ACTIVE-STATUS    PIC X(01)  -> String
 */
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "card_acct_id")
    private Long acctId;

    @Column(name = "card_cvv_cd")
    private Integer cvvCode;

    @Column(name = "card_embossed_name", length = 50)
    private String embossedName;

    @Column(name = "card_expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "card_active_status", length = 1)
    private String activeStatus;

    public Card() {
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public Integer getCvvCode() {
        return cvvCode;
    }

    public void setCvvCode(Integer cvvCode) {
        this.cvvCode = cvvCode;
    }

    public String getEmbossedName() {
        return embossedName;
    }

    public void setEmbossedName(String embossedName) {
        this.embossedName = embossedName;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }
}

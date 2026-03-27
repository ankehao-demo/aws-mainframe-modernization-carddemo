package com.cardemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Card cross-reference entity derived from CVACT03Y.cpy (CARD-XREF-RECORD, RECLN 50).
 *
 * COBOL field mappings:
 *   XREF-CARD-NUM   PIC X(16)  -> String (primary key)
 *   XREF-CUST-ID    PIC 9(09)  -> Long
 *   XREF-ACCT-ID    PIC 9(11)  -> Long
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16)
    private String cardNum;

    @Column(name = "xref_cust_id")
    private Long custId;

    @Column(name = "xref_acct_id")
    private Long acctId;

    public CardXref() {
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }
}

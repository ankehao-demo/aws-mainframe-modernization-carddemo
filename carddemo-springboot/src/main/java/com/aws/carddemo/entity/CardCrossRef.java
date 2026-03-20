package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "card_cross_ref")
public class CardCrossRef {

    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "cust_id", length = 9)
    private String custId;

    @Column(name = "acct_id", length = 11)
    private String acctId;

    public CardCrossRef() {}

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public String getCustId() { return custId; }
    public void setCustId(String custId) { this.custId = custId; }

    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }
}

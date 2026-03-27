package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "tran_cat_balances")
@IdClass(TranCatBalanceId.class)
public class TranCatBalance {

    @Id
    @Column(name = "acct_id")
    private Long acctId;

    @Id
    @Column(name = "type_cd", length = 2)
    private String typeCd;

    @Id
    @Column(name = "cat_cd")
    private Integer catCd;

    @Column(name = "balance", precision = 12, scale = 2)
    private BigDecimal balance;

    public TranCatBalance() {
    }

    public TranCatBalance(Long acctId, String typeCd, Integer catCd, BigDecimal balance) {
        this.acctId = acctId;
        this.typeCd = typeCd;
        this.catCd = catCd;
        this.balance = balance;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public Integer getCatCd() {
        return catCd;
    }

    public void setCatCd(Integer catCd) {
        this.catCd = catCd;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

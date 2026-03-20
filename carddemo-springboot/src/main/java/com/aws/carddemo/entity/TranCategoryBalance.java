package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "tran_category_balance")
@IdClass(TranCategoryBalanceId.class)
public class TranCategoryBalance {

    @Id
    @Column(name = "acct_id", length = 11, nullable = false)
    private String acctId;

    @Id
    @Column(name = "type_cd", length = 2, nullable = false)
    private String typeCd;

    @Id
    @Column(name = "cat_cd", nullable = false)
    private Integer catCd;

    @Column(name = "balance", precision = 11, scale = 2)
    private BigDecimal balance;

    public TranCategoryBalance() {}

    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}

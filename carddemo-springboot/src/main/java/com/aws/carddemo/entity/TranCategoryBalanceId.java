package com.aws.carddemo.entity;

import java.io.Serializable;
import java.util.Objects;

public class TranCategoryBalanceId implements Serializable {

    private String acctId;
    private String typeCd;
    private Integer catCd;

    public TranCategoryBalanceId() {}

    public TranCategoryBalanceId(String acctId, String typeCd, Integer catCd) {
        this.acctId = acctId;
        this.typeCd = typeCd;
        this.catCd = catCd;
    }

    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranCategoryBalanceId that = (TranCategoryBalanceId) o;
        return Objects.equals(acctId, that.acctId)
                && Objects.equals(typeCd, that.typeCd)
                && Objects.equals(catCd, that.catCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, typeCd, catCd);
    }
}

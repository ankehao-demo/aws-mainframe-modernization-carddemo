package com.carddemo.model;

import java.io.Serializable;
import java.util.Objects;

public class DisclosureGroupId implements Serializable {

    private String groupId;
    private String tranTypeCd;
    private Integer tranCatCd;

    public DisclosureGroupId() {
    }

    public DisclosureGroupId(String groupId, String tranTypeCd, Integer tranCatCd) {
        this.groupId = groupId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisclosureGroupId that = (DisclosureGroupId) o;
        return Objects.equals(groupId, that.groupId) &&
               Objects.equals(tranTypeCd, that.tranTypeCd) &&
               Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, tranTypeCd, tranCatCd);
    }
}

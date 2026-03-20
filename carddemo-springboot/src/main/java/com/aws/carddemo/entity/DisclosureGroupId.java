package com.aws.carddemo.entity;

import java.io.Serializable;
import java.util.Objects;

public class DisclosureGroupId implements Serializable {

    private String groupId;
    private String typeCd;
    private Integer catCd;

    public DisclosureGroupId() {}

    public DisclosureGroupId(String groupId, String typeCd, Integer catCd) {
        this.groupId = groupId;
        this.typeCd = typeCd;
        this.catCd = catCd;
    }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisclosureGroupId that = (DisclosureGroupId) o;
        return Objects.equals(groupId, that.groupId)
                && Objects.equals(typeCd, that.typeCd)
                && Objects.equals(catCd, that.catCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, typeCd, catCd);
    }
}

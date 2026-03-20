package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "disclosure_group")
@IdClass(DisclosureGroupId.class)
public class DisclosureGroup {

    @Id
    @Column(name = "group_id", length = 10, nullable = false)
    private String groupId;

    @Id
    @Column(name = "type_cd", length = 2, nullable = false)
    private String typeCd;

    @Id
    @Column(name = "cat_cd", nullable = false)
    private Integer catCd;

    @Column(name = "int_rate", precision = 6, scale = 2)
    private BigDecimal intRate;

    public DisclosureGroup() {}

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }

    public BigDecimal getIntRate() { return intRate; }
    public void setIntRate(BigDecimal intRate) { this.intRate = intRate; }
}

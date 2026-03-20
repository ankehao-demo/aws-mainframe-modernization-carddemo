package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "transaction_category")
@IdClass(TransactionCategoryId.class)
public class TransactionCategory {

    @Id
    @Column(name = "type_cd", length = 2, nullable = false)
    private String typeCd;

    @Id
    @Column(name = "cat_cd", nullable = false)
    private Integer catCd;

    @Column(name = "cat_type_desc", length = 50)
    private String catTypeDesc;

    public TransactionCategory() {}

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }

    public String getCatTypeDesc() { return catTypeDesc; }
    public void setCatTypeDesc(String catTypeDesc) { this.catTypeDesc = catTypeDesc; }
}

package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tran_types")
public class TranType {

    @Id
    @Column(name = "type_cd", length = 2)
    private String typeCd;

    @Column(name = "type_desc", length = 50)
    private String typeDesc;

    public TranType() {
    }

    public TranType(String typeCd, String typeDesc) {
        this.typeCd = typeCd;
        this.typeDesc = typeDesc;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }
}

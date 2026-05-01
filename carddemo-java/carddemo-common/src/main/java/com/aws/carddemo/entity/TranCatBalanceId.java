package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TranCatBalanceId implements Serializable {

    @Column(name = "acct_id")
    private Long acctId;

    @Column(name = "type_cd", length = 2)
    private String typeCd;

    @Column(name = "cat_cd")
    private Integer catCd;
}

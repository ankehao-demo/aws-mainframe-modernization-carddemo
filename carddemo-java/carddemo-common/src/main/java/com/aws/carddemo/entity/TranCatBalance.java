package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tran_cat_balance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranCatBalance {

    @EmbeddedId
    private TranCatBalanceId id;

    @Column(name = "balance", precision = 12, scale = 2)
    private BigDecimal balance;
}

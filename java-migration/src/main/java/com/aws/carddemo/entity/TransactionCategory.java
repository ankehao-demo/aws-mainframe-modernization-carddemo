package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction_category")
@IdClass(TransactionCategoryId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCategory {

    @Id
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    @Id
    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    @Column(name = "tran_cat_type_desc", length = 50)
    private String tranCatTypeDesc;
}

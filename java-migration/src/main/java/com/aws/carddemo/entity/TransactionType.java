package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionType {

    @Id
    @Column(name = "tran_type", length = 2)
    private String tranType;

    @Column(name = "tran_type_desc", length = 50)
    private String tranTypeDesc;
}

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
public class TransactionId implements Serializable {

    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "tran_id", length = 16)
    private String tranId;
}

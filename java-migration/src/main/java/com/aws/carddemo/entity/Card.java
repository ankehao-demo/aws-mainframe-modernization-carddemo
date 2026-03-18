package com.aws.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "card_acct_id", length = 11, nullable = false)
    private String cardAcctId;

    @Column(name = "card_cvv_cd", length = 3, nullable = false)
    private String cardCvvCd;

    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    @Column(name = "card_expiration_date", length = 10)
    private String cardExpirationDate;

    @Column(name = "card_active_status", length = 1, nullable = false)
    private String cardActiveStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_acct_id", referencedColumnName = "acct_id", insertable = false, updatable = false)
    private Account account;
}

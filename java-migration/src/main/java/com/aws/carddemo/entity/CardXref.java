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
@Table(name = "card_xref")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16)
    private String xrefCardNum;

    @Column(name = "xref_cust_id", length = 9, nullable = false)
    private String xrefCustId;

    @Column(name = "xref_acct_id", length = 11, nullable = false)
    private String xrefAcctId;
}

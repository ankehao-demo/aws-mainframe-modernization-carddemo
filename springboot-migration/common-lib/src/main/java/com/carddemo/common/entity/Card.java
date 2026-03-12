package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVACT02Y.cpy (CARD-RECORD, RECLN 150)
 * and CVACT03Y.cpy (CARD-XREF-RECORD, RECLN 50) for FK relationships.
 * Represents the CARDDATA and CARDXREF VSAM KSDS files.
 */
@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    /** CARD-NUM PIC X(16) - primary key from CARDDATA */
    @Id
    @Column(name = "card_number", nullable = false, length = 16)
    private String cardNumber;

    /** CARD-ACCT-ID PIC 9(11) - FK to accounts from CARDDATA */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /** XREF-CUST-ID PIC 9(09) - FK to customers from CARDXREF */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** CARD-CVV-CD PIC 9(03) */
    @Column(name = "cvv_code", length = 3)
    private String cvvCode;

    /** CARD-EMBOSSED-NAME PIC X(50) */
    @Column(name = "embossed_name", length = 50)
    private String embossedName;

    /** CARD-EXPIRAION-DATE PIC X(10) */
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    /** CARD-ACTIVE-STATUS PIC X(01) */
    @Column(name = "card_status", length = 1, nullable = false)
    private String cardStatus;
}

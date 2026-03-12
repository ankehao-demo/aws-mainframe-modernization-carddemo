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

/**
 * JPA entity mapped from COBOL copybook CVTRA03Y.cpy (TRAN-TYPE-RECORD, RECLN 60).
 * Represents the TRANTYPE VSAM KSDS file.
 */
@Entity
@Table(name = "transaction_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionType {

    /** TRAN-TYPE PIC X(02) */
    @Id
    @Column(name = "transaction_type_code", nullable = false, length = 2)
    private String transactionTypeCode;

    /** TRAN-TYPE-DESC PIC X(50) */
    @Column(name = "description", length = 50)
    private String description;
}

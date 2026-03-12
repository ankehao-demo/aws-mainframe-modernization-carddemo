package com.carddemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * JPA entity mapped from COBOL copybook CVTRA04Y.cpy (TRAN-CAT-RECORD, RECLN 60).
 * Represents the TRANCATG VSAM KSDS file.
 */
@Entity
@Table(name = "transaction_categories")
@IdClass(TransactionCategory.TransactionCategoryId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCategory {

    /** TRAN-TYPE-CD PIC X(02) */
    @Id
    @Column(name = "transaction_type_code", nullable = false, length = 2)
    private String transactionTypeCode;

    /** TRAN-CAT-CD PIC 9(04) */
    @Id
    @Column(name = "category_code", nullable = false, length = 4)
    private String categoryCode;

    /** TRAN-CAT-TYPE-DESC PIC X(50) */
    @Column(name = "description", length = 50)
    private String description;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionCategoryId implements Serializable {
        private String transactionTypeCode;
        private String categoryCode;
    }
}

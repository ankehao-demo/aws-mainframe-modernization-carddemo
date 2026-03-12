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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVCUS01Y.cpy (CUSTOMER-RECORD, RECLN 500).
 * Represents the CUSTDATA VSAM KSDS file.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    /** CUST-ID PIC 9(09) */
    @Id
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** CUST-FIRST-NAME PIC X(25) */
    @Column(name = "first_name", length = 25)
    private String firstName;

    /** CUST-MIDDLE-NAME PIC X(25) */
    @Column(name = "middle_name", length = 25)
    private String middleName;

    /** CUST-LAST-NAME PIC X(25) */
    @Column(name = "last_name", length = 25)
    private String lastName;

    /** CUST-ADDR-LINE-1 PIC X(50) */
    @Column(name = "address_line_1", length = 50)
    private String addressLine1;

    /** CUST-ADDR-LINE-2 PIC X(50) */
    @Column(name = "address_line_2", length = 50)
    private String addressLine2;

    /** CUST-ADDR-LINE-3 PIC X(50) */
    @Column(name = "address_line_3", length = 50)
    private String addressLine3;

    /** CUST-ADDR-STATE-CD PIC X(02) */
    @Column(name = "state", length = 2)
    private String state;

    /** CUST-ADDR-COUNTRY-CD PIC X(03) */
    @Column(name = "country_code", length = 3)
    private String countryCode;

    /** CUST-ADDR-ZIP PIC X(10) */
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    /** CUST-PHONE-NUM-1 PIC X(15) */
    @Column(name = "phone_1", length = 15)
    private String phone1;

    /** CUST-PHONE-NUM-2 PIC X(15) */
    @Column(name = "phone_2", length = 15)
    private String phone2;

    /** CUST-SSN PIC 9(09) */
    @Column(name = "ssn", length = 9)
    private String ssn;

    /** CUST-GOVT-ISSUED-ID PIC X(20) */
    @Column(name = "govt_id", length = 20)
    private String govtId;

    /** CUST-DOB-YYYY-MM-DD PIC X(10) */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** CUST-EFT-ACCOUNT-ID PIC X(10) */
    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    /** CUST-PRI-CARD-HOLDER-IND PIC X(01) */
    @Column(name = "primary_card_holder", length = 1)
    private String primaryCardHolder;

    /** CUST-FICO-CREDIT-SCORE PIC 9(03) */
    @Column(name = "fico_credit_score")
    private Integer ficoCreditScore;
}

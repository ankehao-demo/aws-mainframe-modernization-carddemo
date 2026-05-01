package com.aws.carddemo.entity;

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

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "first_name", length = 25)
    private String firstName;

    @Column(name = "middle_name", length = 25)
    private String middleName;

    @Column(name = "last_name", length = 25)
    private String lastName;

    @Column(name = "addr_line1", length = 50)
    private String addrLine1;

    @Column(name = "addr_line2", length = 50)
    private String addrLine2;

    @Column(name = "addr_line3", length = 50)
    private String addrLine3;

    @Column(name = "state_cd", length = 2)
    private String stateCd;

    @Column(name = "country_cd", length = 3)
    private String countryCd;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "phone_num1", length = 15)
    private String phoneNum1;

    @Column(name = "phone_num2", length = 15)
    private String phoneNum2;

    @Column(name = "ssn", length = 9)
    private String ssn;

    @Column(name = "govt_issued_id", length = 20)
    private String govtIssuedId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    @Column(name = "pri_card_holder_ind", length = 1)
    private String priCardHolderInd;

    @Column(name = "fico_score")
    private Integer ficoScore;

    @Column(name = "active_status", length = 1)
    private String activeStatus;
}

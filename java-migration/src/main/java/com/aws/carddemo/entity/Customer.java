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
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "cust_id", length = 9)
    private String custId;

    @Column(name = "cust_first_name", length = 25)
    private String custFirstName;

    @Column(name = "cust_middle_name", length = 25)
    private String custMiddleName;

    @Column(name = "cust_last_name", length = 25)
    private String custLastName;

    @Column(name = "cust_addr_line_1", length = 50)
    private String custAddrLine1;

    @Column(name = "cust_addr_line_2", length = 50)
    private String custAddrLine2;

    @Column(name = "cust_addr_line_3", length = 50)
    private String custAddrLine3;

    @Column(name = "cust_addr_state_cd", length = 2)
    private String custAddrStateCd;

    @Column(name = "cust_addr_country_cd", length = 3)
    private String custAddrCountryCd;

    @Column(name = "cust_addr_zip", length = 10)
    private String custAddrZip;

    @Column(name = "cust_phone_num_1", length = 15)
    private String custPhoneNum1;

    @Column(name = "cust_phone_num_2", length = 15)
    private String custPhoneNum2;

    @Column(name = "cust_ssn", length = 9)
    private String custSsn;

    @Column(name = "cust_govt_issued_id", length = 20)
    private String custGovtIssuedId;

    @Column(name = "cust_dob_yyyy_mm_dd", length = 10)
    private String custDobYyyyMmDd;

    @Column(name = "cust_eft_account_id", length = 10)
    private String custEftAccountId;

    @Column(name = "cust_pri_card_holder_ind", length = 1)
    private String custPriCardHolderInd;

    @Column(name = "cust_fico_credit_score")
    private Integer custFicoCreditScore;
}

package com.cardemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Customer entity derived from CVCUS01Y.cpy (CUSTOMER-RECORD, RECLN 500).
 *
 * COBOL field mappings:
 *   CUST-ID                   PIC 9(09)  -> Long
 *   CUST-FIRST-NAME           PIC X(25)  -> String
 *   CUST-MIDDLE-NAME          PIC X(25)  -> String
 *   CUST-LAST-NAME            PIC X(25)  -> String
 *   CUST-ADDR-LINE-1          PIC X(50)  -> String
 *   CUST-ADDR-LINE-2          PIC X(50)  -> String
 *   CUST-ADDR-LINE-3          PIC X(50)  -> String
 *   CUST-ADDR-STATE-CD        PIC X(02)  -> String
 *   CUST-ADDR-COUNTRY-CD      PIC X(03)  -> String
 *   CUST-ADDR-ZIP             PIC X(10)  -> String
 *   CUST-PHONE-NUM-1          PIC X(15)  -> String
 *   CUST-PHONE-NUM-2          PIC X(15)  -> String
 *   CUST-SSN                  PIC 9(09)  -> Long
 *   CUST-GOVT-ISSUED-ID       PIC X(20)  -> String
 *   CUST-DOB-YYYY-MM-DD       PIC X(10)  -> String
 *   CUST-EFT-ACCOUNT-ID       PIC X(10)  -> String
 *   CUST-PRI-CARD-HOLDER-IND  PIC X(01)  -> String
 *   CUST-FICO-CREDIT-SCORE    PIC 9(03)  -> Integer
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "cust_first_name", length = 25)
    private String firstName;

    @Column(name = "cust_middle_name", length = 25)
    private String middleName;

    @Column(name = "cust_last_name", length = 25)
    private String lastName;

    @Column(name = "cust_addr_line_1", length = 50)
    private String addrLine1;

    @Column(name = "cust_addr_line_2", length = 50)
    private String addrLine2;

    @Column(name = "cust_addr_line_3", length = 50)
    private String addrLine3;

    @Column(name = "cust_addr_state_cd", length = 2)
    private String addrStateCd;

    @Column(name = "cust_addr_country_cd", length = 3)
    private String addrCountryCd;

    @Column(name = "cust_addr_zip", length = 10)
    private String addrZip;

    @Column(name = "cust_phone_num_1", length = 15)
    private String phoneNum1;

    @Column(name = "cust_phone_num_2", length = 15)
    private String phoneNum2;

    @Column(name = "cust_ssn")
    private Long ssn;

    @Column(name = "cust_govt_issued_id", length = 20)
    private String govtIssuedId;

    @Column(name = "cust_dob", length = 10)
    private String dateOfBirth;

    @Column(name = "cust_eft_account_id", length = 10)
    private String eftAccountId;

    @Column(name = "cust_pri_card_holder_ind", length = 1)
    private String primaryCardHolderInd;

    @Column(name = "cust_fico_credit_score")
    private Integer ficoCreditScore;

    public Customer() {
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddrLine1() {
        return addrLine1;
    }

    public void setAddrLine1(String addrLine1) {
        this.addrLine1 = addrLine1;
    }

    public String getAddrLine2() {
        return addrLine2;
    }

    public void setAddrLine2(String addrLine2) {
        this.addrLine2 = addrLine2;
    }

    public String getAddrLine3() {
        return addrLine3;
    }

    public void setAddrLine3(String addrLine3) {
        this.addrLine3 = addrLine3;
    }

    public String getAddrStateCd() {
        return addrStateCd;
    }

    public void setAddrStateCd(String addrStateCd) {
        this.addrStateCd = addrStateCd;
    }

    public String getAddrCountryCd() {
        return addrCountryCd;
    }

    public void setAddrCountryCd(String addrCountryCd) {
        this.addrCountryCd = addrCountryCd;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public void setAddrZip(String addrZip) {
        this.addrZip = addrZip;
    }

    public String getPhoneNum1() {
        return phoneNum1;
    }

    public void setPhoneNum1(String phoneNum1) {
        this.phoneNum1 = phoneNum1;
    }

    public String getPhoneNum2() {
        return phoneNum2;
    }

    public void setPhoneNum2(String phoneNum2) {
        this.phoneNum2 = phoneNum2;
    }

    public Long getSsn() {
        return ssn;
    }

    public void setSsn(Long ssn) {
        this.ssn = ssn;
    }

    public String getGovtIssuedId() {
        return govtIssuedId;
    }

    public void setGovtIssuedId(String govtIssuedId) {
        this.govtIssuedId = govtIssuedId;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEftAccountId() {
        return eftAccountId;
    }

    public void setEftAccountId(String eftAccountId) {
        this.eftAccountId = eftAccountId;
    }

    public String getPrimaryCardHolderInd() {
        return primaryCardHolderInd;
    }

    public void setPrimaryCardHolderInd(String primaryCardHolderInd) {
        this.primaryCardHolderInd = primaryCardHolderInd;
    }

    public Integer getFicoCreditScore() {
        return ficoCreditScore;
    }

    public void setFicoCreditScore(Integer ficoCreditScore) {
        this.ficoCreditScore = ficoCreditScore;
    }
}

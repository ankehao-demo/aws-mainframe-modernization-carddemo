package com.carddemo.account.entity;

import jakarta.persistence.*;

/**
 * Customer entity - from copybook CVCUS01Y.cpy (500-byte records).
 * Dataset: AWS.M2.CARDDEMO.CUSTDATA.PS
 */
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "first_name", length = 25)
    private String firstName;

    @Column(name = "middle_name", length = 25)
    private String middleName;

    @Column(name = "last_name", length = 25)
    private String lastName;

    @Column(name = "address_line_1", length = 50)
    private String addressLine1;

    @Column(name = "address_line_2", length = 50)
    private String addressLine2;

    @Column(name = "address_line_3", length = 50)
    private String addressLine3;

    @Column(name = "address_state_cd", length = 2)
    private String addressStateCd;

    @Column(name = "address_country_cd", length = 3)
    private String addressCountryCd;

    @Column(name = "address_zip", length = 10)
    private String addressZip;

    @Column(name = "phone_num_1", length = 15)
    private String phoneNum1;

    @Column(name = "phone_num_2", length = 15)
    private String phoneNum2;

    @Column(name = "ssn")
    private Long ssn;

    @Column(name = "govt_issued_id", length = 20)
    private String govtIssuedId;

    @Column(name = "date_of_birth", length = 10)
    private String dateOfBirth;

    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    @Column(name = "primary_card_holder_ind", length = 1)
    private String primaryCardHolderInd;

    @Column(name = "fico_credit_score")
    private Integer ficoCreditScore;

    public Customer() {}

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    public String getAddressLine3() { return addressLine3; }
    public void setAddressLine3(String addressLine3) { this.addressLine3 = addressLine3; }
    public String getAddressStateCd() { return addressStateCd; }
    public void setAddressStateCd(String addressStateCd) { this.addressStateCd = addressStateCd; }
    public String getAddressCountryCd() { return addressCountryCd; }
    public void setAddressCountryCd(String addressCountryCd) { this.addressCountryCd = addressCountryCd; }
    public String getAddressZip() { return addressZip; }
    public void setAddressZip(String addressZip) { this.addressZip = addressZip; }
    public String getPhoneNum1() { return phoneNum1; }
    public void setPhoneNum1(String phoneNum1) { this.phoneNum1 = phoneNum1; }
    public String getPhoneNum2() { return phoneNum2; }
    public void setPhoneNum2(String phoneNum2) { this.phoneNum2 = phoneNum2; }
    public Long getSsn() { return ssn; }
    public void setSsn(Long ssn) { this.ssn = ssn; }
    public String getGovtIssuedId() { return govtIssuedId; }
    public void setGovtIssuedId(String govtIssuedId) { this.govtIssuedId = govtIssuedId; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getEftAccountId() { return eftAccountId; }
    public void setEftAccountId(String eftAccountId) { this.eftAccountId = eftAccountId; }
    public String getPrimaryCardHolderInd() { return primaryCardHolderInd; }
    public void setPrimaryCardHolderInd(String primaryCardHolderInd) { this.primaryCardHolderInd = primaryCardHolderInd; }
    public Integer getFicoCreditScore() { return ficoCreditScore; }
    public void setFicoCreditScore(Integer ficoCreditScore) { this.ficoCreditScore = ficoCreditScore; }
}

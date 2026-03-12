package com.carddemo.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {

    @JsonProperty("customer_id")
    private Long customerId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("address_line_2")
    private String addressLine2;

    @JsonProperty("address_line_3")
    private String addressLine3;

    private String state;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("zip_code")
    private String zipCode;

    @JsonProperty("phone_1")
    private String phone1;

    @JsonProperty("phone_2")
    private String phone2;

    private String ssn;

    @JsonProperty("govt_id")
    private String govtId;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @JsonProperty("eft_account_id")
    private String eftAccountId;

    @JsonProperty("primary_card_holder")
    private String primaryCardHolder;

    @JsonProperty("fico_credit_score")
    private Integer ficoCreditScore;
}

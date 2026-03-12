package com.carddemo.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("current_balance")
    private BigDecimal currentBalance;

    @JsonProperty("credit_limit")
    private BigDecimal creditLimit;

    @JsonProperty("cash_credit_limit")
    private BigDecimal cashCreditLimit;

    @JsonProperty("open_date")
    private LocalDate openDate;

    @JsonProperty("expiration_date")
    private LocalDate expirationDate;

    @JsonProperty("reissue_date")
    private LocalDate reissueDate;

    @JsonProperty("curr_cyc_credit")
    private BigDecimal currCycCredit;

    @JsonProperty("curr_cyc_debit")
    private BigDecimal currCycDebit;

    @JsonProperty("address_zip")
    private String addressZip;

    @JsonProperty("group_id")
    private String groupId;
}

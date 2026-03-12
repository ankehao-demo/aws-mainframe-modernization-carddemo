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
public class CardDto {

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("customer_id")
    private Long customerId;

    @JsonProperty("cvv_code")
    private String cvvCode;

    @JsonProperty("embossed_name")
    private String embossedName;

    @JsonProperty("expiration_date")
    private LocalDate expirationDate;

    @JsonProperty("card_status")
    private String cardStatus;
}

package com.carddemo.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("transaction_type_code")
    private String transactionTypeCode;

    @JsonProperty("transaction_category_code")
    private Integer transactionCategoryCode;

    @JsonProperty("transaction_source")
    private String transactionSource;

    @JsonProperty("transaction_description")
    private String transactionDescription;

    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    @JsonProperty("merchant_id")
    private Long merchantId;

    @JsonProperty("merchant_name")
    private String merchantName;

    @JsonProperty("merchant_city")
    private String merchantCity;

    @JsonProperty("merchant_zip")
    private String merchantZip;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("transaction_timestamp")
    private LocalDateTime transactionTimestamp;

    @JsonProperty("processed_timestamp")
    private LocalDateTime processedTimestamp;
}

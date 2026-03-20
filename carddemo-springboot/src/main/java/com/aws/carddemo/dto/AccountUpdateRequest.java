package com.aws.carddemo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountUpdateRequest(
        String activeStatus,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        LocalDate expirationDate,
        LocalDate reissueDate,
        String addrZip,
        String groupId
) {}

package com.aws.carddemo.dto;

import java.time.LocalDate;

public record CardUpdateRequest(
        String embossedName,
        LocalDate expirationDate,
        String activeStatus
) {}

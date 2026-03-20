package com.aws.carddemo.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReportRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}

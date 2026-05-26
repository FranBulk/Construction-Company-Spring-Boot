package com.constructioncompany.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
    @Min(2000) @Max(9999) int year,
    @Min(1) @Max(53) int weekNumber,
    @NotBlank String employeeId
) {
}

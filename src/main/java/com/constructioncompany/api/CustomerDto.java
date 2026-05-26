package com.constructioncompany.api;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CustomerDto(
    @NotBlank String customerId,
    @NotBlank String name,
    String phone,
    String email,
    String customerType,
    LocalDate registrationDate
) {
}

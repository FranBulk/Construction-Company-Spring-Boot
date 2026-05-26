package com.constructioncompany.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeDto(
    String employeeId,
    String bossId,
    String constructionSiteId,
    String firstName,
    String lastName,
    LocalDate startDate,
    LocalDate hireDate,
    BigDecimal currentPay,
    String bankAccount,
    String bankName,
    String phone
) {
}

package com.constructioncompany.api;

import java.math.BigDecimal;

public record EmployeePaymentStatus(
    String employeeId,
    String employeeName,
    BigDecimal currentPay,
    BigDecimal paymentAmount,
    BigDecimal tax,
    BigDecimal netPayment,
    String status
) {
}

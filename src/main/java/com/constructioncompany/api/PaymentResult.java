package com.constructioncompany.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResult(
    String status,
    String employeeId,
    String employeeName,
    int year,
    int weekNumber,
    BigDecimal currentPay,
    BigDecimal paymentAmount,
    BigDecimal tax,
    BigDecimal netPayment,
    LocalDate payDate,
    String message
) {
}

package com.constructioncompany.repository;

import com.constructioncompany.api.EmployeePaymentStatus;
import com.constructioncompany.api.PaymentRequest;
import com.constructioncompany.api.PaymentResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EmployeePaymentStatus> findEmployeesToPay(int year, int weekNumber) {
        return jdbcTemplate.query(
            """
            select e.Employee_ID,
                   e.FirstName || ' ' || e.LastName as EmployeeName,
                   e.CurrentPay,
                   (
                       select ep2.Tax
                       from EmployeePayment ep2
                       where ep2.Employee_ID = e.Employee_ID
                         and ep2.Tax is not null
                       order by ep2.Year desc, ep2.WeekNumber desc
                       fetch first 1 row only
                   ) as LastTax,
                   case
                       when ep.Employee_ID is null then 'Pending'
                       else 'Paid'
                   end as PaymentStatus
            from Employees e
            left join EmployeePayment ep
              on ep.Employee_ID = e.Employee_ID
             and ep.Year = ?
             and ep.WeekNumber = ?
            order by e.Employee_ID
            """,
            (rs, rowNum) -> {
                BigDecimal currentPay = rs.getBigDecimal("CurrentPay");
                BigDecimal paymentAmount = weeklyAmount(currentPay);
                BigDecimal tax = valueOrZero(rs.getBigDecimal("LastTax"));
                return new EmployeePaymentStatus(
                    rs.getString("Employee_ID"),
                    rs.getString("EmployeeName"),
                    currentPay,
                    paymentAmount,
                    tax,
                    paymentAmount.subtract(tax),
                    rs.getString("PaymentStatus")
                );
            },
            year,
            weekNumber
        );
    }

    @Transactional
    public PaymentResult registerPayment(PaymentRequest request) {
        Optional<PaymentResult> existing = findPayment(request);
        if (existing.isPresent()) {
            PaymentResult result = existing.get();
            return new PaymentResult(
                "ALREADY_PAID",
                result.employeeId(),
                result.employeeName(),
                result.year(),
                result.weekNumber(),
                result.currentPay(),
                result.paymentAmount(),
                result.tax(),
                result.netPayment(),
                result.payDate(),
                "This employee has already been paid for the requested week."
            );
        }

        List<EmployeePayrollData> employees = jdbcTemplate.query(
            """
            select Employee_ID, FirstName || ' ' || LastName as EmployeeName, CurrentPay
            from Employees
            where Employee_ID = ?
            """,
            (rs, rowNum) -> new EmployeePayrollData(
                rs.getString("Employee_ID"),
                rs.getString("EmployeeName"),
                rs.getBigDecimal("CurrentPay")
            ),
            request.employeeId()
        );

        if (employees.isEmpty()) {
            return new PaymentResult(
                "NOT_FOUND",
                request.employeeId(),
                null,
                request.year(),
                request.weekNumber(),
                null,
                null,
                null,
                null,
                null,
                "Employee was not found."
            );
        }

        EmployeePayrollData employee = employees.get(0);
        BigDecimal paymentAmount = weeklyAmount(employee.currentPay());
        BigDecimal tax = lastTaxForEmployee(employee.employeeId());
        LocalDate payDate = LocalDate.now();

        jdbcTemplate.update(
            """
            insert into EmployeePayment (WeekNumber, Year, Employee_ID, PaymentAmount, Tax, PayDate)
            values (?, ?, ?, ?, ?, ?)
            """,
            request.weekNumber(),
            request.year(),
            employee.employeeId(),
            paymentAmount,
            tax,
            Date.valueOf(payDate)
        );

        return new PaymentResult(
            "REGISTERED",
            employee.employeeId(),
            employee.employeeName(),
            request.year(),
            request.weekNumber(),
            employee.currentPay(),
            paymentAmount,
            tax,
            paymentAmount.subtract(tax),
            payDate,
            "Payment registered successfully."
        );
    }

    public Optional<PaymentResult> findPayment(PaymentRequest request) {
        List<PaymentResult> payments = jdbcTemplate.query(
            """
            select ep.Employee_ID,
                   e.FirstName || ' ' || e.LastName as EmployeeName,
                   e.CurrentPay,
                   ep.Year,
                   ep.WeekNumber,
                   ep.PaymentAmount,
                   ep.Tax,
                   ep.PayDate
            from EmployeePayment ep
            join Employees e on e.Employee_ID = ep.Employee_ID
            where ep.Employee_ID = ?
              and ep.Year = ?
              and ep.WeekNumber = ?
            """,
            (rs, rowNum) -> {
                BigDecimal paymentAmount = rs.getBigDecimal("PaymentAmount");
                BigDecimal tax = valueOrZero(rs.getBigDecimal("Tax"));
                return new PaymentResult(
                    "FOUND",
                    rs.getString("Employee_ID"),
                    rs.getString("EmployeeName"),
                    rs.getInt("Year"),
                    rs.getInt("WeekNumber"),
                    rs.getBigDecimal("CurrentPay"),
                    paymentAmount,
                    tax,
                    paymentAmount.subtract(tax),
                    JdbcMapping.localDate(rs, "PayDate"),
                    "Payment found."
                );
            },
            request.employeeId(),
            request.year(),
            request.weekNumber()
        );
        return payments.stream().findFirst();
    }

    private BigDecimal lastTaxForEmployee(String employeeId) {
        List<BigDecimal> taxes = jdbcTemplate.query(
            """
            select Tax
            from EmployeePayment
            where Employee_ID = ?
              and Tax is not null
            order by Year desc, WeekNumber desc
            fetch first 1 row only
            """,
            (rs, rowNum) -> rs.getBigDecimal("Tax"),
            employeeId
        );
        return taxes.stream().findFirst().orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal weeklyAmount(BigDecimal currentPay) {
        if (currentPay == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return currentPay.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value;
    }

    private record EmployeePayrollData(String employeeId, String employeeName, BigDecimal currentPay) {
    }
}

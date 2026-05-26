package com.constructioncompany.repository;

import com.constructioncompany.api.EmployeeDto;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EmployeeDto> findAll() {
        return jdbcTemplate.query(
            """
            select Employee_ID, Boss_ID, ConstructionSite_ID, FirstName, LastName,
                   StartDate, HireDate, CurrentPay, BankAccount, BankName, Phone
            from Employees
            order by Employee_ID
            """,
            (rs, rowNum) -> mapEmployee(rs)
        );
    }

    public List<EmployeeDto> findByConstructionSite(String constructionSiteId) {
        return jdbcTemplate.query(
            """
            select Employee_ID, Boss_ID, ConstructionSite_ID, FirstName, LastName,
                   StartDate, HireDate, CurrentPay, BankAccount, BankName, Phone
            from Employees
            where ConstructionSite_ID = ?
            order by Employee_ID
            """,
            (rs, rowNum) -> mapEmployee(rs),
            constructionSiteId
        );
    }

    private EmployeeDto mapEmployee(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new EmployeeDto(
            rs.getString("Employee_ID"),
            rs.getString("Boss_ID"),
            rs.getString("ConstructionSite_ID"),
            rs.getString("FirstName"),
            rs.getString("LastName"),
            JdbcMapping.localDate(rs, "StartDate"),
            JdbcMapping.localDate(rs, "HireDate"),
            rs.getBigDecimal("CurrentPay"),
            rs.getString("BankAccount"),
            rs.getString("BankName"),
            rs.getString("Phone")
        );
    }
}

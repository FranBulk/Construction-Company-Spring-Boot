package com.constructioncompany.repository;

import com.constructioncompany.api.CustomerDto;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CustomerDto> findAll() {
        return jdbcTemplate.query(
            """
            select Customer_ID, Name, Phone, Email, CustomerType, RegistrationDate
            from Customers
            order by Customer_ID
            """,
            (rs, rowNum) -> new CustomerDto(
                rs.getString("Customer_ID"),
                rs.getString("Name"),
                rs.getString("Phone"),
                rs.getString("Email"),
                rs.getString("CustomerType"),
                JdbcMapping.localDate(rs, "RegistrationDate")
            )
        );
    }

    public Optional<CustomerDto> findById(String customerId) {
        List<CustomerDto> customers = jdbcTemplate.query(
            """
            select Customer_ID, Name, Phone, Email, CustomerType, RegistrationDate
            from Customers
            where Customer_ID = ?
            """,
            (rs, rowNum) -> new CustomerDto(
                rs.getString("Customer_ID"),
                rs.getString("Name"),
                rs.getString("Phone"),
                rs.getString("Email"),
                rs.getString("CustomerType"),
                JdbcMapping.localDate(rs, "RegistrationDate")
            ),
            customerId
        );
        return customers.stream().findFirst();
    }

    public CustomerDto create(CustomerDto customer) {
        jdbcTemplate.update(
            """
            insert into Customers (Customer_ID, Name, Phone, Email, CustomerType, RegistrationDate)
            values (?, ?, ?, ?, ?, ?)
            """,
            customer.customerId(),
            customer.name(),
            customer.phone(),
            customer.email(),
            customer.customerType(),
            customer.registrationDate()
        );
        return customer;
    }
}

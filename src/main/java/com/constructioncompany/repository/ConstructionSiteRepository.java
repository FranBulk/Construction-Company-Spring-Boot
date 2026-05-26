package com.constructioncompany.repository;

import com.constructioncompany.api.ConstructionSiteDto;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConstructionSiteRepository {

    private final JdbcTemplate jdbcTemplate;

    public ConstructionSiteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ConstructionSiteDto> findAll() {
        return jdbcTemplate.query(
            """
            select cs.ConstructionSite_ID, cs.Customer_ID, c.Name as CustomerName,
                   cs.Country, cs.City, cs.Street, cs.Address
            from ConstructionSite cs
            left join Customers c on c.Customer_ID = cs.Customer_ID
            order by cs.ConstructionSite_ID
            """,
            (rs, rowNum) -> mapSite(rs)
        );
    }

    public Optional<ConstructionSiteDto> findById(String siteId) {
        List<ConstructionSiteDto> sites = jdbcTemplate.query(
            """
            select cs.ConstructionSite_ID, cs.Customer_ID, c.Name as CustomerName,
                   cs.Country, cs.City, cs.Street, cs.Address
            from ConstructionSite cs
            left join Customers c on c.Customer_ID = cs.Customer_ID
            where cs.ConstructionSite_ID = ?
            """,
            (rs, rowNum) -> mapSite(rs),
            siteId
        );
        return sites.stream().findFirst();
    }

    private ConstructionSiteDto mapSite(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ConstructionSiteDto(
            rs.getString("ConstructionSite_ID"),
            rs.getString("Customer_ID"),
            rs.getString("CustomerName"),
            rs.getString("Country"),
            rs.getString("City"),
            rs.getString("Street"),
            rs.getString("Address")
        );
    }
}

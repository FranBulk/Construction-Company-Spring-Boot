package com.constructioncompany.repository;

import com.constructioncompany.api.WarehouseDto;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WarehouseRepository {

    private final JdbcTemplate jdbcTemplate;

    public WarehouseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<WarehouseDto> findAll() {
        return jdbcTemplate.query(
            """
            select WareHouse_ID, ConstructionSite_ID, Name, Country, City, Street, Address, Phone
            from WareHouse
            order by WareHouse_ID
            """,
            (rs, rowNum) -> new WarehouseDto(
                rs.getString("WareHouse_ID"),
                rs.getString("ConstructionSite_ID"),
                rs.getString("Name"),
                rs.getString("Country"),
                rs.getString("City"),
                rs.getString("Street"),
                rs.getString("Address"),
                rs.getString("Phone")
            )
        );
    }
}

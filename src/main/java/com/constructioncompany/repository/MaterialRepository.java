package com.constructioncompany.repository;

import com.constructioncompany.api.MaterialDto;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MaterialRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaterialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MaterialDto> findAll() {
        return jdbcTemplate.query(
            """
            select Material_ID, Name, Purpose
            from Material
            order by Material_ID
            """,
            (rs, rowNum) -> new MaterialDto(
                rs.getString("Material_ID"),
                rs.getString("Name"),
                rs.getString("Purpose")
            )
        );
    }
}

package com.constructioncompany.repository;

import com.constructioncompany.api.MachineDto;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MachineRepository {

    private final JdbcTemplate jdbcTemplate;

    public MachineRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MachineDto> findAll() {
        return jdbcTemplate.query(
            """
            select Machine_ID, Model, Type, IsUsed
            from Machines
            order by Machine_ID
            """,
            (rs, rowNum) -> new MachineDto(
                rs.getString("Machine_ID"),
                rs.getString("Model"),
                rs.getString("Type"),
                rs.getInt("IsUsed") == 1
            )
        );
    }
}

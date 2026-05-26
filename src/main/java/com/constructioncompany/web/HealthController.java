package com.constructioncompany.web;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> health() {
        Integer databaseCheck = jdbcTemplate.queryForObject("select 1 from dual", Integer.class);
        return Map.of(
            "status", "UP",
            "database", databaseCheck != null && databaseCheck == 1 ? "UP" : "UNKNOWN"
        );
    }
}

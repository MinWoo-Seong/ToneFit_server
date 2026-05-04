package com.example.tonefitserver.global.config;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clean() {
        jdbcTemplate.execute("SET session_replication_role = 'replica'");
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
                String.class
        );
        for (String table : tables) {
            jdbcTemplate.execute("TRUNCATE TABLE \"" + table + "\" CASCADE");
        }
        jdbcTemplate.execute("SET session_replication_role = 'DEFAULT'");
    }
}

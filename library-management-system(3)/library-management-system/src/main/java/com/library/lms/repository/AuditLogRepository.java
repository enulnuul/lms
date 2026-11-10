package com.library.lms.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Integer userId, String actionType, String description, String ipAddress) {
        String sql = """
                INSERT INTO AuditLogs (user_id, action_type, description, ip_address)
                VALUES (?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, userId, actionType, description, ipAddress);
    }
}

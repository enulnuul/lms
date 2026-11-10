package com.library.lms.repository;

import com.library.lms.model.UserAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final RowMapper<UserAccount> USER_MAPPER = UserRepository::mapUser;

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserAccount> findById(int userId) {
        String sql = """
                SELECT user_id, username, email, role, is_active
                FROM Users
                WHERE user_id = ?
                """;
        List<UserAccount> users = jdbcTemplate.query(sql, USER_MAPPER, userId);
        return users.stream().findFirst();
    }

    private static UserAccount mapUser(ResultSet rs, int rowNum) throws SQLException {
        UserAccount user = new UserAccount();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    }
}

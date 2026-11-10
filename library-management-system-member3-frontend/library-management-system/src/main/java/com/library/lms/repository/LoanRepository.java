package com.library.lms.repository;

import com.library.lms.model.Loan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class LoanRepository {

    private static final RowMapper<Loan> LOAN_MAPPER = LoanRepository::mapLoan;

    private final JdbcTemplate jdbcTemplate;

    public LoanRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Loan insert(int userId, String bookId, LocalDate dueDate) {
        String sql = """
                INSERT INTO Loans (user_id, book_id, due_date, status, late_fee)
                VALUES (?, ?, ?, 'Active', 0.00)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setString(2, bookId);
            ps.setDate(3, Date.valueOf(dueDate));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Loan insert did not return an ID");
        }
        return findById(key.intValue()).orElseThrow();
    }

    public Optional<Loan> findById(int loanId) {
        String sql = """
                SELECT loan_id, user_id, book_id, borrow_date, due_date, return_date, status, late_fee
                FROM Loans
                WHERE loan_id = ?
                """;
        List<Loan> loans = jdbcTemplate.query(sql, LOAN_MAPPER, loanId);
        return loans.stream().findFirst();
    }

    public Optional<Loan> findActiveByUserAndBook(int userId, String bookId) {
        String sql = """
                SELECT loan_id, user_id, book_id, borrow_date, due_date, return_date, status, late_fee
                FROM Loans
                WHERE user_id = ? AND book_id = ? AND status IN ('Active', 'Overdue')
                ORDER BY loan_id DESC
                LIMIT 1
                """;
        List<Loan> loans = jdbcTemplate.query(sql, LOAN_MAPPER, userId, bookId);
        return loans.stream().findFirst();
    }

    public boolean markReturned(int loanId, LocalDateTime returnDate, BigDecimal lateFee, String status) {
        String sql = """
                UPDATE Loans
                SET return_date = ?, late_fee = ?, status = ?
                WHERE loan_id = ? AND status IN ('Active', 'Overdue')
                """;
        return jdbcTemplate.update(sql, Timestamp.valueOf(returnDate), lateFee, status, loanId) == 1;
    }

    private static Loan mapLoan(ResultSet rs, int rowNum) throws SQLException {
        Loan loan = new Loan();
        loan.setLoanId(rs.getInt("loan_id"));
        loan.setUserId(rs.getInt("user_id"));
        loan.setBookId(rs.getString("book_id"));
        Timestamp borrow = rs.getTimestamp("borrow_date");
        if (borrow != null) {
            loan.setBorrowDate(borrow.toLocalDateTime());
        }
        Date due = rs.getDate("due_date");
        if (due != null) {
            loan.setDueDate(due.toLocalDate());
        }
        Timestamp returned = rs.getTimestamp("return_date");
        if (returned != null) {
            loan.setReturnDate(returned.toLocalDateTime());
        }
        loan.setStatus(rs.getString("status"));
        loan.setLateFee(rs.getBigDecimal("late_fee"));
        return loan;
    }
}

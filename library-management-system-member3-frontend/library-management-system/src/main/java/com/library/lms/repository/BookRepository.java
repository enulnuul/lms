package com.library.lms.repository;

import com.library.lms.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository {

    private static final RowMapper<Book> BOOK_MAPPER = BookRepository::mapBook;

    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Book> findById(String bookId) {
        String sql = """
                SELECT book_id, title, author, isbn, total_copies, available_copies, created_at
                FROM Books
                WHERE book_id = ?
                """;
        List<Book> books = jdbcTemplate.query(sql, BOOK_MAPPER, bookId);
        return books.stream().findFirst();
    }

    public List<Book> search(String title, String author, String isbn, String bookId) {
        StringBuilder sql = new StringBuilder("""
                SELECT book_id, title, author, isbn, total_copies, available_copies, created_at
                FROM Books
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();

        if (title != null) {
            sql.append(" AND title LIKE ? ESCAPE '\\\\' ");
            params.add(title);
        }
        if (author != null) {
            sql.append(" AND author LIKE ? ESCAPE '\\\\' ");
            params.add(author);
        }
        if (isbn != null) {
            sql.append(" AND isbn LIKE ? ESCAPE '\\\\' ");
            params.add(isbn);
        }
        if (bookId != null) {
            sql.append(" AND book_id = ? ");
            params.add(bookId);
        }
        sql.append(" ORDER BY title ASC ");

        return jdbcTemplate.query(sql.toString(), BOOK_MAPPER, params.toArray());
    }

    public boolean decrementAvailableCopy(String bookId) {
        String sql = """
                UPDATE Books
                SET available_copies = available_copies - 1
                WHERE book_id = ? AND available_copies > 0
                """;
        return jdbcTemplate.update(sql, bookId) == 1;
    }

    public boolean incrementAvailableCopy(String bookId) {
        String sql = """
                UPDATE Books
                SET available_copies = available_copies + 1
                WHERE book_id = ? AND available_copies < total_copies
                """;
        return jdbcTemplate.update(sql, bookId) == 1;
    }

    private static Book mapBook(ResultSet rs, int rowNum) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getString("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            book.setCreatedAt(created.toLocalDateTime());
        }
        return book;
    }
}

package com.library.lms.service;

import com.library.lms.dto.BookResponse;
import com.library.lms.dto.BookValidationResponse;
import com.library.lms.exception.ApiException;
import com.library.lms.model.Book;
import com.library.lms.repository.AuditLogRepository;
import com.library.lms.repository.BookRepository;
import com.library.lms.validation.InputValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuditLogRepository auditLogRepository;

    public BookService(BookRepository bookRepository, AuditLogRepository auditLogRepository) {
        this.bookRepository = bookRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<BookResponse> search(String title, String author, String isbn, String bookId, String ipAddress) {
        String titleTerm = InputValidator.optionalSearchTerm(title, "title");
        String authorTerm = InputValidator.optionalSearchTerm(author, "author");
        String isbnTerm = InputValidator.optionalSearchTerm(isbn, "isbn");
        String bookIdTerm = InputValidator.optionalSearchTerm(bookId, "bookId");

        if (titleTerm == null && authorTerm == null && isbnTerm == null && bookIdTerm == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SEARCH",
                    "Provide at least one search field: title, author, isbn, or bookId.");
        }

        if (bookIdTerm != null) {
            bookIdTerm = InputValidator.requireBookId(bookIdTerm);
        }

        String titleLike = titleTerm == null ? null : InputValidator.likeContains(titleTerm);
        String authorLike = authorTerm == null ? null : InputValidator.likeContains(authorTerm);
        String isbnLike = isbnTerm == null ? null : InputValidator.likeContains(isbnTerm);

        List<BookResponse> results = bookRepository.search(titleLike, authorLike, isbnLike, bookIdTerm).stream()
                .map(BookResponse::from)
                .toList();

        auditLogRepository.insert(null, "BOOK_SEARCH",
                "Search executed; resultCount=" + results.size(), ipAddress);
        return results;
    }

    public BookValidationResponse validateBookId(String rawBookId, String ipAddress) {
        String bookId = InputValidator.requireBookId(rawBookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND",
                        "No book exists for the supplied Book ID."));

        auditLogRepository.insert(null, "BOOK_VALIDATE",
                "Validated book_id=" + book.getBookId(), ipAddress);
        return BookValidationResponse.from(book);
    }
}

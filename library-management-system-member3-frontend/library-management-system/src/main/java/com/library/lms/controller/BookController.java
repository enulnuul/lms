package com.library.lms.controller;

import com.library.lms.dto.BookResponse;
import com.library.lms.dto.BookValidationResponse;
import com.library.lms.service.BookService;
import com.library.lms.validation.InputValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String bookId,
            HttpServletRequest request) {
        return ResponseEntity.ok(bookService.search(title, author, isbn, bookId, clientIp(request)));
    }

    @GetMapping("/{bookId}/validate")
    public ResponseEntity<BookValidationResponse> validate(
            @PathVariable String bookId,
            HttpServletRequest request) {
        return ResponseEntity.ok(bookService.validateBookId(bookId, clientIp(request)));
    }

    private String clientIp(HttpServletRequest request) {
        return InputValidator.clientIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    }
}

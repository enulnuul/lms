package com.library.lms.dto;

import com.library.lms.model.Book;

public class BookValidationResponse {

    private boolean valid;
    private String bookId;
    private String title;
    private boolean available;
    private int availableCopies;
    private String message;

    public static BookValidationResponse from(Book book) {
        BookValidationResponse response = new BookValidationResponse();
        response.valid = true;
        response.bookId = book.getBookId();
        response.title = book.getTitle();
        response.available = book.isAvailable();
        response.availableCopies = book.getAvailableCopies();
        response.message = book.isAvailable()
                ? "Book ID is valid and copies are available."
                : "Book ID is valid but no copies are currently available.";
        return response;
    }

    public boolean isValid() {
        return valid;
    }

    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public String getMessage() {
        return message;
    }
}

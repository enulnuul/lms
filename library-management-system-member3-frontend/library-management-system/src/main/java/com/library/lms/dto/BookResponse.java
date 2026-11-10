package com.library.lms.dto;

import com.library.lms.model.Book;

public class BookResponse {

    private String bookId;
    private String title;
    private String author;
    private String isbn;
    private int totalCopies;
    private int availableCopies;
    private boolean available;

    public static BookResponse from(Book book) {
        BookResponse response = new BookResponse();
        response.bookId = book.getBookId();
        response.title = book.getTitle();
        response.author = book.getAuthor();
        response.isbn = book.getIsbn();
        response.totalCopies = book.getTotalCopies();
        response.availableCopies = book.getAvailableCopies();
        response.available = book.isAvailable();
        return response;
    }

    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public boolean isAvailable() {
        return available;
    }
}

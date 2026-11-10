package com.library.lms.dto;

import com.library.lms.model.Loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanResponse {

    private int loanId;
    private int userId;
    private String bookId;
    private LocalDateTime borrowDate;
    private LocalDate dueDate;
    private LocalDateTime returnDate;
    private String status;
    private BigDecimal lateFee;
    private int daysOverdue;

    public static LoanResponse from(Loan loan, int daysOverdue) {
        LoanResponse response = new LoanResponse();
        response.loanId = loan.getLoanId();
        response.userId = loan.getUserId();
        response.bookId = loan.getBookId();
        response.borrowDate = loan.getBorrowDate();
        response.dueDate = loan.getDueDate();
        response.returnDate = loan.getReturnDate();
        response.status = loan.getStatus();
        response.lateFee = loan.getLateFee();
        response.daysOverdue = daysOverdue;
        return response;
    }

    public int getLoanId() {
        return loanId;
    }

    public int getUserId() {
        return userId;
    }

    public String getBookId() {
        return bookId;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public int getDaysOverdue() {
        return daysOverdue;
    }
}

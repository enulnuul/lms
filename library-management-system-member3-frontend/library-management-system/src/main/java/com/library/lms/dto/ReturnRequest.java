package com.library.lms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class ReturnRequest {

    @Min(1)
    private Integer loanId;

    @Min(1)
    private Integer userId;

    @Size(max = 20)
    private String bookId;

    public Integer getLoanId() {
        return loanId;
    }

    public void setLoanId(Integer loanId) {
        this.loanId = loanId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}

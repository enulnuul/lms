package com.library.lms.controller;

import com.library.lms.dto.BorrowRequest;
import com.library.lms.dto.LoanResponse;
import com.library.lms.dto.ReturnRequest;
import com.library.lms.service.LoanService;
import com.library.lms.validation.InputValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/borrow")
    public ResponseEntity<LoanResponse> borrow(
            @Valid @RequestBody BorrowRequest request,
            HttpServletRequest httpRequest) {
        LoanResponse response = loanService.borrow(request, clientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/return")
    public ResponseEntity<LoanResponse> returnBook(
            @Valid @RequestBody ReturnRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(loanService.returnBook(request, clientIp(httpRequest)));
    }

    private String clientIp(HttpServletRequest request) {
        return InputValidator.clientIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    }
}

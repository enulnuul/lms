package com.library.lms.service;

import com.library.lms.config.LibraryProperties;
import com.library.lms.dto.BorrowRequest;
import com.library.lms.dto.LoanResponse;
import com.library.lms.dto.ReturnRequest;
import com.library.lms.exception.ApiException;
import com.library.lms.model.Book;
import com.library.lms.model.Loan;
import com.library.lms.model.UserAccount;
import com.library.lms.repository.AuditLogRepository;
import com.library.lms.repository.BookRepository;
import com.library.lms.repository.LoanRepository;
import com.library.lms.repository.UserRepository;
import com.library.lms.validation.InputValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class LoanService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final AuditLogRepository auditLogRepository;
    private final LateFeeCalculator lateFeeCalculator;
    private final LibraryProperties properties;

    public LoanService(UserRepository userRepository,
                       BookRepository bookRepository,
                       LoanRepository loanRepository,
                       AuditLogRepository auditLogRepository,
                       LateFeeCalculator lateFeeCalculator,
                       LibraryProperties properties) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.auditLogRepository = auditLogRepository;
        this.lateFeeCalculator = lateFeeCalculator;
        this.properties = properties;
    }

    @Transactional
    public LoanResponse borrow(BorrowRequest request, String ipAddress) {
        int userId = InputValidator.requireUserId(request.getUserId());
        String bookId = InputValidator.requireBookId(request.getBookId());

        UserAccount user = requireActiveUser(userId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND",
                        "No book exists for the supplied Book ID."));

        if (!book.isAvailable()) {
            throw new ApiException(HttpStatus.CONFLICT, "BOOK_UNAVAILABLE",
                    "This book has no available copies.");
        }

        if (loanRepository.findActiveByUserAndBook(userId, bookId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_LOAN",
                    "This user already has an active loan for this book.");
        }

        if (!bookRepository.decrementAvailableCopy(bookId)) {
            throw new ApiException(HttpStatus.CONFLICT, "BOOK_UNAVAILABLE",
                    "This book has no available copies.");
        }

        LocalDate dueDate = LocalDate.now().plusDays(properties.getPeriodDays());
        Loan loan = loanRepository.insert(user.getUserId(), bookId, dueDate);

        auditLogRepository.insert(userId, "BOOK_BORROW",
                "Borrowed book_id=" + bookId + " loan_id=" + loan.getLoanId(), ipAddress);
        return LoanResponse.from(loan, 0);
    }

    @Transactional
    public LoanResponse returnBook(ReturnRequest request, String ipAddress) {
        Loan loan = resolveActiveLoan(request);
        LocalDateTime now = LocalDateTime.now();
        int daysOverdue = lateFeeCalculator.daysOverdue(loan.getDueDate(), now.toLocalDate());
        BigDecimal lateFee = lateFeeCalculator.calculate(
                loan.getDueDate(), now.toLocalDate(), properties.getLateFeePerDay());

        if (!loanRepository.markReturned(loan.getLoanId(), now, lateFee, "Returned")) {
            throw new ApiException(HttpStatus.CONFLICT, "LOAN_NOT_ACTIVE",
                    "This loan is not active and cannot be returned.");
        }
        bookRepository.incrementAvailableCopy(loan.getBookId());

        Loan updated = loanRepository.findById(loan.getLoanId()).orElse(loan);
        auditLogRepository.insert(loan.getUserId(), "BOOK_RETURN",
                "Returned loan_id=" + loan.getLoanId() + " late_fee=" + lateFee, ipAddress);
        return LoanResponse.from(updated, daysOverdue);
    }

    private Loan resolveActiveLoan(ReturnRequest request) {
        if (request.getLoanId() != null) {
            int loanId = InputValidator.requireLoanId(request.getLoanId());
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "LOAN_NOT_FOUND",
                            "No loan exists for the supplied loan ID."));
            if (!isOpen(loan)) {
                throw new ApiException(HttpStatus.CONFLICT, "LOAN_NOT_ACTIVE",
                        "This loan is not active and cannot be returned.");
            }
            return loan;
        }

        int userId = InputValidator.requireUserId(request.getUserId());
        String bookId = InputValidator.requireBookId(request.getBookId());
        return loanRepository.findActiveByUserAndBook(userId, bookId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "LOAN_NOT_FOUND",
                        "No active loan was found for this user and book."));
    }

    private UserAccount requireActiveUser(int userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "No user exists for the supplied user ID."));
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "USER_INACTIVE",
                    "This user account is not active.");
        }
        return user;
    }

    private boolean isOpen(Loan loan) {
        return "Active".equalsIgnoreCase(loan.getStatus()) || "Overdue".equalsIgnoreCase(loan.getStatus());
    }
}

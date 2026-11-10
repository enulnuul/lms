package com.library.lms.validation;

import com.library.lms.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.regex.Pattern;

public final class InputValidator {

    public static final int BOOK_ID_MAX_LENGTH = 20;
    public static final int SEARCH_MAX_LENGTH = 100;
    public static final Pattern BOOK_ID_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9-]{0,19}$");
    public static final Pattern ISBN_PATTERN = Pattern.compile("^[0-9Xx-]{10,20}$");

    private InputValidator() {
    }

    public static String requireBookId(String bookId) {
        String value = normalize(bookId);
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_BOOK_ID", "Book ID is required.");
        }
        if (value.length() > BOOK_ID_MAX_LENGTH || !BOOK_ID_PATTERN.matcher(value).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_BOOK_ID",
                    "Book ID must be 1–20 characters using letters, digits, or hyphens.");
        }
        return value;
    }

    public static String optionalSearchTerm(String value, String fieldName) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        if (normalized.length() > SEARCH_MAX_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SEARCH",
                    fieldName + " must be at most " + SEARCH_MAX_LENGTH + " characters.");
        }
        if (containsControlChars(normalized)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SEARCH",
                    fieldName + " contains invalid characters.");
        }
        return normalized;
    }

    public static int requireUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_USER_ID", "A valid user ID is required.");
        }
        return userId;
    }

    public static int requireLoanId(Integer loanId) {
        if (loanId == null || loanId <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_LOAN_ID", "A valid loan ID is required.");
        }
        return loanId;
    }

    public static String likeContains(String term) {
        return "%" + escapeLike(term) + "%";
    }

    public static String escapeLike(String term) {
        return term.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public static String clientIp(String remoteAddr, String forwardedFor) {
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String first = forwardedFor.split(",")[0].trim();
            if (first.length() <= 45 && first.matches("^[A-Za-z0-9.:]+$")) {
                return first;
            }
        }
        if (remoteAddr == null || remoteAddr.isBlank()) {
            return "unknown";
        }
        return remoteAddr.length() > 45 ? remoteAddr.substring(0, 45) : remoteAddr;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private static boolean containsControlChars(String value) {
        return value.chars().anyMatch(ch -> Character.isISOControl(ch));
    }
}

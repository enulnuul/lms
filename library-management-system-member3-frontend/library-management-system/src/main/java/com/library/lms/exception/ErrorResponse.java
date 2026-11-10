package com.library.lms.exception;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {

    private final Instant timestamp = Instant.now();
    private final int status;
    private final String code;
    private final String message;
    private final List<String> details;

    public ErrorResponse(int status, String code, String message, List<String> details) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getDetails() {
        return details;
    }
}

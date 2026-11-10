package com.library.lms.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class LateFeeCalculator {

    public int daysOverdue(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || returnDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(dueDate, returnDate);
        return days > 0 ? (int) days : 0;
    }

    public BigDecimal calculate(LocalDate dueDate, LocalDate returnDate, BigDecimal ratePerDay) {
        int overdue = daysOverdue(dueDate, returnDate);
        if (overdue == 0 || ratePerDay == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return ratePerDay.multiply(BigDecimal.valueOf(overdue)).setScale(2, RoundingMode.HALF_UP);
    }
}

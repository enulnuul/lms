package com.library.lms.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LateFeeCalculatorTest {

    private final LateFeeCalculator calculator = new LateFeeCalculator();

    @Test
    void noFeeWhenReturnedOnOrBeforeDueDate() {
        LocalDate due = LocalDate.of(2026, 11, 20);
        assertEquals(0, calculator.daysOverdue(due, due));
        assertEquals(new BigDecimal("0.00"),
                calculator.calculate(due, due.minusDays(1), new BigDecimal("1.00")));
    }

    @Test
    void feeIsDaysTimesRate() {
        LocalDate due = LocalDate.of(2026, 11, 1);
        LocalDate returned = LocalDate.of(2026, 11, 6);
        assertEquals(5, calculator.daysOverdue(due, returned));
        assertEquals(new BigDecimal("5.00"),
                calculator.calculate(due, returned, new BigDecimal("1.00")));
    }
}

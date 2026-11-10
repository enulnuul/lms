package com.library.lms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "library.loan")
public class LibraryProperties {

    private int periodDays = 14;
    private BigDecimal lateFeePerDay = new BigDecimal("1.00");

    public int getPeriodDays() {
        return periodDays;
    }

    public void setPeriodDays(int periodDays) {
        this.periodDays = periodDays;
    }

    public BigDecimal getLateFeePerDay() {
        return lateFeePerDay;
    }

    public void setLateFeePerDay(BigDecimal lateFeePerDay) {
        this.lateFeePerDay = lateFeePerDay;
    }
}

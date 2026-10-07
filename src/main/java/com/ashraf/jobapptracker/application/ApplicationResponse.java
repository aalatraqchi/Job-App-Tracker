package com.ashraf.jobapptracker.application;

import java.time.LocalDate;

public record ApplicationResponse(
        long id,
        String company,
        String position,
        LocalDate appliedDate,
        ApplicationStatus status,
        String jobUrl,
        String notes
) {
}

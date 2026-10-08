package com.ashraf.jobapptracker.application;

import java.time.LocalDate;

public record UpdateApplicationRequest(
        String company,
        String position,
        LocalDate appliedDate,
        ApplicationStatus status,
        String jobUrl,
        String notes
) {
}

package com.ashraf.jobapptracker.web;

import com.ashraf.jobapptracker.application.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.ashraf.jobapptracker.common.HttpUrl;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ApplicationForm(
        @NotBlank(message = "Enter a company name.")
        @Size(max = 200, message = "Use 200 characters or fewer.") String company,
        @NotBlank(message = "Enter a position.")
        @Size(max = 200, message = "Use 200 characters or fewer.") String position,
        @NotNull(message = "Choose an application date.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appliedDate,
        @NotNull(message = "Choose a status.") ApplicationStatus status,
        @Size(max = 2000, message = "Use 2,000 characters or fewer.")
        @HttpUrl String jobUrl,
        @Size(max = 5000, message = "Use 5,000 characters or fewer.") String notes
) {
}

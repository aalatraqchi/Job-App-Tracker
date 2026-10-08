package com.ashraf.jobapptracker.application;

import java.time.LocalDate;
import com.ashraf.jobapptracker.common.HttpUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
    @NotBlank(message = "Enter a company name.")
    @Size(max = 200, message = "Use 200 characters or fewer.") String company,
    @NotBlank(message = "Enter a position.")
    @Size(max = 200, message = "Use 200 characters or fewer.") String position,
    @NotNull(message = "Choose an application date.") LocalDate appliedDate,
    ApplicationStatus status,
    @HttpUrl @Size(max = 2000, message = "Use 2,000 characters or fewer.") String jobUrl,
    @Size(max = 5000, message = "Use 5,000 characters or fewer.") String notes
) {
}

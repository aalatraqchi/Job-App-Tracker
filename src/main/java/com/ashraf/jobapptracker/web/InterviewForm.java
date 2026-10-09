package com.ashraf.jobapptracker.web;

import com.ashraf.jobapptracker.interview.InterviewOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record InterviewForm(
        @NotBlank(message = "Enter a stage name.")
        @Size(max = 200, message = "Use at most 200 characters.") String stageName,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
        @NotBlank(message = "Enter a UTC offset, for example -04:00 or +00:00.")
        @Pattern(regexp = "Z|[+-](?:0[0-9]|1[0-7]):[0-5][0-9]|[+-]18:00",
                message = "Use a UTC offset such as -04:00 or +00:00.") String utcOffset,
        @NotNull(message = "Choose an outcome.") InterviewOutcome outcome,
        @Size(max = 5000, message = "Use at most 5000 characters.") String notes) {

    public OffsetDateTime scheduledTime() {
        return scheduledAt == null ? null : scheduledAt.atOffset(ZoneOffset.of(utcOffset));
    }
}

package com.ashraf.jobapptracker.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record CreateInterviewRequest(
        @NotBlank(message = "Enter a stage name.")
        @Size(max = 200, message = "Use at most 200 characters.") String stageName,
        OffsetDateTime scheduledAt,
        InterviewOutcome outcome,
        @Size(max = 5000, message = "Use at most 5000 characters.") String notes) {
}

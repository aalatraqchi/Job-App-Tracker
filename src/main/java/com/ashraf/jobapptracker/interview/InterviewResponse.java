package com.ashraf.jobapptracker.interview;

import java.time.OffsetDateTime;

public record InterviewResponse(long id, String stageName, OffsetDateTime scheduledAt,
                                InterviewOutcome outcome, String notes) {
    public static InterviewResponse from(InterviewStage stage) {
        return new InterviewResponse(stage.getId(), stage.getStageName(), stage.getScheduledAt(),
                stage.getOutcome(), stage.getNotes());
    }
}

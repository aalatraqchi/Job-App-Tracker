package com.ashraf.jobapptracker.interview;

import com.ashraf.jobapptracker.application.JobApplication;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "interview_stages")
public class InterviewStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    @Column(nullable = false, length = 200)
    private String stageName;
    private OffsetDateTime scheduledAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewOutcome outcome;
    @Column(length = 5000)
    private String notes;

    protected InterviewStage() {
    }

    public InterviewStage(JobApplication application, String stageName,
                          OffsetDateTime scheduledAt, InterviewOutcome outcome, String notes) {
        this.application = application;
        updateDetails(stageName, scheduledAt, outcome, notes);
    }

    public void updateDetails(String stageName, OffsetDateTime scheduledAt,
                              InterviewOutcome outcome, String notes) {
        this.stageName = stageName;
        this.scheduledAt = scheduledAt;
        this.outcome = outcome;
        this.notes = notes;
    }

    public long getId() { return id; }
    public String getStageName() { return stageName; }
    public OffsetDateTime getScheduledAt() { return scheduledAt; }
    public InterviewOutcome getOutcome() { return outcome; }
    public String getNotes() { return notes; }
}

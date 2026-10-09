package com.ashraf.jobapptracker.interview;

import com.ashraf.jobapptracker.application.ApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InterviewService {
    private final ApplicationService applications;
    private final InterviewRepository repository;

    public InterviewService(ApplicationService applications, InterviewRepository repository) {
        this.applications = applications;
        this.repository = repository;
    }

    @Transactional
    public InterviewStage create(long applicationId, CreateInterviewRequest request) {
        var application = applications.findById(applicationId);
        return repository.save(new InterviewStage(application, request.stageName().strip(),
                request.scheduledAt(), request.outcome() == null ? InterviewOutcome.PENDING : request.outcome(),
                request.notes()));
    }

    public List<InterviewStage> findAll(long applicationId) {
        applications.findById(applicationId);
        return repository.findByApplication_IdOrderByIdAsc(applicationId);
    }

    public InterviewStage findById(long applicationId, long interviewId) {
        applications.findById(applicationId);
        return repository.findByIdAndApplication_Id(interviewId, applicationId)
                .orElseThrow(() -> new InterviewNotFoundException(applicationId, interviewId));
    }

    @Transactional
    public InterviewStage update(long applicationId, long interviewId, UpdateInterviewRequest request) {
        var stage = findById(applicationId, interviewId);
        stage.updateDetails(request.stageName().strip(), request.scheduledAt(),
                request.outcome() == null ? stage.getOutcome() : request.outcome(), request.notes());
        return repository.save(stage);
    }

    @Transactional
    public void delete(long applicationId, long interviewId) {
        repository.delete(findById(applicationId, interviewId));
    }
}

package com.ashraf.jobapptracker.application;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ApplicationService {
    private final Map<Long, JobApplication> applications = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public JobApplication create(CreateApplicationRequest request) {
        ApplicationStatus status = request.status();
        if (status == null) {
            status = ApplicationStatus.APPLIED;
        }

        long id = nextId.getAndIncrement();
        JobApplication application = new JobApplication(
                id, request.company(), request.position(), request.appliedDate(),
                status, request.jobUrl(), request.notes()
        );
        applications.put(id, application);
        return application;
    }

    public List<JobApplication> findAll() {
        return List.copyOf(applications.values());
    }

    public JobApplication findById(long id) {
        JobApplication application = applications.get(id);
        if (application == null) {
            throw new ApplicationNotFoundException(id);
        }
        return application;
    }
}

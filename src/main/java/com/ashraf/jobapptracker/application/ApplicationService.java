package com.ashraf.jobapptracker.application;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Comparator;
import java.util.Locale;
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

    public JobApplication update(long id, UpdateApplicationRequest request) {
        // Check app existence
        JobApplication app = findById(id);

        ApplicationStatus status = request.status();
        if (status == null) {
            status = app.getStatus();
        }

        JobApplication updated = new JobApplication(
                id, request.company(), request.position(), request.appliedDate(),
                status, request.jobUrl(), request.notes()
        );

        if (applications.replace(id, updated) == null){
            throw new ApplicationNotFoundException(id);
        }

        return updated;
    }

    public void delete(long id) {
        if (applications.remove(id) == null) {
            throw new ApplicationNotFoundException(id);
        }
    }

    public List<JobApplication> findAll() {
        return findAll(null, null);
    }

    public List<JobApplication> findAll(ApplicationStatus status, String company) {
        String companyTerm = company == null ? "" : company.strip().toLowerCase(Locale.ROOT);
        Comparator<JobApplication> order = Comparator.comparing(
                JobApplication::getAppliedDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Comparator.comparingLong(JobApplication::getId).reversed());
        return applications.values().stream()
                .filter(application -> status == null || application.getStatus() == status)
                .filter(application -> companyTerm.isEmpty()
                        || (application.getCompany() != null
                        && application.getCompany().toLowerCase(Locale.ROOT).contains(companyTerm)))
                .sorted(order)
                .toList();
    }

    public JobApplication findById(long id) {
        JobApplication application = applications.get(id);
        if (application == null) {
            throw new ApplicationNotFoundException(id);
        }
        return application;
    }
}

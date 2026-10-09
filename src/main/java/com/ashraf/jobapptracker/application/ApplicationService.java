package com.ashraf.jobapptracker.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ApplicationService {
    private final ApplicationRepository repository;

    public ApplicationService(ApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public JobApplication create(CreateApplicationRequest request) {
        ApplicationStatus status = request.status();
        if (status == null) {
            status = ApplicationStatus.APPLIED;
        }

        JobApplication application = new JobApplication(
                request.company(), request.position(), request.appliedDate(),
                status, request.jobUrl(), request.notes()
        );
        return repository.save(application);
    }

    @Transactional
    public JobApplication update(long id, UpdateApplicationRequest request) {
        // Check app existence
        JobApplication app = findById(id);

        ApplicationStatus status = request.status();
        if (status == null) {
            status = app.getStatus();
        }

        app.updateDetails(
                request.company(), request.position(), request.appliedDate(),
                status, request.jobUrl(), request.notes()
        );
        return repository.save(app);
    }

    @Transactional
    public void delete(long id) {
        repository.delete(findById(id));
    }

    public List<JobApplication> findAll() {
        return findAll(null, null);
    }

    public List<JobApplication> findAll(ApplicationStatus status, String company) {
        String companyTerm = company == null ? "" : company.strip().toLowerCase(Locale.ROOT);
        Comparator<JobApplication> order = Comparator.comparing(
                JobApplication::getAppliedDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Comparator.comparingLong(JobApplication::getId).reversed());
        return repository.findAll().stream()
                .filter(application -> status == null || application.getStatus() == status)
                .filter(application -> companyTerm.isEmpty()
                        || (application.getCompany() != null
                        && application.getCompany().toLowerCase(Locale.ROOT).contains(companyTerm)))
                .sorted(order)
                .toList();
    }

    public JobApplication findById(long id) {
        return repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }
}

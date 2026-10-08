package com.ashraf.jobapptracker.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceTests {
    @Test
    void creationCopiesDetailsAndDefaultsOmittedStatus() {
        ApplicationService service = new ApplicationService();
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Acme", "Java Developer", LocalDate.of(2026, 10, 8),
                null, "https://example.com/jobs/1", "Applied through company website"
        );

        JobApplication application = service.create(request);

        assertTrue(application.getId() > 0);
        assertEquals(request.company(), application.getCompany());
        assertEquals(request.position(), application.getPosition());
        assertEquals(request.appliedDate(), application.getAppliedDate());
        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
        assertEquals(request.jobUrl(), application.getJobUrl());
        assertEquals(request.notes(), application.getNotes());
    }

    @Test
    void identicalRequestsCreateDistinctStoredApplications() {
        ApplicationService service = new ApplicationService();
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Acme", "Java Developer", LocalDate.of(2026, 10, 8),
                null, null, null
        );

        JobApplication first = service.create(request);
        JobApplication second = service.create(request);
        var stored = service.findAll();

        assertNotEquals(first.getId(), second.getId());
        assertEquals(2, stored.size());
        assertTrue(stored.contains(first));
        assertTrue(stored.contains(second));
    }
}

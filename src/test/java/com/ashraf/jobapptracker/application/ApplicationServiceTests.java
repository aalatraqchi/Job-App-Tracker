package com.ashraf.jobapptracker.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceTests {
    @Test
    void updateReplacesDetailsAndReturnsStoredReplacementWithoutAllocatingId() {
        ApplicationService service = new ApplicationService();
        CreateApplicationRequest original = new CreateApplicationRequest(
                "Acme", "Java Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.APPLIED, null, "Original notes"
        );
        JobApplication first = service.create(original);
        JobApplication other = service.create(original);
        UpdateApplicationRequest request = new UpdateApplicationRequest(
                "New Company", "Backend Developer", LocalDate.of(2026, 10, 9),
                ApplicationStatus.INTERVIEW, "https://example.com/jobs/2", "Interview scheduled"
        );

        JobApplication updated = service.update(first.getId(), request);

        assertEquals(first.getId(), updated.getId());
        assertEquals(request.company(), updated.getCompany());
        assertEquals(request.position(), updated.getPosition());
        assertEquals(request.appliedDate(), updated.getAppliedDate());
        assertEquals(request.status(), updated.getStatus());
        assertEquals(request.jobUrl(), updated.getJobUrl());
        assertEquals(request.notes(), updated.getNotes());
        assertSame(updated, service.findById(first.getId()));
        assertSame(other, service.findById(other.getId()));
        assertEquals(2, service.findAll().size());
        assertEquals(other.getId() + 1, service.create(original).getId());
    }

    @Test
    void updatePreservesExistingStatusWhenOmittedAndClearsOptionalFields() {
        ApplicationService service = new ApplicationService();
        JobApplication application = service.create(new CreateApplicationRequest(
                "Acme", "Java Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.INTERVIEW, "https://example.com/jobs/1", "Old notes"
        ));

        JobApplication updated = service.update(application.getId(), new UpdateApplicationRequest(
                "Acme", "Java Developer", application.getAppliedDate(), null, null, null
        ));

        assertEquals(ApplicationStatus.INTERVIEW, updated.getStatus());
        assertEquals(null, updated.getJobUrl());
        assertEquals(null, updated.getNotes());
        assertSame(updated, service.findById(application.getId()));
    }

    @Test
    void updatingMissingIdThrowsWithoutInsertingApplication() {
        ApplicationService service = new ApplicationService();
        UpdateApplicationRequest request = new UpdateApplicationRequest(
                "Acme", "Java Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.APPLIED, null, null
        );

        assertThrows(ApplicationNotFoundException.class, () -> service.update(999, request));
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void findsTheApplicationWithTheRequestedId() {
        ApplicationService service = new ApplicationService();
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Acme", "Java Developer", LocalDate.of(2026, 10, 8),
                null, null, null
        );
        JobApplication first = service.create(request);
        JobApplication second = service.create(request);

        assertSame(second, service.findById(second.getId()));
        assertSame(first, service.findById(first.getId()));
    }

    @Test
    void missingApplicationThrowsNotFoundException() {
        ApplicationService service = new ApplicationService();

        assertThrows(ApplicationNotFoundException.class, () -> service.findById(999));
    }

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

    @Test
    void testingCustomStatusEnumPassedIn() {
        ApplicationService service = new ApplicationService();
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Acme", "Java Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.INTERVIEW, "https://example.com/jobs/1", "Applied through company website"
        );

        JobApplication application = service.create(request);

        assertTrue(application.getId() > 0);
        assertEquals(request.company(), application.getCompany());
        assertEquals(request.position(), application.getPosition());
        assertEquals(request.appliedDate(), application.getAppliedDate());
        assertEquals(ApplicationStatus.INTERVIEW, application.getStatus());
        assertEquals(request.jobUrl(), application.getJobUrl());
        assertEquals(request.notes(), application.getNotes());
    }
}

package com.ashraf.jobapptracker.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceTests {
    @Test
    void filtersCombineCompanySubstringAndStatusWithoutChangingStorage() {
        ApplicationService service = new ApplicationService();
        JobApplication applied = service.create(new CreateApplicationRequest(
                "Acme Labs", "Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.APPLIED, null, null));
        JobApplication interview = service.create(new CreateApplicationRequest(
                "ACME Group", "Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.INTERVIEW, null, null));
        JobApplication other = service.create(new CreateApplicationRequest(
                "Other Company", "Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.INTERVIEW, null, null));

        assertEquals(List.of(interview, applied), service.findAll(null, "  cMe  "));
        assertEquals(List.of(other, interview), service.findAll(ApplicationStatus.INTERVIEW, null));
        assertEquals(List.of(interview), service.findAll(ApplicationStatus.INTERVIEW, "acme"));
        assertTrue(service.findAll(ApplicationStatus.OFFER, "acme").isEmpty());
        assertTrue(service.findAll(null, "missing").isEmpty());
        assertEquals(service.findAll(), service.findAll(null, "  "));
        assertEquals(3, service.findAll().size());
    }

    @Test
    void listOrdersDateDescendingThenIdDescendingAndPlacesMissingDatesLast() {
        ApplicationService service = new ApplicationService();
        JobApplication older = service.create(new CreateApplicationRequest(
                "Acme", "Developer", LocalDate.of(2026, 10, 7), null, null, null));
        JobApplication firstNewer = service.create(new CreateApplicationRequest(
                "Acme", "Developer", LocalDate.of(2026, 10, 9), null, null, null));
        JobApplication lastNewer = service.create(new CreateApplicationRequest(
                "Acme", "Developer", LocalDate.of(2026, 10, 9), null, null, null));
        JobApplication undated = service.create(new CreateApplicationRequest(
                "Acme", "Developer", null, null, null, null));

        assertEquals(List.of(lastNewer, firstNewer, older, undated), service.findAll());
        assertEquals(service.findAll(), service.findAll(ApplicationStatus.APPLIED, "ACME"));
    }

    @Test
    void deletionRemovesOnlyRequestedApplicationAndRejectsMissingIds() {
        ApplicationService service = new ApplicationService();
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Acme", "Developer", LocalDate.of(2026, 10, 8), null, null, null);
        JobApplication removed = service.create(request);
        JobApplication retained = service.create(request);

        service.delete(removed.getId());

        assertEquals(1, service.findAll().size());
        assertSame(retained, service.findById(retained.getId()));
        assertThrows(ApplicationNotFoundException.class, () -> service.findById(removed.getId()));
        assertThrows(ApplicationNotFoundException.class, () -> service.delete(removed.getId()));
        assertThrows(ApplicationNotFoundException.class, () -> service.delete(999));
        assertEquals(1, service.findAll().size());
    }

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

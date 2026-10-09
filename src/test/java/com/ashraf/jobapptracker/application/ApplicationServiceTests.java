package com.ashraf.jobapptracker.application;

import com.ashraf.jobapptracker.PostgresTestSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationServiceTests extends PostgresTestSupport {
    @Autowired
    private ApplicationService service;

    @Test
    void filtersCombineCompanySubstringAndStatusWithoutChangingStorage() {
        JobApplication applied = service.create(new CreateApplicationRequest(
                "Acme Labs", "Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.APPLIED, null, null));
        JobApplication interview = service.create(new CreateApplicationRequest(
                "ACME Group", "Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.INTERVIEW, null, null));
        JobApplication other = service.create(new CreateApplicationRequest(
                "Other Company", "Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.INTERVIEW, null, null));

        assertEquals(List.of(interview.getId(), applied.getId()), ids(service.findAll(null, "  cMe  ")));
        assertEquals(List.of(other.getId(), interview.getId()), ids(service.findAll(ApplicationStatus.INTERVIEW, null)));
        assertEquals(List.of(interview.getId()), ids(service.findAll(ApplicationStatus.INTERVIEW, "acme")));
        assertTrue(service.findAll(ApplicationStatus.OFFER, "acme").isEmpty());
        assertTrue(service.findAll(null, "missing").isEmpty());
        assertEquals(ids(service.findAll()), ids(service.findAll(null, "  ")));
        assertEquals(3, service.findAll().size());
    }

    @Test
    void listOrdersDateDescendingThenIdDescending() {
        JobApplication older = service.create(new CreateApplicationRequest(
                "Acme", "Developer", LocalDate.of(2026, 10, 7), null, null, null));
        JobApplication firstNewer = service.create(new CreateApplicationRequest(
                "Acme", "Developer", LocalDate.of(2026, 10, 9), null, null, null));
        JobApplication lastNewer = service.create(new CreateApplicationRequest(
                "Acme", "Developer", LocalDate.of(2026, 10, 9), null, null, null));

        assertEquals(List.of(lastNewer.getId(), firstNewer.getId(), older.getId()), ids(service.findAll()));
        assertEquals(ids(service.findAll()), ids(service.findAll(ApplicationStatus.APPLIED, "ACME")));
    }

    @Test
    void deletionRemovesOnlyRequestedApplicationAndRejectsMissingIds() {
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Acme", "Developer", LocalDate.of(2026, 10, 8), null, null, null);
        JobApplication removed = service.create(request);
        JobApplication retained = service.create(request);

        service.delete(removed.getId());

        assertEquals(1, service.findAll().size());
        assertApplicationEquals(retained, service.findById(retained.getId()));
        assertThrows(ApplicationNotFoundException.class, () -> service.findById(removed.getId()));
        assertThrows(ApplicationNotFoundException.class, () -> service.delete(removed.getId()));
        assertThrows(ApplicationNotFoundException.class, () -> service.delete(999));
        assertEquals(1, service.findAll().size());
    }

    @Test
    void updateReplacesDetailsAndReturnsStoredReplacementWithoutAllocatingId() {
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
        assertApplicationEquals(updated, service.findById(first.getId()));
        assertApplicationEquals(other, service.findById(other.getId()));
        assertEquals(2, service.findAll().size());
        assertEquals(other.getId() + 1, service.create(original).getId());
    }

    @Test
    void updatePreservesExistingStatusWhenOmittedAndClearsOptionalFields() {
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
        assertApplicationEquals(updated, service.findById(application.getId()));
    }

    @Test
    void updatingMissingIdThrowsWithoutInsertingApplication() {
        UpdateApplicationRequest request = new UpdateApplicationRequest(
                "Acme", "Java Developer", LocalDate.of(2026, 10, 8),
                ApplicationStatus.APPLIED, null, null
        );

        assertThrows(ApplicationNotFoundException.class, () -> service.update(999, request));
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void findsTheApplicationWithTheRequestedId() {
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Acme", "Java Developer", LocalDate.of(2026, 10, 8),
                null, null, null
        );
        JobApplication first = service.create(request);
        JobApplication second = service.create(request);

        assertApplicationEquals(second, service.findById(second.getId()));
        assertApplicationEquals(first, service.findById(first.getId()));
    }

    @Test
    void missingApplicationThrowsNotFoundException() {

        assertThrows(ApplicationNotFoundException.class, () -> service.findById(999));
    }

    @Test
    void creationCopiesDetailsAndDefaultsOmittedStatus() {
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
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Acme", "Java Developer", LocalDate.of(2026, 10, 8),
                null, null, null
        );

        JobApplication first = service.create(request);
        JobApplication second = service.create(request);
        var stored = service.findAll();

        assertNotEquals(first.getId(), second.getId());
        assertEquals(2, stored.size());
        assertTrue(ids(stored).contains(first.getId()));
        assertTrue(ids(stored).contains(second.getId()));
    }

    @Test
    void testingCustomStatusEnumPassedIn() {
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
    private List<Long> ids(List<JobApplication> applications) {
        return applications.stream().map(JobApplication::getId).toList();
    }

    private void assertApplicationEquals(JobApplication expected, JobApplication actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCompany(), actual.getCompany());
        assertEquals(expected.getPosition(), actual.getPosition());
        assertEquals(expected.getAppliedDate(), actual.getAppliedDate());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getJobUrl(), actual.getJobUrl());
        assertEquals(expected.getNotes(), actual.getNotes());
    }
}

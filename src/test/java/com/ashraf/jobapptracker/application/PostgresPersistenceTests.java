package com.ashraf.jobapptracker.application;

import com.ashraf.jobapptracker.JobAppTrackerApplication;
import com.ashraf.jobapptracker.PostgresTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostgresPersistenceTests extends PostgresTestSupport {
    @Test
    void applicationSurvivesClosingAndRestartingTheSpringContext() {
        long id;
        try (var firstRun = startApplication()) {
            var service = firstRun.getBean(ApplicationService.class);
            id = service.create(new CreateApplicationRequest(
                    "Persistent Company", "Java Developer", LocalDate.of(2026, 10, 8),
                    ApplicationStatus.INTERVIEW, "https://example.com/jobs/1", "Keep after restart"
            )).getId();
        }

        try (var restarted = startApplication()) {
            var service = restarted.getBean(ApplicationService.class);
            var restored = service.findById(id);
            assertEquals("Persistent Company", restored.getCompany());
            assertEquals("Java Developer", restored.getPosition());
            assertEquals(LocalDate.of(2026, 10, 8), restored.getAppliedDate());
            assertEquals(ApplicationStatus.INTERVIEW, restored.getStatus());
            assertEquals("https://example.com/jobs/1", restored.getJobUrl());
            assertEquals("Keep after restart", restored.getNotes());
            assertEquals(1, service.findAll(ApplicationStatus.INTERVIEW, "persistent").size());
            long nextId = service.create(new CreateApplicationRequest(
                    "Next Company", "Developer", LocalDate.of(2026, 10, 8), null, null, null)).getId();
            assertTrue(nextId > id);
        }
    }

    private ConfigurableApplicationContext startApplication() {
        return new SpringApplicationBuilder(JobAppTrackerApplication.class)
                .web(WebApplicationType.NONE)
                .run("--spring.config.import=", "--spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                        "--spring.datasource.username=" + POSTGRES.getUsername(),
                        "--spring.datasource.password=" + POSTGRES.getPassword(),
                        "--spring.jpa.hibernate.ddl-auto=update");
    }
}

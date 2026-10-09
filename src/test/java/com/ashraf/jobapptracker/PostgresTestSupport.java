package com.ashraf.jobapptracker;

import com.ashraf.jobapptracker.application.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

public abstract class PostgresTestSupport {
    protected static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("job_app_tracker_test")
            .withUsername("tracker_test")
            .withPassword(UUID.randomUUID().toString());

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.config.import", () -> "");
    }

    @Autowired
    private ApplicationRepository testRepository;

    @BeforeEach
    void clearTestDatabase() {
        testRepository.deleteAll();
    }
}

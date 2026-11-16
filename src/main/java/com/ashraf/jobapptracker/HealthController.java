package com.ashraf.jobapptracker;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "job-app-tracker");
    }

    @GetMapping("/about")
    public Map<String, String> about() {
        return Map.of("project", "Job Application Tracker", "purpose", "Track job applications and interview stages");
    }
}
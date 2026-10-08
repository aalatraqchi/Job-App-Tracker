package com.ashraf.jobapptracker.application;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications")
public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@RequestBody CreateApplicationRequest request) {
        JobApplication application = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(application));
    }

    private ApplicationResponse toResponse(JobApplication application) {
        return new ApplicationResponse(
                application.getId(), application.getCompany(), application.getPosition(),
                application.getAppliedDate(), application.getStatus(),
                application.getJobUrl(), application.getNotes()
        );
    }
}

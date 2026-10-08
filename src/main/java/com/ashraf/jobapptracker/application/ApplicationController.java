package com.ashraf.jobapptracker.application;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/applications")
public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody CreateApplicationRequest request) {
        JobApplication application = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(application));
    }

    @GetMapping("/{id}")
    public ApplicationResponse getById(@PathVariable("id") long id) {
        return toResponse(service.findById(id));
    }

    @PutMapping("/{id}")
    public ApplicationResponse updateById(@PathVariable("id") long id, @Valid @RequestBody UpdateApplicationRequest request) {
        return toResponse(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") long id) {
        service.delete(id);
    }

    private ApplicationResponse toResponse(JobApplication application) {
        return new ApplicationResponse(
                application.getId(), application.getCompany(), application.getPosition(),
                application.getAppliedDate(), application.getStatus(),
                application.getJobUrl(), application.getNotes()
        );
    }

    @GetMapping
    public List<ApplicationResponse> listApps(){
        List<ApplicationResponse> list = new ArrayList<>();

        // Append list
        for (JobApplication application : service.findAll()){
            list.add(toResponse(application));
        }

        return list;
    }
}

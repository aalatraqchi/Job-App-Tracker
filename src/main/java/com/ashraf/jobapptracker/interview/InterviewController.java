package com.ashraf.jobapptracker.interview;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/applications/{applicationId}/interviews")
public class InterviewController {
    private final InterviewService service;

    public InterviewController(InterviewService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> create(@PathVariable("applicationId") long applicationId,
            @Valid @RequestBody CreateInterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InterviewResponse.from(service.create(applicationId, request)));
    }

    @GetMapping
    public List<InterviewResponse> list(@PathVariable("applicationId") long applicationId) {
        return service.findAll(applicationId).stream().map(InterviewResponse::from).toList();
    }

    @PutMapping("/{interviewId}")
    public InterviewResponse update(@PathVariable("applicationId") long applicationId,
            @PathVariable("interviewId") long interviewId, @Valid @RequestBody UpdateInterviewRequest request) {
        return InterviewResponse.from(service.update(applicationId, interviewId, request));
    }

    @DeleteMapping("/{interviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("applicationId") long applicationId,
                       @PathVariable("interviewId") long interviewId) {
        service.delete(applicationId, interviewId);
    }
}

package com.ashraf.jobapptracker.interview;

public class InterviewNotFoundException extends RuntimeException {
    public InterviewNotFoundException(long applicationId, long interviewId) {
        super("Interview " + interviewId + " was not found for application " + applicationId);
    }
}

package com.ashraf.jobapptracker.application;

import java.time.LocalDate;

public class JobApplication {
    private long id;
    private String company;
    private String position;
    private LocalDate appliedDate;
    private ApplicationStatus status;

    // Going to be optional during input validation
    private String jobUrl;
    private String notes;

    // Constructor
    public JobApplication(long id, String company, String position, LocalDate date, ApplicationStatus status, String url, String notes) {
        this.id = id;
        this.company = company;
        this.position = position;
        this.appliedDate = date;
        this.status = status;
        this.jobUrl = url;
        this.notes = notes;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getPosition(){
        return position;
    }

    public LocalDate getAppliedDate(){
        return appliedDate;
    }

    public ApplicationStatus getStatus(){
        return status;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public String getNotes() {
        return notes;
    }
}

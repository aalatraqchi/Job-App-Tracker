package com.ashraf.jobapptracker.application;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import com.ashraf.jobapptracker.interview.InterviewStage;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, length = 200)
    private String company;
    @Column(nullable = false, length = 200)
    private String position;
    @Column(nullable = false)
    private LocalDate appliedDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    // Going to be optional during input validation
    @Column(length = 2000)
    private String jobUrl;
    @Column(length = 5000)
    private String notes;

    @OneToMany(mappedBy = "application", cascade = CascadeType.REMOVE)
    private List<InterviewStage> interviews = new ArrayList<>();

    protected JobApplication() {
    }

    public JobApplication(String company, String position, LocalDate date,
                          ApplicationStatus status, String url, String notes) {
        this(0, company, position, date, status, url, notes);
    }

    public void updateDetails(String company, String position, LocalDate date,
                              ApplicationStatus status, String url, String notes) {
        this.company = company;
        this.position = position;
        this.appliedDate = date;
        this.status = status;
        this.jobUrl = url;
        this.notes = notes;
    }

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

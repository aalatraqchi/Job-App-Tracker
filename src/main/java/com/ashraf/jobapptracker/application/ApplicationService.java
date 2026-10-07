package com.ashraf.jobapptracker.application;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApplicationService {
    private final Map<Long, JobApplication> applications = new ConcurrentHashMap<>();

    public List<JobApplication> findAll() {
        return List.copyOf(applications.values());
    }
}

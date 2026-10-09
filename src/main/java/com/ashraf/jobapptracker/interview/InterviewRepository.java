package com.ashraf.jobapptracker.interview;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<InterviewStage, Long> {
    List<InterviewStage> findByApplication_IdOrderByIdAsc(long applicationId);
    Optional<InterviewStage> findByIdAndApplication_Id(long id, long applicationId);
}

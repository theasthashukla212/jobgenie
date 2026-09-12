package com.jobgenie.jobgenie_backend.repository;

import com.jobgenie.jobgenie_backend.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    @Query("SELECT j FROM Job j WHERE LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Job> searchJobs(@Param("keyword") String keyword, Pageable pageable);
    
    List<Job> findByType(String type);
}

package com.jobgenie.jobgenie_backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jobgenie.jobgenie_backend.model.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    @Query("SELECT j FROM Job j WHERE (LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:type IS NULL OR LOWER(j.type) = LOWER(:type))")
    Page<Job> searchJobs(@Param("keyword") String keyword, @Param("type") String type, Pageable pageable);

    Page<Job> findByTypeIgnoreCase(String type, Pageable pageable);
    
    List<Job> findByType(String type);
}

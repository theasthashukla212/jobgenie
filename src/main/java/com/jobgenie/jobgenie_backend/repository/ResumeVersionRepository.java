package com.jobgenie.jobgenie_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jobgenie.jobgenie_backend.model.ResumeVersion;
import com.jobgenie.jobgenie_backend.model.User;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, Long> {
    List<ResumeVersion> findByResumeIdAndResumeUserOrderByVersionNumberDesc(Long resumeId, User user);
    Optional<ResumeVersion> findByIdAndResumeIdAndResumeUser(Long id, Long resumeId, User user);
    int countByResumeId(Long resumeId);
}

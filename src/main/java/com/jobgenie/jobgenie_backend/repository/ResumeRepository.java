package com.jobgenie.jobgenie_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jobgenie.jobgenie_backend.model.Resume;
import com.jobgenie.jobgenie_backend.model.User;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUser(User user);
    Optional<Resume> findByIdAndUser(Long id, User user);
    boolean existsByUserAndIsDefaultTrue(User user);
}

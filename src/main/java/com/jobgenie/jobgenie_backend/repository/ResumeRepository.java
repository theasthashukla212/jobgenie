package com.jobgenie.jobgenie_backend.repository;

import com.jobgenie.jobgenie_backend.model.Resume;
import com.jobgenie.jobgenie_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUser(User user);
}

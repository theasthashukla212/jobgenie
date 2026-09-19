package com.jobgenie.jobgenie_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jobgenie.jobgenie_backend.model.Application;
import com.jobgenie.jobgenie_backend.model.User;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    @EntityGraph(attributePaths = {"job", "resume"})
    List<Application> findByUser(User user);
    @EntityGraph(attributePaths = {"job", "resume"})
    List<Application> findByUserOrderByUpdatedAtDesc(User user);
    @EntityGraph(attributePaths = {"job", "resume"})
    Optional<Application> findByIdAndUser(Long id, User user);
    @EntityGraph(attributePaths = {"job", "resume"})
    List<Application> findByUserAndStatusOrderByUpdatedAtDesc(User user, Application.Status status);
}

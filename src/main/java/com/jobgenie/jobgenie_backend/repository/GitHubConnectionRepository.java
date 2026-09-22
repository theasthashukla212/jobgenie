package com.jobgenie.jobgenie_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jobgenie.jobgenie_backend.model.GitHubConnection;
import com.jobgenie.jobgenie_backend.model.User;

@Repository
public interface GitHubConnectionRepository extends JpaRepository<GitHubConnection, Long> {
    Optional<GitHubConnection> findByUser(User user);
    Optional<GitHubConnection> findByUserId(Long userId);
    Optional<GitHubConnection> findByGithubUsername(String githubUsername);
    void deleteByUser(User user);
}

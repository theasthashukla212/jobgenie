package com.jobgenie.jobgenie_backend.repository;

import com.jobgenie.jobgenie_backend.model.Application;
import com.jobgenie.jobgenie_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUser(User user);
}

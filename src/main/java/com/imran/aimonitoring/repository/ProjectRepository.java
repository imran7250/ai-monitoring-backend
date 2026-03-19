package com.imran.aimonitoring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imran.aimonitoring.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwnerId(Long ownerId);
    
    Optional<Project> findByIdAndOwnerEmail(Long id, String email);
}

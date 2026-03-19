package com.imran.aimonitoring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.imran.aimonitoring.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    // ✅ NEW — needed for last-admin guard
    long countByRole(String role);
 
    @Query("""
    	    SELECT COUNT(p) > 0
    	    FROM Project p
    	    WHERE p.owner.id = :userId
    	""")
    	boolean existsUserWithProjects(Long userId);
    
}


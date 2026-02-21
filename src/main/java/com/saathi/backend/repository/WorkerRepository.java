package com.saathi.backend.repository;

import java.util.Optional;
import com.saathi.backend.model.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerRepository extends JpaRepository<WorkerProfile, Long> {

    List<WorkerProfile> findByIsAvailableTrue();

    List<WorkerProfile> findByCityAndIsAvailableTrue(String city);

    // LIKE search inside comma-separated skills string
    @Query("SELECT w FROM WorkerProfile w " +
            "WHERE w.skills LIKE %:skill% " +
            "AND w.isAvailable = true")
    List<WorkerProfile> findBySkillsContaining(
            @Param("skill") String skill
    );

    @Query("SELECT w FROM WorkerProfile w " +
            "WHERE w.skills LIKE %:skill% " +
            "AND w.city = :city " +
            "AND w.isAvailable = true " +
            "ORDER BY w.rating DESC")
    List<WorkerProfile> findBySkillAndCity(
            @Param("skill") String skill,
            @Param("city") String city
    );



    // find worker profile by user email
    Optional<WorkerProfile> findByUserEmail(String email);
}
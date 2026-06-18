package com.bloodcircle.repository;

import com.bloodcircle.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);

    long countByIsFulfilledFalse();

    // Blood group distribution
    @Query("SELECT p.bloodGroupRequired, COUNT(p) FROM Patient p GROUP BY p.bloodGroupRequired")
    List<Object[]> countByBloodGroupRequired();

    // Admin: urgent unfulfilled patients
    List<Patient> findTop5ByUrgencyLevelAndIsFulfilledFalseOrderByCreatedAtDesc(String urgencyLevel);

    // Admin filters
    Page<Patient> findByBloodGroupRequired(String bloodGroup, Pageable pageable);

    Page<Patient> findByUrgencyLevel(String urgencyLevel, Pageable pageable);

    Page<Patient> findByIsFulfilledTrue(Pageable pageable);

    Page<Patient> findByIsFulfilledFalse(Pageable pageable);

    Page<Patient> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Patient> findByBloodGroupRequiredAndFullNameContainingIgnoreCase(String bloodGroup, String name, Pageable pageable);
}

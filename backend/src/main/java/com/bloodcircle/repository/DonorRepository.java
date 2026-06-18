package com.bloodcircle.repository;

import com.bloodcircle.model.Donor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    Optional<Donor> findByUserId(Long userId);

    long countByIsAvailableTrue();

    // Blood group distribution
    @Query("SELECT d.bloodGroup, COUNT(d) FROM Donor d GROUP BY d.bloodGroup")
    List<Object[]> countByBloodGroup();

    // Search with filters
    Page<Donor> findByBloodGroupInAndIsAvailableTrue(List<String> bloodGroups, Pageable pageable);

    Page<Donor> findByBloodGroupInAndIsAvailableTrueAndCityContainingIgnoreCase(
            List<String> bloodGroups, String city, Pageable pageable);

    Page<Donor> findByBloodGroupAndIsAvailableTrue(String bloodGroup, Pageable pageable);

    Page<Donor> findByBloodGroupAndIsAvailableTrueAndCityContainingIgnoreCase(
            String bloodGroup, String city, Pageable pageable);

    // Admin: filter by blood group
    Page<Donor> findByBloodGroup(String bloodGroup, Pageable pageable);

    Page<Donor> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Donor> findByBloodGroupAndFullNameContainingIgnoreCase(String bloodGroup, String name, Pageable pageable);

    Page<Donor> findByIsAvailableTrue(Pageable pageable);

    Page<Donor> findByIsAvailableFalse(Pageable pageable);

    // Patient dashboard: compatible donors in same city excluding current user
    List<Donor> findTop10ByBloodGroupInAndIsAvailableTrueAndCityContainingIgnoreCaseAndUserIdNot(
            List<String> bloodGroups, String city, Long userId);

    // Full search with all filters excluding current user
    @Query("SELECT d FROM Donor d WHERE " +
            "d.userId != :userId AND " +
            "(:bloodGroup IS NULL OR d.bloodGroup = :bloodGroup) AND " +
            "(:city IS NULL OR LOWER(d.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
            "(:state IS NULL OR LOWER(d.state) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
            "(:availableOnly = false OR d.isAvailable = true)")
    Page<Donor> searchDonors(String bloodGroup, String city, String state, boolean availableOnly, Long userId, Pageable pageable);

    @Query("SELECT d FROM Donor d WHERE " +
            "d.userId != :userId AND " +
            "d.bloodGroup IN :compatibleGroups AND " +
            "(:city IS NULL OR LOWER(d.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
            "(:state IS NULL OR LOWER(d.state) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
            "(:availableOnly = false OR d.isAvailable = true)")
    Page<Donor> searchDonorsCompatible(List<String> compatibleGroups, String city, String state,
                                       boolean availableOnly, Long userId, Pageable pageable);
}

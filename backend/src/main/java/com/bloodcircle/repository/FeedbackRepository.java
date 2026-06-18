package com.bloodcircle.repository;

import com.bloodcircle.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    long countByIsResolvedFalse();

    Page<Feedback> findByIsResolvedFalse(Pageable pageable);

    Page<Feedback> findByIsResolvedTrue(Pageable pageable);

    List<Feedback> findTop5ByOrderByCreatedAtDesc();
}

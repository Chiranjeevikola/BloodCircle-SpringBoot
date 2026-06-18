package com.bloodcircle.repository;

import com.bloodcircle.model.OtpResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpResetTokenRepository extends JpaRepository<OtpResetToken, Long> {

    Optional<OtpResetToken> findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);

    void deleteAllByEmail(String email);
}

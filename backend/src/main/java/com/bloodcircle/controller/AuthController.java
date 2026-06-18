package com.bloodcircle.controller;

import com.bloodcircle.model.OtpResetToken;
import com.bloodcircle.model.User;
import com.bloodcircle.repository.OtpResetTokenRepository;
import com.bloodcircle.repository.UserRepository;
import com.bloodcircle.security.JwtUtil;
import com.bloodcircle.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpResetTokenRepository otpRepository;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 5;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil, OtpResetTokenRepository otpRepository,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return (Long) auth.getPrincipal();
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> data) {
        String email = data.getOrDefault("email", "").toLowerCase().trim();
        String password = data.getOrDefault("password", "");
        String confirm = data.getOrDefault("confirm_password", "");

        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required."));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters."));
        }
        if (!password.equals(confirm)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match."));
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered."));
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(null);
        user.setVerified(true);
        user.setActive(true);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());
        String refresh = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", token);
        response.put("refresh_token", refresh);
        response.put("user", user.toMap());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> data) {
        String email = data.getOrDefault("email", "").toLowerCase().trim();
        String password = data.getOrDefault("password", "");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password."));
        }
        if (user.isBlocked()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Your account has been blocked. Contact support."));
        }
        if (user.getDeletedAt() != null) {
            long days = ChronoUnit.DAYS.between(user.getDeletedAt(), LocalDateTime.now());
            if (days <= 30) {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("error", "Account deleted. You can recover it within 30 days.");
                resp.put("recoverable", true);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Account permanently deleted."));
        }
        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Account deactivated. Contact support."));
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());
        String refresh = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", token);
        response.put("refresh_token", refresh);
        response.put("user", user.toMap());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        }

        Map<String, Object> userData = user.toMap();
        if (user.getDonor() != null) {
            userData.put("donor", user.getDonor().toMap());
        }
        if (user.getPatient() != null) {
            userData.put("patient", user.getPatient().toMap());
        }

        return ResponseEntity.ok(Map.of("user", userData));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh() {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));

        // Check that this is a refresh token
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String tokenType = (String) auth.getDetails();
        if (!"refresh".equals(tokenType)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token required."));
        }

        String token = jwtUtil.generateToken(userId);
        return ResponseEntity.ok(Map.of("access_token", token));
    }

    @PostMapping("/select-role")
    public ResponseEntity<Map<String, Object>> selectRole(@RequestBody Map<String, String> data) {
        String role = data.get("role");
        if (role == null || (!role.equals("donor") && !role.equals("patient"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Role must be donor or patient."));
        }

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        }
        if ("admin".equals(user.getRole()) || "sub_admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admins cannot change role."));
        }

        user.setRole(role);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("user", user.toMap()));
    }

    @PostMapping("/delete-account")
    public ResponseEntity<Map<String, Object>> deleteAccount() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        }

        user.setDeletedAt(LocalDateTime.now());
        user.setActive(false);
        if (user.getDonor() != null) {
            user.getDonor().setAvailable(false);
        }
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Account deleted. Recover within 30 days by logging in."));
    }

    @PostMapping("/recover-account")
    public ResponseEntity<Map<String, Object>> recoverAccount(@RequestBody Map<String, String> data) {
        String email = data.getOrDefault("email", "").toLowerCase().trim();
        String password = data.getOrDefault("password", "");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials."));
        }
        if (user.getDeletedAt() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Account is not deleted."));
        }
        if (ChronoUnit.DAYS.between(user.getDeletedAt(), LocalDateTime.now()) > 30) {
            return ResponseEntity.status(HttpStatus.GONE).body(Map.of("error", "Recovery period expired."));
        }

        user.setDeletedAt(null);
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Account recovered successfully."));
    }

    // ==================== OTP Password Reset ====================

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> data) {
        String email = data.getOrDefault("email", "").toLowerCase().trim();
        if (email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        // Always return success to prevent email enumeration
        if (user == null || !user.isActive() || user.getDeletedAt() != null) {
            return ResponseEntity.ok(Map.of("message", "If the email is registered, an OTP has been sent."));
        }

        String otp = generateOtp();
        OtpResetToken token = new OtpResetToken(email, otp, OTP_EXPIRY_MINUTES);
        otpRepository.save(token);

        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send OTP email. Please try again."));
        }

        return ResponseEntity.ok(Map.of("message", "If the email is registered, an OTP has been sent."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> data) {
        String email = data.getOrDefault("email", "").toLowerCase().trim();
        String otp = data.getOrDefault("otp", "").trim();

        if (email.isEmpty() || otp.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and OTP are required."));
        }

        OtpResetToken token = otpRepository.findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(email)
                .orElse(null);

        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No OTP found. Request a new one."));
        }
        if (token.isExpired()) {
            return ResponseEntity.badRequest().body(Map.of("error", "OTP has expired. Request a new one."));
        }
        if (token.getAttempts() >= MAX_OTP_ATTEMPTS) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many attempts. Request a new OTP."));
        }

        token.setAttempts(token.getAttempts() + 1);
        otpRepository.save(token);

        if (!token.getOtp().equals(otp)) {
            int remaining = MAX_OTP_ATTEMPTS - token.getAttempts();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid OTP. " + remaining + " attempts remaining."));
        }

        return ResponseEntity.ok(Map.of("message", "OTP verified successfully.", "verified", true));
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> data) {
        String email = data.getOrDefault("email", "").toLowerCase().trim();
        String otp = data.getOrDefault("otp", "").trim();
        String newPassword = data.getOrDefault("new_password", "");
        String confirmPassword = data.getOrDefault("confirm_password", "");

        if (email.isEmpty() || otp.isEmpty() || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required."));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters."));
        }
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match."));
        }

        OtpResetToken token = otpRepository.findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(email)
                .orElse(null);

        if (token == null || token.isExpired() || !token.getOtp().equals(otp)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP. Request a new one."));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark OTP as used
        token.setUsed(true);
        otpRepository.save(token);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now login."));
    }
}

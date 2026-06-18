package com.bloodcircle.controller;

import com.bloodcircle.model.*;
import com.bloodcircle.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final PatientRepository patientRepository;
    private final FeedbackRepository feedbackRepository;

    public AdminController(UserRepository userRepository, DonorRepository donorRepository,
                            PatientRepository patientRepository, FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.donorRepository = donorRepository;
        this.patientRepository = patientRepository;
        this.feedbackRepository = feedbackRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }

    private ResponseEntity<Map<String, Object>> checkAdmin() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required."));
        }
        return null; // Admin OK
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long recentUsers = userRepository.countByCreatedAtGreaterThanEqual(weekAgo);

        long totalUsers = userRepository.count();
        long totalDonors = donorRepository.count();
        long totalPatients = patientRepository.count();
        long activeDonors = donorRepository.countByIsAvailableTrue();
        long activePatients = patientRepository.countByIsFulfilledFalse();
        long blockedUsers = userRepository.countByIsBlockedTrue();
        long totalFeedback = feedbackRepository.count();
        long unreadFeedback = feedbackRepository.countByIsResolvedFalse();

        List<String> bloodGroups = List.of("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-");

        // Blood group distributions
        Map<String, Long> donorDist = new LinkedHashMap<>();
        donorRepository.countByBloodGroup().forEach(row -> donorDist.put((String) row[0], (Long) row[1]));

        Map<String, Long> patientDist = new LinkedHashMap<>();
        patientRepository.countByBloodGroupRequired().forEach(row -> patientDist.put((String) row[0], (Long) row[1]));

        List<Long> donorCounts = bloodGroups.stream().map(bg -> donorDist.getOrDefault(bg, 0L)).collect(Collectors.toList());
        List<Long> patientCounts = bloodGroups.stream().map(bg -> patientDist.getOrDefault(bg, 0L)).collect(Collectors.toList());

        // Urgent patients
        List<Patient> urgent = patientRepository.findTop5ByUrgencyLevelAndIsFulfilledFalseOrderByCreatedAtDesc("Critical");

        // Recent feedback
        List<Feedback> recentFb = feedbackRepository.findTop5ByOrderByCreatedAtDesc();

        Map<String, Long> bloodGroupStats = new LinkedHashMap<>();
        for (String bg : bloodGroups) {
            bloodGroupStats.put(bg, donorDist.getOrDefault(bg, 0L));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("total_users", totalUsers);
        response.put("recent_users", recentUsers);
        response.put("total_donors", totalDonors);
        response.put("total_patients", totalPatients);
        response.put("active_donors", activeDonors);
        response.put("active_patients", activePatients);
        response.put("blocked_users", blockedUsers);
        response.put("total_feedback", totalFeedback);
        response.put("unread_feedback", unreadFeedback);
        response.put("blood_groups", bloodGroups);
        response.put("blood_group_stats", bloodGroupStats);
        response.put("donor_counts", donorCounts);
        response.put("patient_counts", patientCounts);
        response.put("urgent_patients", urgent.stream().map(Patient::toMap).collect(Collectors.toList()));
        response.put("recent_feedback", recentFb.stream().map(Feedback::toMap).collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }

    // ==================== Manage Users ====================

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> manageUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int per_page,
            @RequestParam(defaultValue = "all") String role,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "") String search) {

        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        PageRequest pageable = PageRequest.of(page - 1, per_page, Sort.by("createdAt").descending());
        Page<User> pagination;

        if (!role.equals("all") && !search.isEmpty()) {
            pagination = userRepository.findByDeletedAtIsNullAndRoleAndEmailContainingIgnoreCase(role, search, pageable);
        } else if (!role.equals("all")) {
            pagination = userRepository.findByDeletedAtIsNullAndRole(role, pageable);
        } else if (!search.isEmpty()) {
            pagination = userRepository.findByDeletedAtIsNullAndEmailContainingIgnoreCase(search, pageable);
        } else {
            pagination = userRepository.findByDeletedAtIsNull(pageable);
        }

        List<Map<String, Object>> usersData = pagination.getContent().stream().map(u -> {
            Map<String, Object> d = new LinkedHashMap<>(u.toMap());
            if (u.getDonor() != null) {
                d.put("donor_name", u.getDonor().getFullName());
                d.put("donor_blood_group", u.getDonor().getBloodGroup());
            }
            if (u.getPatient() != null) {
                d.put("patient_name", u.getPatient().getFullName());
                d.put("patient_blood_group", u.getPatient().getBloodGroupRequired());
            }
            return d;
        }).collect(Collectors.toList());

        Map<String, Object> paginationInfo = new LinkedHashMap<>();
        paginationInfo.put("total", pagination.getTotalElements());
        paginationInfo.put("pages", pagination.getTotalPages());
        paginationInfo.put("page", page);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("users", usersData);
        response.put("pagination", paginationInfo);
        return ResponseEntity.ok(response);
    }

    // ==================== Manage Donors ====================

    @GetMapping("/donors")
    public ResponseEntity<Map<String, Object>> manageDonors(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int per_page,
            @RequestParam(defaultValue = "all") String blood_group,
            @RequestParam(defaultValue = "all") String availability,
            @RequestParam(defaultValue = "") String search) {

        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        PageRequest pageable = PageRequest.of(page - 1, per_page, Sort.by("createdAt").descending());
        Page<Donor> pagination;

        if (!blood_group.equals("all") && !search.isEmpty()) {
            pagination = donorRepository.findByBloodGroupAndFullNameContainingIgnoreCase(blood_group, search, pageable);
        } else if (!blood_group.equals("all")) {
            pagination = donorRepository.findByBloodGroup(blood_group, pageable);
        } else if (!search.isEmpty()) {
            pagination = donorRepository.findByFullNameContainingIgnoreCase(search, pageable);
        } else if (availability.equals("available")) {
            pagination = donorRepository.findByIsAvailableTrue(pageable);
        } else if (availability.equals("unavailable")) {
            pagination = donorRepository.findByIsAvailableFalse(pageable);
        } else {
            pagination = donorRepository.findAll(pageable);
        }

        Map<String, Object> paginationInfo = new LinkedHashMap<>();
        paginationInfo.put("total", pagination.getTotalElements());
        paginationInfo.put("pages", pagination.getTotalPages());
        paginationInfo.put("page", page);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("donors", pagination.getContent().stream().map(Donor::toMap).collect(Collectors.toList()));
        response.put("pagination", paginationInfo);
        return ResponseEntity.ok(response);
    }

    // ==================== Manage Patients ====================

    @GetMapping("/patients")
    public ResponseEntity<Map<String, Object>> managePatients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int per_page,
            @RequestParam(defaultValue = "all") String blood_group,
            @RequestParam(defaultValue = "all") String urgency,
            @RequestParam(defaultValue = "all") String fulfillment,
            @RequestParam(defaultValue = "") String search) {

        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        PageRequest pageable = PageRequest.of(page - 1, per_page, Sort.by("createdAt").descending());
        Page<Patient> pagination;

        if (!blood_group.equals("all") && !search.isEmpty()) {
            pagination = patientRepository.findByBloodGroupRequiredAndFullNameContainingIgnoreCase(blood_group, search, pageable);
        } else if (!blood_group.equals("all")) {
            pagination = patientRepository.findByBloodGroupRequired(blood_group, pageable);
        } else if (!urgency.equals("all")) {
            pagination = patientRepository.findByUrgencyLevel(urgency, pageable);
        } else if (fulfillment.equals("fulfilled")) {
            pagination = patientRepository.findByIsFulfilledTrue(pageable);
        } else if (fulfillment.equals("pending")) {
            pagination = patientRepository.findByIsFulfilledFalse(pageable);
        } else if (!search.isEmpty()) {
            pagination = patientRepository.findByFullNameContainingIgnoreCase(search, pageable);
        } else {
            pagination = patientRepository.findAll(pageable);
        }

        Map<String, Object> paginationInfo = new LinkedHashMap<>();
        paginationInfo.put("total", pagination.getTotalElements());
        paginationInfo.put("pages", pagination.getTotalPages());
        paginationInfo.put("page", page);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("patients", pagination.getContent().stream().map(Patient::toMap).collect(Collectors.toList()));
        response.put("pagination", paginationInfo);
        return ResponseEntity.ok(response);
    }

    // ==================== Manage Feedback ====================

    @GetMapping("/feedback")
    public ResponseEntity<Map<String, Object>> manageFeedback(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int per_page,
            @RequestParam(defaultValue = "all") String status) {

        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        PageRequest pageable = PageRequest.of(page - 1, per_page, Sort.by("createdAt").descending());
        Page<Feedback> pagination;

        if (status.equals("pending")) {
            pagination = feedbackRepository.findByIsResolvedFalse(pageable);
        } else if (status.equals("resolved")) {
            pagination = feedbackRepository.findByIsResolvedTrue(pageable);
        } else {
            pagination = feedbackRepository.findAll(pageable);
        }

        Map<String, Object> paginationInfo = new LinkedHashMap<>();
        paginationInfo.put("total", pagination.getTotalElements());
        paginationInfo.put("pages", pagination.getTotalPages());
        paginationInfo.put("page", page);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("feedbacks", pagination.getContent().stream().map(Feedback::toMap).collect(Collectors.toList()));
        response.put("pagination", paginationInfo);
        return ResponseEntity.ok(response);
    }

    // ==================== User Actions ====================

    @PostMapping("/user/{userId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleUser(@PathVariable Long userId) {
        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if ("admin".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cannot modify admin accounts."));

        user.setActive(!user.isActive());
        userRepository.save(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user", user.toMap());
        response.put("message", "User " + (user.isActive() ? "activated" : "deactivated") + ".");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/block")
    public ResponseEntity<Map<String, Object>> blockUser(@PathVariable Long userId) {
        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if ("admin".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cannot block admin."));

        user.setBlocked(true);
        user.setActive(false);
        userRepository.save(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user", user.toMap());
        response.put("message", "User blocked.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/unblock")
    public ResponseEntity<Map<String, Object>> unblockUser(@PathVariable Long userId) {
        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));

        user.setBlocked(false);
        user.setActive(true);
        userRepository.save(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user", user.toMap());
        response.put("message", "User unblocked.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        Long adminId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if ("admin".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cannot delete admin."));
        if (user.getId().equals(adminId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cannot delete yourself."));

        user.setDeletedAt(LocalDateTime.now());
        user.setActive(false);
        if (user.getDonor() != null) {
            user.getDonor().setAvailable(false);
            donorRepository.save(user.getDonor());
        }
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User " + user.getEmail() + " deleted."));
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> editUser(@PathVariable Long userId, @RequestBody Map<String, Object> data) {
        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));

        if (data.containsKey("email")) user.setEmail(data.get("email").toString());
        if (data.containsKey("phone")) user.setPhone(data.get("phone") != null ? data.get("phone").toString() : null);
        if (data.containsKey("role")) user.setRole(data.get("role") != null ? data.get("role").toString() : null);
        if (data.containsKey("is_active")) user.setActive(Boolean.parseBoolean(data.get("is_active").toString()));
        if (data.containsKey("is_verified")) user.setVerified(Boolean.parseBoolean(data.get("is_verified").toString()));
        if (data.containsKey("is_blocked")) user.setBlocked(Boolean.parseBoolean(data.get("is_blocked").toString()));

        userRepository.save(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user", user.toMap());
        response.put("message", "User updated.");
        return ResponseEntity.ok(response);
    }

    // ==================== Feedback Actions ====================

    @PostMapping("/feedback/{fid}/respond")
    public ResponseEntity<Map<String, Object>> respondFeedback(@PathVariable Long fid, @RequestBody Map<String, String> data) {
        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        Feedback fb = feedbackRepository.findById(fid).orElse(null);
        if (fb == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Feedback not found."));

        String responseText = data.getOrDefault("response", data.getOrDefault("admin_response", ""));
        if (responseText.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Response is required."));

        fb.setAdminResponse(responseText);
        fb.setResolved(true);
        fb.setResolvedAt(LocalDateTime.now());
        feedbackRepository.save(fb);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("feedback", fb.toMap());
        response.put("message", "Response sent.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback/{fid}/toggle")
    public ResponseEntity<Map<String, Object>> toggleFeedback(@PathVariable Long fid) {
        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        Feedback fb = feedbackRepository.findById(fid).orElse(null);
        if (fb == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Feedback not found."));

        fb.setResolved(!fb.isResolved());
        fb.setResolvedAt(fb.isResolved() ? LocalDateTime.now() : null);
        feedbackRepository.save(fb);

        return ResponseEntity.ok(Map.of("feedback", fb.toMap()));
    }

    // ==================== Patient Actions ====================

    @PostMapping("/patient/{pid}/fulfill")
    public ResponseEntity<Map<String, Object>> fulfillPatient(@PathVariable Long pid) {
        ResponseEntity<Map<String, Object>> adminCheck = checkAdmin();
        if (adminCheck != null) return adminCheck;

        Patient patient = patientRepository.findById(pid).orElse(null);
        if (patient == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Patient not found."));

        patient.setFulfilled(true);
        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("patient", patient.toMap());
        response.put("message", "Request fulfilled.");
        return ResponseEntity.ok(response);
    }
}

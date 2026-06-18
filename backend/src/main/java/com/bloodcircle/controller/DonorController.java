package com.bloodcircle.controller;

import com.bloodcircle.model.Donor;
import com.bloodcircle.model.User;
import com.bloodcircle.repository.DonorRepository;
import com.bloodcircle.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/donor")
public class DonorController {

    private final UserRepository userRepository;
    private final DonorRepository donorRepository;

    public DonorController(UserRepository userRepository, DonorRepository donorRepository) {
        this.userRepository = userRepository;
        this.donorRepository = donorRepository;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> data) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        }

        String[] required = {"full_name", "phone", "blood_group", "date_of_birth", "gender", "city", "state", "pincode"};
        for (String f : required) {
            Object val = data.get(f);
            if (val == null || val.toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", f + " is required."));
            }
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(data.get("date_of_birth").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date_of_birth format. Use YYYY-MM-DD."));
        }

        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < 18) return ResponseEntity.badRequest().body(Map.of("error", "Must be at least 18 years old."));
        if (age > 65) return ResponseEntity.badRequest().body(Map.of("error", "Must be under 65 years old."));

        LocalDate lastDonation = null;
        Object lastDonStr = data.get("last_donation_date");
        if (lastDonStr != null && !lastDonStr.toString().isEmpty()) {
            try {
                lastDonation = LocalDate.parse(lastDonStr.toString());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid last_donation_date format."));
            }
        }

        user.setRole("donor");

        Donor donor = donorRepository.findByUserId(userId).orElse(null);
        if (donor != null) {
            // Update existing
            donor.setFullName(data.get("full_name").toString());
            donor.setPhone(data.get("phone").toString());
            donor.setBloodGroup(data.get("blood_group").toString());
            donor.setDateOfBirth(dob);
            donor.setGender(data.get("gender").toString());
            donor.setCity(data.get("city").toString());
            donor.setState(data.get("state").toString());
            donor.setPincode(data.get("pincode").toString());
            donor.setLastDonationDate(lastDonation);
            donor.setMedicalHistory(data.getOrDefault("medical_history", "").toString());
            donor.setAvailable(data.get("is_available") == null || Boolean.parseBoolean(data.get("is_available").toString()));
            donor.setUpdatedAt(LocalDateTime.now());
        } else {
            donor = new Donor();
            donor.setUser(user);
            donor.setFullName(data.get("full_name").toString());
            donor.setPhone(data.get("phone").toString());
            donor.setBloodGroup(data.get("blood_group").toString());
            donor.setDateOfBirth(dob);
            donor.setGender(data.get("gender").toString());
            donor.setCity(data.get("city").toString());
            donor.setState(data.get("state").toString());
            donor.setPincode(data.get("pincode").toString());
            donor.setLastDonationDate(lastDonation);
            donor.setMedicalHistory(data.getOrDefault("medical_history", "").toString());
            donor.setAvailable(data.get("is_available") == null || Boolean.parseBoolean(data.get("is_available").toString()));
        }

        userRepository.save(user);
        donorRepository.save(donor);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("donor", donor.toMap());
        response.put("user", user.toMap());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if (!"donor".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Donors only."));

        Donor donor = donorRepository.findByUserId(userId).orElse(null);
        if (donor == null) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("error", "Complete your donor profile first.");
            resp.put("needs_profile", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        boolean canDonate = donor.canDonate();
        Long daysSince = null;
        if (donor.getLastDonationDate() != null) {
            daysSince = ChronoUnit.DAYS.between(donor.getLastDonationDate(), LocalDate.now());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("donor", donor.toMap());
        response.put("can_donate", canDonate);
        response.put("days_since_donation", daysSince);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if (!"donor".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Donors only."));

        Donor donor = donorRepository.findByUserId(userId).orElse(null);
        if (donor == null) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("error", "Profile not found.");
            resp.put("needs_profile", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        return ResponseEntity.ok(Map.of("donor", donor.toMap()));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> data) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if (!"donor".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Donors only."));

        Donor donor = donorRepository.findByUserId(userId).orElse(null);
        if (donor == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Profile not found."));

        if (data.containsKey("full_name") && data.get("full_name") != null && !data.get("full_name").toString().isEmpty())
            donor.setFullName(data.get("full_name").toString());
        if (data.containsKey("phone") && data.get("phone") != null && !data.get("phone").toString().isEmpty())
            donor.setPhone(data.get("phone").toString());
        if (data.containsKey("blood_group") && data.get("blood_group") != null && !data.get("blood_group").toString().isEmpty())
            donor.setBloodGroup(data.get("blood_group").toString());
        if (data.containsKey("date_of_birth") && data.get("date_of_birth") != null && !data.get("date_of_birth").toString().isEmpty()) {
            try {
                donor.setDateOfBirth(LocalDate.parse(data.get("date_of_birth").toString()));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format."));
            }
        }
        if (data.containsKey("gender") && data.get("gender") != null && !data.get("gender").toString().isEmpty())
            donor.setGender(data.get("gender").toString());
        if (data.containsKey("city") && data.get("city") != null && !data.get("city").toString().isEmpty())
            donor.setCity(data.get("city").toString());
        if (data.containsKey("state") && data.get("state") != null && !data.get("state").toString().isEmpty())
            donor.setState(data.get("state").toString());
        if (data.containsKey("pincode") && data.get("pincode") != null && !data.get("pincode").toString().isEmpty())
            donor.setPincode(data.get("pincode").toString());
        if (data.containsKey("last_donation_date")) {
            Object val = data.get("last_donation_date");
            if (val != null && !val.toString().isEmpty()) {
                try {
                    donor.setLastDonationDate(LocalDate.parse(val.toString()));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format."));
                }
            } else {
                donor.setLastDonationDate(null);
            }
        }
        if (data.containsKey("medical_history"))
            donor.setMedicalHistory(data.get("medical_history") != null ? data.get("medical_history").toString() : "");
        if (data.containsKey("is_available"))
            donor.setAvailable(Boolean.parseBoolean(data.get("is_available").toString()));

        donor.setUpdatedAt(LocalDateTime.now());
        donorRepository.save(donor);
        return ResponseEntity.ok(Map.of("donor", donor.toMap()));
    }

    @PostMapping("/toggle-availability")
    public ResponseEntity<Map<String, Object>> toggleAvailability() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if (!"donor".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Donors only."));

        Donor donor = donorRepository.findByUserId(userId).orElse(null);
        if (donor == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Profile not found."));

        donor.setAvailable(!donor.isAvailable());
        donor.setUpdatedAt(LocalDateTime.now());
        donorRepository.save(donor);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("donor", donor.toMap());
        response.put("message", "Status: " + (donor.isAvailable() ? "available" : "unavailable"));
        return ResponseEntity.ok(response);
    }
}

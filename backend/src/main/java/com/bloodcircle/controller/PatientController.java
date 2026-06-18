package com.bloodcircle.controller;

import com.bloodcircle.model.Donor;
import com.bloodcircle.model.Patient;
import com.bloodcircle.model.User;
import com.bloodcircle.repository.DonorRepository;
import com.bloodcircle.repository.PatientRepository;
import com.bloodcircle.repository.UserRepository;
import com.bloodcircle.util.BloodCompatibilityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DonorRepository donorRepository;

    public PatientController(UserRepository userRepository, PatientRepository patientRepository,
                              DonorRepository donorRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
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
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));

        String[] required = {"full_name", "phone", "blood_group_required", "hospital_name",
                "city", "state", "pincode", "urgency_level", "required_by_date"};
        for (String f : required) {
            Object val = data.get(f);
            if (val == null || val.toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", f + " is required."));
            }
        }

        LocalDate reqDate;
        try {
            reqDate = LocalDate.parse(data.get("required_by_date").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid required_by_date format. Use YYYY-MM-DD."));
        }

        user.setRole("patient");

        Patient patient = patientRepository.findByUserId(userId).orElse(null);
        if (patient != null) {
            patient.setFullName(data.get("full_name").toString());
            patient.setPhone(data.get("phone").toString());
            patient.setBloodGroupRequired(data.get("blood_group_required").toString());
            patient.setHospitalName(data.get("hospital_name").toString());
            patient.setCity(data.get("city").toString());
            patient.setState(data.get("state").toString());
            patient.setPincode(data.get("pincode").toString());
            patient.setUrgencyLevel(data.get("urgency_level").toString());
            patient.setRequiredByDate(reqDate);
            patient.setMedicalCondition(data.getOrDefault("medical_condition", "").toString());
            patient.setUpdatedAt(LocalDateTime.now());
        } else {
            patient = new Patient();
            patient.setUser(user);
            patient.setFullName(data.get("full_name").toString());
            patient.setPhone(data.get("phone").toString());
            patient.setBloodGroupRequired(data.get("blood_group_required").toString());
            patient.setHospitalName(data.get("hospital_name").toString());
            patient.setCity(data.get("city").toString());
            patient.setState(data.get("state").toString());
            patient.setPincode(data.get("pincode").toString());
            patient.setUrgencyLevel(data.get("urgency_level").toString());
            patient.setRequiredByDate(reqDate);
            patient.setMedicalCondition(data.getOrDefault("medical_condition", "").toString());
        }

        userRepository.save(user);
        patientRepository.save(patient);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("patient", patient.toMap());
        response.put("user", user.toMap());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if (!"patient".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Patients only."));

        Patient patient = patientRepository.findByUserId(userId).orElse(null);
        if (patient == null) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("error", "Complete your patient profile first.");
            resp.put("needs_profile", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        List<String> compatible = BloodCompatibilityUtil.getCompatibleDonorGroups(patient.getBloodGroupRequired());
        List<Donor> matching = donorRepository.findTop10ByBloodGroupInAndIsAvailableTrueAndCityContainingIgnoreCase(
                compatible, patient.getCity());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("patient", patient.toMap());
        response.put("matching_donors", matching.stream().map(Donor::toMap).collect(Collectors.toList()));
        response.put("compatible_groups", compatible);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if (!"patient".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Patients only."));

        Patient patient = patientRepository.findByUserId(userId).orElse(null);
        if (patient == null) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("error", "Profile not found.");
            resp.put("needs_profile", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        List<String> compatible = BloodCompatibilityUtil.getCompatibleDonorGroups(patient.getBloodGroupRequired());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("patient", patient.toMap());
        response.put("compatible_groups", compatible);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> data) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if (!"patient".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Patients only."));

        Patient patient = patientRepository.findByUserId(userId).orElse(null);
        if (patient == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Profile not found."));

        if (data.containsKey("full_name") && data.get("full_name") != null && !data.get("full_name").toString().isEmpty())
            patient.setFullName(data.get("full_name").toString());
        if (data.containsKey("phone") && data.get("phone") != null && !data.get("phone").toString().isEmpty())
            patient.setPhone(data.get("phone").toString());
        if (data.containsKey("blood_group_required") && data.get("blood_group_required") != null && !data.get("blood_group_required").toString().isEmpty())
            patient.setBloodGroupRequired(data.get("blood_group_required").toString());
        if (data.containsKey("hospital_name") && data.get("hospital_name") != null && !data.get("hospital_name").toString().isEmpty())
            patient.setHospitalName(data.get("hospital_name").toString());
        if (data.containsKey("city") && data.get("city") != null && !data.get("city").toString().isEmpty())
            patient.setCity(data.get("city").toString());
        if (data.containsKey("state") && data.get("state") != null && !data.get("state").toString().isEmpty())
            patient.setState(data.get("state").toString());
        if (data.containsKey("pincode") && data.get("pincode") != null && !data.get("pincode").toString().isEmpty())
            patient.setPincode(data.get("pincode").toString());
        if (data.containsKey("urgency_level") && data.get("urgency_level") != null && !data.get("urgency_level").toString().isEmpty())
            patient.setUrgencyLevel(data.get("urgency_level").toString());
        if (data.containsKey("required_by_date") && data.get("required_by_date") != null && !data.get("required_by_date").toString().isEmpty()) {
            try {
                patient.setRequiredByDate(LocalDate.parse(data.get("required_by_date").toString()));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format."));
            }
        }
        if (data.containsKey("medical_condition"))
            patient.setMedicalCondition(data.get("medical_condition") != null ? data.get("medical_condition").toString() : "");
        if (data.containsKey("is_fulfilled"))
            patient.setFulfilled(Boolean.parseBoolean(data.get("is_fulfilled").toString()));

        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);
        return ResponseEntity.ok(Map.of("patient", patient.toMap()));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchDonors(
            @RequestParam(defaultValue = "") String blood_group,
            @RequestParam(defaultValue = "") String city,
            @RequestParam(defaultValue = "") String state,
            @RequestParam(defaultValue = "true") boolean available_only,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int per_page) {

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if (!"patient".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Patients only."));

        Patient patient = patientRepository.findByUserId(userId).orElse(null);
        if (patient == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Complete profile first."));

        List<String> compatible = BloodCompatibilityUtil.getCompatibleDonorGroups(patient.getBloodGroupRequired());
        PageRequest pageable = PageRequest.of(page - 1, per_page, Sort.by("createdAt").descending());

        Page<Donor> results;
        String bg = blood_group.isEmpty() ? null : blood_group;
        String ct = city.isEmpty() ? null : city;
        String st = state.isEmpty() ? null : state;

        if (bg != null) {
            results = donorRepository.searchDonors(bg, ct, st, available_only, pageable);
        } else {
            results = donorRepository.searchDonorsCompatible(compatible, ct, st, available_only, pageable);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("donors", results.getContent().stream().map(Donor::toMap).collect(Collectors.toList()));
        response.put("total", results.getTotalElements());
        response.put("pages", results.getTotalPages());
        response.put("page", page);
        response.put("compatible_groups", compatible);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/donor/{donorId}")
    public ResponseEntity<Map<String, Object>> viewDonor(@PathVariable Long donorId) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        if (!"patient".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Patients only."));

        Patient patient = patientRepository.findByUserId(userId).orElse(null);
        if (patient == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Complete profile first."));

        Donor donor = donorRepository.findById(donorId).orElse(null);
        if (donor == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Donor not found."));

        List<String> compatible = BloodCompatibilityUtil.getCompatibleDonorGroups(patient.getBloodGroupRequired());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("donor", donor.toMap());
        response.put("is_compatible", compatible.contains(donor.getBloodGroup()));
        return ResponseEntity.ok(response);
    }
}

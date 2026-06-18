package com.bloodcircle.controller;

import com.bloodcircle.model.Donor;
import com.bloodcircle.model.Feedback;
import com.bloodcircle.model.Patient;
import com.bloodcircle.repository.DonorRepository;
import com.bloodcircle.repository.FeedbackRepository;
import com.bloodcircle.repository.PatientRepository;
import com.bloodcircle.util.BloodCompatibilityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MainController {

    private final DonorRepository donorRepository;
    private final PatientRepository patientRepository;
    private final FeedbackRepository feedbackRepository;

    public MainController(DonorRepository donorRepository, PatientRepository patientRepository,
                           FeedbackRepository feedbackRepository) {
        this.donorRepository = donorRepository;
        this.patientRepository = patientRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        long totalDonors = donorRepository.count();
        long availableDonors = donorRepository.countByIsAvailableTrue();
        long totalPatients = patientRepository.count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("total_donors", totalDonors);
        response.put("available_donors", availableDonors);
        response.put("total_patients", totalPatients);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> submitFeedback(@RequestBody Map<String, Object> data) {
        String[] required = {"name", "email", "subject", "message"};
        for (String field : required) {
            Object val = data.get(field);
            if (val == null || val.toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", field + " is required."));
            }
        }

        Feedback fb = new Feedback();
        fb.setName(data.get("name").toString());
        fb.setEmail(data.get("email").toString());
        fb.setSubject(data.get("subject").toString());
        fb.setMessage(data.get("message").toString());

        Object rating = data.get("rating");
        if (rating != null && !rating.toString().isEmpty()) {
            try {
                fb.setRating(Integer.parseInt(rating.toString()));
            } catch (NumberFormatException ignored) {}
        }

        feedbackRepository.save(fb);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Feedback submitted successfully!"));
    }

    @GetMapping("/blood-compatibility")
    public ResponseEntity<Map<String, Object>> bloodCompatibility() {
        Map<String, Object> compat = new LinkedHashMap<>();

        for (String bg : BloodCompatibilityUtil.BLOOD_COMPATIBILITY.keySet()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("can_donate_to", BloodCompatibilityUtil.BLOOD_COMPATIBILITY.get(bg));
            entry.put("can_receive_from", BloodCompatibilityUtil.RECEIVE_FROM.getOrDefault(bg, java.util.List.of()));
            compat.put(bg, entry);
        }

        return ResponseEntity.ok(Map.of("compatibility", compat));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}

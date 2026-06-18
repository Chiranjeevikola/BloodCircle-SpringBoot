package com.bloodcircle.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, insertable = false, updatable = false)
    private Long userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "blood_group_required", nullable = false, length = 5)
    private String bloodGroupRequired;

    @Column(name = "hospital_name", nullable = false, length = 100)
    private String hospitalName;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(nullable = false, length = 10)
    private String pincode;

    @Column(name = "urgency_level", nullable = false, length = 20)
    private String urgencyLevel;

    @Column(name = "required_by_date", nullable = false)
    private LocalDate requiredByDate;

    @Column(name = "medical_condition", columnDefinition = "TEXT")
    private String medicalCondition;

    @Column(name = "is_fulfilled")
    private boolean isFulfilled = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Patient() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public long daysRemaining() {
        if (requiredByDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), requiredByDate);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBloodGroupRequired() { return bloodGroupRequired; }
    public void setBloodGroupRequired(String bloodGroupRequired) { this.bloodGroupRequired = bloodGroupRequired; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public LocalDate getRequiredByDate() { return requiredByDate; }
    public void setRequiredByDate(LocalDate requiredByDate) { this.requiredByDate = requiredByDate; }
    public String getMedicalCondition() { return medicalCondition; }
    public void setMedicalCondition(String medicalCondition) { this.medicalCondition = medicalCondition; }
    public boolean isFulfilled() { return isFulfilled; }
    public void setFulfilled(boolean fulfilled) { isFulfilled = fulfilled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("user_id", userId);
        map.put("full_name", fullName);
        map.put("phone", phone);
        map.put("blood_group_required", bloodGroupRequired);
        map.put("blood_group", bloodGroupRequired);
        map.put("hospital_name", hospitalName);
        map.put("city", city);
        map.put("state", state);
        map.put("pincode", pincode);
        map.put("urgency_level", urgencyLevel);
        map.put("required_by_date", requiredByDate != null ? requiredByDate.toString() : null);
        map.put("medical_condition", medicalCondition);
        map.put("is_fulfilled", isFulfilled);
        map.put("days_remaining", daysRemaining());
        map.put("created_at", createdAt != null ? createdAt.toString() : null);
        map.put("email", user != null ? user.getEmail() : null);
        return map;
    }
}

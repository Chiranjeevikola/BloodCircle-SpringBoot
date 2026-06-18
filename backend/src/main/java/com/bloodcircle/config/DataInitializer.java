package com.bloodcircle.config;

import com.bloodcircle.model.User;
import com.bloodcircle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create default admin user if not exists
        userRepository.findByEmail(adminEmail).ifPresentOrElse(
                existing -> {
                    if (!"admin".equals(existing.getRole())) {
                        existing.setRole("admin");
                    }
                    existing.setPasswordHash(passwordEncoder.encode(adminPassword));
                    userRepository.save(existing);
                    System.out.println("Admin password updated to match current config: " + adminEmail);
                },
                () -> {
                    User admin = new User();
                    admin.setEmail(adminEmail);
                    admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                    admin.setRole("admin");
                    admin.setVerified(true);
                    admin.setActive(true);
                    admin.setBlocked(false);
                    userRepository.save(admin);
                    System.out.println("Admin created: " + adminEmail);
                }
        );

        System.out.printf("Users: %d, Donors: %d, Patients: %d%n",
                userRepository.count(),
                userRepository.count(), // Will be replaced with donor count
                userRepository.count()  // Will be replaced with patient count
        );
    }
}

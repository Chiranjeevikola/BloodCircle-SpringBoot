package com.bloodcircle.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@bloodcircle.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("BloodCircle — Password Reset OTP");
        message.setText(
                "Hello,\n\n" +
                "Your OTP for password reset is: " + otp + "\n\n" +
                "This code is valid for 10 minutes.\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "— BloodCircle Team"
        );
        mailSender.send(message);
    }
}

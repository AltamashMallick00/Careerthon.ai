package com.careerthon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_VALIDITY_MINUTES = 5;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private static class OtpEntry {
        final String otp;
        final LocalDateTime expiry;

        OtpEntry(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }

    public String generateAndSendOtp(String email) {
        String cleanEmail = email.trim().toLowerCase();
        
        // Generate 6-digit OTP
        int code = 100000 + random.nextInt(900000);
        String otp = String.valueOf(code);
        
        otpStorage.put(cleanEmail, new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES)));
        
        log.info("🔑 Generated OTP for {}: {} (valid for {} minutes)", cleanEmail, otp, OTP_VALIDITY_MINUTES);

        // Attempt sending via Spring Mail Sender
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(cleanEmail);
                message.setSubject("Careerthon.AI - Your One-Time Login Code: " + otp);
                message.setText("Hello,\n\nYour Careerthon.AI verification code is: " + otp + 
                        "\n\nThis code will expire in 5 minutes. If you did not request this code, please ignore this email.\n\nBest regards,\nCareerthon.AI Team");
                mailSender.send(message);
                log.info("✉️ Successfully sent OTP email to {}", cleanEmail);
            } else {
                log.info("ℹ️ JavaMailSender not configured; simulated email OTP dispatch for: {}", cleanEmail);
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not send real email OTP (simulated mode active): {}", e.getMessage());
        }

        return otp;
    }

    public boolean verifyOtp(String email, String enteredOtp) {
        if (email == null || enteredOtp == null) return false;
        String cleanEmail = email.trim().toLowerCase();
        String cleanOtp = enteredOtp.trim();

        OtpEntry entry = otpStorage.get(cleanEmail);
        if (entry == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(entry.expiry)) {
            otpStorage.remove(cleanEmail);
            return false;
        }

        boolean matches = entry.otp.equals(cleanOtp);
        if (matches) {
            otpStorage.remove(cleanEmail);
        }
        return matches;
    }
}

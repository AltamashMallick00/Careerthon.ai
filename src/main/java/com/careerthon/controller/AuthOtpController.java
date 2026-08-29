package com.careerthon.controller;

import com.careerthon.model.User;
import com.careerthon.repository.UserRepository;
import com.careerthon.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/api/auth")
@SuppressWarnings("null")
public class AuthOtpController {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthOtpController(OtpService otpService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/send-otp")
    @ResponseBody
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email address is required."));
        }

        String cleanEmail = email.trim().toLowerCase();
        String generatedOtp = otpService.generateAndSendOtp(cleanEmail);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "A 6-digit OTP code has been dispatched to " + cleanEmail,
                "devOtp", generatedOtp // In dev mode, allows instant copy-paste
        ));
    }

    @PostMapping("/verify-otp")
    public String verifyOtpAndLogin(
            @RequestParam String email,
            @RequestParam String otp,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        String cleanOtp = otp != null ? otp.trim() : "";

        if (cleanEmail.isEmpty() || cleanOtp.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please provide both email and OTP.");
            return "redirect:/login";
        }

        boolean isValid = otpService.verifyOtp(cleanEmail, cleanOtp);
        if (!isValid) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP code. Please try again.");
            return "redirect:/login";
        }

        // Find or auto-provision user
        User user = userRepository.findByEmail(cleanEmail)
                .or(() -> userRepository.findByUsername(cleanEmail))
                .orElseGet(() -> {
                    String baseUsername = cleanEmail.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
                    String uniqueUsername = baseUsername;
                    int suffix = 1;
                    while (userRepository.findByUsername(uniqueUsername).isPresent()) {
                        uniqueUsername = baseUsername + suffix++;
                    }
                    User newUser = new User();
                    newUser.setEmail(cleanEmail);
                    newUser.setUsername(uniqueUsername);
                    newUser.setFullName(baseUsername);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setRoles("ROLE_STUDENT,ROLE_USER");
                    newUser.setEmailVerified(true);
                    newUser.setEnabled(true);
                    return userRepository.save(newUser);
                });

        // Establish Spring Security Context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        return "redirect:/";
    }

    @GetMapping("/google/callback")
    public String handleGoogleSso(
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String name,
            HttpServletRequest request) {

        if (email.trim().isEmpty()) {
            email = "demo.google.user@gmail.com";
        }
        if (name.trim().isEmpty()) {
            name = "Google Verified Candidate";
        }

        final String cleanEmail = email.trim().toLowerCase();
        final String cleanName = name.trim();

        User user = userRepository.findByEmail(cleanEmail)
                .or(() -> userRepository.findByUsername(cleanEmail))
                .orElseGet(() -> {
                    String baseUsername = cleanEmail.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
                    String uniqueUsername = baseUsername;
                    int suffix = 1;
                    while (userRepository.findByUsername(uniqueUsername).isPresent()) {
                        uniqueUsername = baseUsername + suffix++;
                    }
                    User newUser = new User();
                    newUser.setEmail(cleanEmail);
                    newUser.setUsername(uniqueUsername);
                    newUser.setFullName(cleanName);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setRoles("ROLE_STUDENT,ROLE_USER");
                    newUser.setEmailVerified(true);
                    newUser.setAuthProvider("GOOGLE");
                    newUser.setEnabled(true);
                    return userRepository.save(newUser);
                });

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        return "redirect:/";
    }
}

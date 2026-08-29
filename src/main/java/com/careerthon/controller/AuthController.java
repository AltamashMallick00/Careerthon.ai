package com.careerthon.controller;

import com.careerthon.model.User;
import com.careerthon.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Random;

@Controller
@SuppressWarnings("null")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model, HttpSession session) {
        Random rand = new Random();
        int num1 = rand.nextInt(10) + 1;
        int num2 = rand.nextInt(10) + 1;
        session.setAttribute("captchaResult", num1 + num2);
        model.addAttribute("captchaQuestion", num1 + " + " + num2 + " = ?");
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String username,
            @RequestParam String password,
            @RequestParam String captcha,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String trimmedName = fullName != null ? fullName.trim() : "";
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        String cleanPhone = phoneNumber != null ? phoneNumber.replaceAll("[^0-9+]", "").trim() : "";
        String cleanPassword = password != null ? password.trim() : "";
        String cleanUsername = (username != null && !username.trim().isEmpty()) ? username.trim() : cleanEmail;

        if (trimmedName.isEmpty() || cleanEmail.isEmpty() || cleanPhone.isEmpty() || cleanPassword.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Full Name, Email, Phone Number, and Password are all required.");
            return "redirect:/signup";
        }

        if (cleanPhone.length() < 10) {
            redirectAttributes.addFlashAttribute("error", "Please provide a valid 10-digit mobile number.");
            return "redirect:/signup";
        }

        if (cleanPassword.length() < 4) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 4 characters long.");
            return "redirect:/signup";
        }

        Integer expectedCaptcha = (Integer) session.getAttribute("captchaResult");
        if (expectedCaptcha == null || !captcha.trim().equals(String.valueOf(expectedCaptcha))) {
            redirectAttributes.addFlashAttribute("error", "Invalid CAPTCHA answer. Please try again.");
            return "redirect:/signup";
        }

        if (userRepository.findByEmail(cleanEmail).isPresent() || userRepository.findByUsername(cleanUsername).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "An account with this email/username already exists. Please login.");
            return "redirect:/signup";
        }

        if (userRepository.findByPhoneNumber(cleanPhone).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "An account with this mobile number is already registered.");
            return "redirect:/signup";
        }

        User newUser = new User();
        newUser.setFullName(trimmedName);
        newUser.setEmail(cleanEmail);
        newUser.setUsername(cleanUsername);
        newUser.setPhoneNumber(cleanPhone);
        newUser.setPassword(passwordEncoder.encode(cleanPassword));
        newUser.setRoles("ROLE_STUDENT,ROLE_USER");
        newUser.setEnabled(true);
        userRepository.save(newUser);

        redirectAttributes.addFlashAttribute("signupSuccess", true);
        return "redirect:/login?signupSuccess=true";
    }
}


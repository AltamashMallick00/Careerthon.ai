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
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String phoneNumber,
            @RequestParam String captcha,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String trimmedName = fullName != null ? fullName.trim() : "";
        String trimmedUsername = username != null ? username.trim() : "";
        String cleanPassword = password != null ? password.trim() : "";
        String cleanPhone = phoneNumber != null ? phoneNumber.replaceAll("[^0-9+]", "").trim() : "";

        if (trimmedName.isEmpty() || trimmedUsername.isEmpty() || cleanPassword.isEmpty() || cleanPhone.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "All fields (Full Name, Username, Password, Phone Number) are required.");
            return "redirect:/signup";
        }

        if (cleanPhone.length() < 10) {
            redirectAttributes.addFlashAttribute("error", "Please provide a valid 10-digit mobile number for offer letter generation.");
            return "redirect:/signup";
        }

        if (cleanPassword.length() < 4) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 4 characters long.");
            return "redirect:/signup";
        }

        Integer expectedCaptcha = (Integer) session.getAttribute("captchaResult");
        if (expectedCaptcha == null || !captcha.trim().equals(String.valueOf(expectedCaptcha))) {
            redirectAttributes.addFlashAttribute("error", "Invalid verification answer. Please try again.");
            return "redirect:/signup";
        }

        if (userRepository.findByUsername(trimmedUsername).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username '" + trimmedUsername + "' is already taken. Please choose another.");
            return "redirect:/signup";
        }

        if (userRepository.findByPhoneNumber(cleanPhone).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "An account with this mobile number is already registered.");
            return "redirect:/signup";
        }

        User newUser = new User();
        newUser.setFullName(trimmedName);
        newUser.setUsername(trimmedUsername);
        newUser.setPassword(passwordEncoder.encode(cleanPassword));
        newUser.setPhoneNumber(cleanPhone);
        newUser.setRoles("ROLE_STUDENT,ROLE_USER");
        newUser.setEnabled(true);
        userRepository.save(newUser);

        redirectAttributes.addFlashAttribute("signupSuccess", true);
        return "redirect:/login?signupSuccess=true";
    }
}


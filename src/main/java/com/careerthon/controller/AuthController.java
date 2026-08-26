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
            @RequestParam String captcha,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer expectedCaptcha = (Integer) session.getAttribute("captchaResult");
        if (expectedCaptcha == null || !captcha.equals(String.valueOf(expectedCaptcha))) {
            redirectAttributes.addFlashAttribute("error", "Invalid CAPTCHA answer.");
            return "redirect:/signup";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username already exists.");
            return "redirect:/signup";
        }

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRoles("ROLE_STUDENT,ROLE_USER");
        newUser.setEnabled(true);
        userRepository.save(newUser);

        redirectAttributes.addFlashAttribute("signupSuccess", true);
        return "redirect:/login?signupSuccess=true";
    }
}

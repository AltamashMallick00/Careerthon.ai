package com.careerthon.controller;

import com.careerthon.model.ProfileReview;
import com.careerthon.service.EmailService;
import com.careerthon.service.ProfileAnalyzerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final ProfileAnalyzerService analyzerService;
    private final EmailService emailService;

    public ReportController(ProfileAnalyzerService analyzerService, EmailService emailService) {
        this.analyzerService = analyzerService;
        this.emailService = emailService;
    }

    @GetMapping("/{id}")
    public String viewReport(@PathVariable Long id, Model model) {
        Optional<ProfileReview> optReview = analyzerService.getReview(id);
        if (optReview.isEmpty()) {
            return "redirect:/";
        }
        ProfileReview review = optReview.get();
        model.addAttribute("review", review);
        model.addAttribute("breakdown", review.getScoreBreakdown());
        return "report";
    }

    @PostMapping("/{id}/email")
    @ResponseBody
    public String emailReport(@PathVariable Long id, @RequestParam String email) {
        Optional<ProfileReview> optReview = analyzerService.getReview(id);
        if (optReview.isPresent()) {
            ProfileReview review = optReview.get();
            emailService.sendReportWithPdf(email, review);
            return "{\"success\":true,\"message\":\"Report sent with PDF to " + email + "\"}";
        }
        return "{\"success\":false,\"message\":\"Review not found\"}";
    }
}

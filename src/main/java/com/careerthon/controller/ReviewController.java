package com.careerthon.controller;

import com.careerthon.model.ProfileReview;
import com.careerthon.model.ResumeReview;
import com.careerthon.service.ProfileAnalyzerService;
import com.careerthon.service.ResumeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/review")
@SuppressWarnings("null")
public class ReviewController {

    private final ProfileAnalyzerService analyzerService;
    private final ResumeService resumeService;

    public ReviewController(ProfileAnalyzerService analyzerService, ResumeService resumeService) {
        this.analyzerService = analyzerService;
        this.resumeService = resumeService;
    }

    @GetMapping
    public String reviewForm() {
        return "review";
    }

    /**
     * Handles submissions from Home Page (LinkedIn PDF / URL) and Review page forms
     */
    @PostMapping("/submit")
    public String submitReview(
            @RequestParam(value = "linkedinUrl", required = false) String linkedinUrl,
            @RequestParam(value = "profilePdf", required = false) MultipartFile profilePdf,
            @RequestParam(value = "resume", required = false) MultipartFile resumeFile,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "userEmail", required = false) String userEmail,
            @RequestParam(value = "email", required = false) String emailParam) {

        String email = (userEmail != null && !userEmail.trim().isEmpty()) ? userEmail.trim() :
                      ((emailParam != null && !emailParam.trim().isEmpty()) ? emailParam.trim() : "");

        // 1. Resume Upload Scan
        if (resumeFile != null && !resumeFile.isEmpty()) {
            String name = (userName != null && !userName.trim().isEmpty()) ? userName.trim() : "Candidate";
            String candidateEmail = !email.isEmpty() ? email : "candidate@careerthon.ai";
            ResumeReview resumeReview = resumeService.analyzeResume(resumeFile, name, candidateEmail);
            return "redirect:/resume/results/" + resumeReview.getId();
        }

        // 2. LinkedIn Profile PDF or URL Scan
        boolean hasPdf = profilePdf != null && !profilePdf.isEmpty();
        boolean hasUrl = linkedinUrl != null && !linkedinUrl.trim().isEmpty();

        if (hasPdf || hasUrl) {
            ProfileReview review = analyzerService.createReview(linkedinUrl, profilePdf, email);
            return "redirect:/review/analyzing/" + review.getId();
        }

        return "redirect:/";
    }

    /**
     * Dedicated Resume Upload endpoint for /review page
     */
    @PostMapping("/upload")
    public String uploadResume(
            @RequestParam(value = "resume", required = false) MultipartFile resumeFile,
            @RequestParam(value = "profilePdf", required = false) MultipartFile profilePdf,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "userEmail", required = false) String userEmail,
            @RequestParam(value = "email", required = false) String emailParam,
            Model model) {

        MultipartFile file = (resumeFile != null && !resumeFile.isEmpty()) ? resumeFile : profilePdf;
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "Please select a valid PDF, DOCX, or text resume file to upload.");
            return "review";
        }

        try {
            String name = (userName != null && !userName.trim().isEmpty()) ? userName.trim() : "Candidate";
            String email = (userEmail != null && !userEmail.trim().isEmpty()) ? userEmail.trim() : 
                          ((emailParam != null && !emailParam.trim().isEmpty()) ? emailParam.trim() : "candidate@careerthon.ai");
            
            ResumeReview review = resumeService.analyzeResume(file, name, email);
            return "redirect:/resume/results/" + review.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Failed to analyze resume: " + e.getMessage());
            return "review";
        }
    }

    @GetMapping("/analyzing/{id}")
    public String analyzing(@PathVariable Long id, Model model) {
        model.addAttribute("reviewId", id);
        return "analyzing";
    }

    @GetMapping("/analyze/{id}")
    @ResponseBody
    public String triggerAnalysis(@PathVariable Long id) {
        ProfileReview review = analyzerService.analyzeProfile(id);
        if (review != null && review.getStatus() == ProfileReview.ReviewStatus.COMPLETED) {
            return "{\"status\":\"COMPLETED\",\"redirectUrl\":\"/report/" + id + "\"}";
        }
        return "{\"status\":\"FAILED\"}";
    }
}

package com.careerthon.controller;

import com.careerthon.model.ResumeReview;
import com.careerthon.repository.ResumeReviewRepository;
import com.careerthon.service.ResumeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/review")
@SuppressWarnings("null")
public class ReviewController {

    private final ResumeService resumeService;
    private final ResumeReviewRepository resumeReviewRepository;

    public ReviewController(ResumeService resumeService, ResumeReviewRepository resumeReviewRepository) {
        this.resumeService = resumeService;
        this.resumeReviewRepository = resumeReviewRepository;
    }

    @GetMapping
    public String reviewForm() {
        return "review";
    }

    @PostMapping({"/upload", "/submit"})
    public String uploadResume(@RequestParam(value = "resume", required = false) MultipartFile resumeFile,
                               @RequestParam(value = "profilePdf", required = false) MultipartFile profilePdf,
                               @RequestParam(value = "userName", required = false) String userName,
                               @RequestParam(value = "userEmail", required = false) String userEmail,
                               @RequestParam(value = "email", required = false) String emailParam,
                               Model model) {
        MultipartFile file = (resumeFile != null && !resumeFile.isEmpty()) ? resumeFile : profilePdf;
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "Please select a valid PDF or DOCX resume to upload.");
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
}

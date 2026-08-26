package com.careerthon.controller;

import com.careerthon.model.ProfileReview;
import com.careerthon.service.ProfileAnalyzerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/review")
public class ReviewController {

    private final ProfileAnalyzerService analyzerService;

    public ReviewController(ProfileAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @GetMapping
    public String reviewForm() {
        return "review";
    }

    @PostMapping("/submit")
    public String submitReview(@RequestParam String linkedinUrl,
                                @RequestParam(required = false) String email) {
        ProfileReview review = analyzerService.createReview(linkedinUrl, email);
        return "redirect:/review/analyzing/" + review.getId();
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

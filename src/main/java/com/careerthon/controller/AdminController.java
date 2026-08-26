package com.careerthon.controller;

import com.careerthon.model.Job;
import com.careerthon.model.ProfileReview;
import com.careerthon.model.ResumeReview;
import com.careerthon.repository.ProfileReviewRepository;
import com.careerthon.repository.ResumeReviewRepository;
import com.careerthon.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@SuppressWarnings("null")
public class AdminController {

    private static final int PAGE_SIZE = 50;

    private final ProfileReviewRepository profileReviewRepository;
    private final ResumeReviewRepository resumeReviewRepository;
    private final JobRepository jobRepository;

    public AdminController(ProfileReviewRepository profileReviewRepository, 
                           ResumeReviewRepository resumeReviewRepository,
                           JobRepository jobRepository) {
        this.profileReviewRepository = profileReviewRepository;
        this.resumeReviewRepository = resumeReviewRepository;
        this.jobRepository = jobRepository;
    }

    @GetMapping("/dashboard")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String dashboard(
            @RequestParam(defaultValue = "0") int profilePage,
            @RequestParam(defaultValue = "0") int resumePage,
            @RequestParam(defaultValue = "") String profileSearch,
            @RequestParam(defaultValue = "") String resumeSearch,
            Model model) {

        // ── Paginated Profile Reviews ───────────────────────────────────────
        Pageable profilePageable = PageRequest.of(profilePage, PAGE_SIZE);
        Page<ProfileReview> profileReviews;
        if (profileSearch != null && !profileSearch.trim().isEmpty()) {
            String q = profileSearch.trim();
            profileReviews = profileReviewRepository
                .findByUserNameContainingIgnoreCaseOrLinkedinUrlContainingIgnoreCaseOrEmailAddressContainingIgnoreCase(
                    q, q, q, profilePageable);
        } else {
            profileReviews = profileReviewRepository.findAllByOrderByCreatedAtDesc(profilePageable);
        }

        // ── Paginated Resume Reviews ────────────────────────────────────────
        Pageable resumePageable = PageRequest.of(resumePage, PAGE_SIZE);
        Page<ResumeReview> resumeReviews;
        if (resumeSearch != null && !resumeSearch.trim().isEmpty()) {
            String q = resumeSearch.trim();
            resumeReviews = resumeReviewRepository
                .findByUserNameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(
                    q, q, resumePageable);
        } else {
            resumeReviews = resumeReviewRepository.findAllByOrderByUploadedAtDesc(resumePageable);
        }

        // ── Computed Stats ──────────────────────────────────────────────────
        long totalProfiles = profileReviewRepository.count();
        long totalResumes = resumeReviewRepository.count();
        long totalUsers = totalProfiles + totalResumes;
        double avgProfileScore = profileReviewRepository.findAverageOverallScore();
        double avgAtsScore = resumeReviewRepository.findAverageAtsScore();

        // ── Model Attributes ────────────────────────────────────────────────
        model.addAttribute("reviews", profileReviews);
        model.addAttribute("resumes", resumeReviews);
        model.addAttribute("jobs", jobRepository.findAllByOrderByCreatedAtDesc());

        // Stats
        model.addAttribute("totalProfiles", totalProfiles);
        model.addAttribute("totalResumes", totalResumes);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("avgProfileScore", String.format("%.1f", avgProfileScore));
        model.addAttribute("avgAtsScore", String.format("%.1f", avgAtsScore));

        // Pagination state
        model.addAttribute("profilePage", profilePage);
        model.addAttribute("resumePage", resumePage);
        model.addAttribute("profileSearch", profileSearch);
        model.addAttribute("resumeSearch", resumeSearch);

        return "admin/dashboard";
    }

    @PostMapping("/profile/suggest")
    public String suggestProfile(@RequestParam Long id, @RequestParam String suggestions) {
        profileReviewRepository.findById(id).ifPresent(review -> {
            review.setAdminSuggestions(suggestions);
            profileReviewRepository.save(review);
        });
        return "redirect:/admin/dashboard?success=true";
    }

    @PostMapping("/resume/suggest")
    public String suggestResume(@RequestParam Long id, @RequestParam String suggestions) {
        resumeReviewRepository.findById(id).ifPresent(resume -> {
            resume.setAdminSuggestions(suggestions);
            resumeReviewRepository.save(resume);
        });
        return "redirect:/admin/dashboard?success=true";
    }

    @PostMapping("/job/add")
    public String addJob(@RequestParam String title,
                         @RequestParam String commitment,
                         @RequestParam String location,
                         @RequestParam String description) {
        Job job = new Job(title, commitment, location, description);
        jobRepository.save(job);
        return "redirect:/admin/dashboard?success=true";
    }

    @PostMapping("/job/delete")
    public String deleteJob(@RequestParam Long id) {
        jobRepository.deleteById(id);
        return "redirect:/admin/dashboard?success=true";
    }
}


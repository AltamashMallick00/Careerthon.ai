package com.careerthon.controller;

import com.careerthon.model.*;
import com.careerthon.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@SuppressWarnings("null")
public class AdminController {

    private static final int PAGE_SIZE = 50;

    private final ProfileReviewRepository profileReviewRepository;
    private final ResumeReviewRepository resumeReviewRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    public AdminController(ProfileReviewRepository profileReviewRepository, 
                           ResumeReviewRepository resumeReviewRepository,
                           JobRepository jobRepository,
                           UserRepository userRepository,
                           StudentProfileRepository studentProfileRepository) {
        this.profileReviewRepository = profileReviewRepository;
        this.resumeReviewRepository = resumeReviewRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
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

    // ─── CSV EXPORT CAPABILITIES ─────────────────────────────────────────────
    @GetMapping("/export/profiles/csv")
    public void exportProfilesCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"careerthon_profiles_" + System.currentTimeMillis() + ".csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("ID,Candidate Name,Email,LinkedIn URL,Overall Score,Headline Score,About Score,Experience Score,Created At,Admin Suggestions");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ProfileReview r : profileReviewRepository.findAll()) {
            int hScore = r.getScoreBreakdown() != null ? r.getScoreBreakdown().getHeadline() : 0;
            int aScore = r.getScoreBreakdown() != null ? r.getScoreBreakdown().getAboutSection() : 0;
            int eScore = r.getScoreBreakdown() != null ? r.getScoreBreakdown().getExperience() : 0;
            String date = r.getCreatedAt() != null ? r.getCreatedAt().format(dtf) : "";

            writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",%d,%d,%d,%d,\"%s\",\"%s\"",
                    r.getId(),
                    cleanCsv(r.getUserName()),
                    cleanCsv(r.getEmailAddress()),
                    cleanCsv(r.getLinkedinUrl()),
                    r.getOverallScore(),
                    hScore,
                    aScore,
                    eScore,
                    date,
                    cleanCsv(r.getAdminSuggestions())
            ));
        }
        writer.flush();
    }

    @GetMapping("/export/resumes/csv")
    public void exportResumesCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"careerthon_resumes_" + System.currentTimeMillis() + ".csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("ID,Candidate Name,Email,Target Roles,ATS Score,Uploaded At,Admin Suggestions");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ResumeReview r : resumeReviewRepository.findAll()) {
            String date = r.getUploadedAt() != null ? r.getUploadedAt().format(dtf) : "";
            writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\"",
                    r.getId(),
                    cleanCsv(r.getUserName()),
                    cleanCsv(r.getUserEmail()),
                    cleanCsv(r.getSuggestedRoles()),
                    r.getAtsScore(),
                    date,
                    cleanCsv(r.getAdminSuggestions())
            ));
        }
        writer.flush();
    }

    @GetMapping("/export/users/csv")
    public void exportUsersCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"careerthon_users_" + System.currentTimeMillis() + ".csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("ID,Username,Full Name,Mobile Number,Roles,Status");

        for (User u : userRepository.findAll()) {
            writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    u.getId(),
                    cleanCsv(u.getUsername()),
                    cleanCsv(u.getFullName()),
                    cleanCsv(u.getPhoneNumber()),
                    cleanCsv(u.getRoles()),
                    u.isEnabled() ? "ACTIVE" : "BLOCKED"
            ));
        }
        writer.flush();
    }

    @GetMapping("/export/students/csv")
    public void exportStudentsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"careerthon_students_" + System.currentTimeMillis() + ".csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("ID,Student Name,College,Branch,Graduation Year,Mobile Number");

        for (StudentProfile s : studentProfileRepository.findAll()) {
            String studentName = s.getUser() != null ? s.getUser().getFullName() : "";
            writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    s.getId(),
                    cleanCsv(studentName),
                    cleanCsv(s.getCollegeName()),
                    cleanCsv(s.getBranch()),
                    cleanCsv(s.getGraduationYear()),
                    cleanCsv(s.getMobileNumber())
            ));
        }
        writer.flush();
    }

    private String cleanCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
    }
}


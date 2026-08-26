package com.careerthon.controller;

import com.careerthon.model.ResumeReview;
import com.careerthon.repository.ResumeReviewRepository;
import com.careerthon.service.ResumeService;
import com.careerthon.service.EmailService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/resume")
@SuppressWarnings("null")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeReviewRepository resumeReviewRepository;
    private final EmailService emailService;

    public ResumeController(ResumeService resumeService, ResumeReviewRepository resumeReviewRepository, EmailService emailService) {
        this.resumeService = resumeService;
        this.resumeReviewRepository = resumeReviewRepository;
        this.emailService = emailService;
    }

    @GetMapping
    public String resumeHome() {
        return "resume/index";
    }

    @GetMapping("/bulk")
    public String bulkResumeHome() {
        return "resume/bulk";
    }

    @PostMapping("/upload")
    public String uploadResume(@RequestParam("resume") MultipartFile file, 
                              @RequestParam("userName") String userName,
                              @RequestParam("userEmail") String userEmail,
                              Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload.");
            return "resume/index";
        }

        try {
            ResumeReview review = resumeService.analyzeResume(file, userName, userEmail);
            return "redirect:/resume/results/" + review.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Failed to analyze resume: " + e.getMessage());
            return "resume/index";
        }
    }

    @PostMapping("/bulk/upload")
    public String uploadBulkResumes(@RequestParam("resumes") List<MultipartFile> files, 
                                    @RequestParam("userName") String userName,
                                    @RequestParam("userEmail") String userEmail,
                                    Model model) {
        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            model.addAttribute("error", "Please select at least one file to upload.");
            return "resume/bulk";
        }

        if (files.size() > 20) {
            model.addAttribute("error", "Bulk scanner capacity exceeded. You can upload up to 20 CVs at one time.");
            return "resume/bulk";
        }

        List<ResumeReview> reviews = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    ResumeReview review = resumeService.analyzeResume(file, userName, userEmail);
                    reviews.add(review);
                }
            }
            model.addAttribute("reviews", reviews);
            model.addAttribute("recruiterName", userName);
            model.addAttribute("recruiterEmail", userEmail);
            return "resume/bulk-results";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process bulk upload: " + e.getMessage());
            return "resume/bulk";
        }
    }

    @GetMapping("/results/{id}")
    public String results(@PathVariable("id") Long id, Model model) {
        ResumeReview review = resumeReviewRepository.findById(id).orElse(null);
        if (review == null) return "redirect:/resume";
        model.addAttribute("review", review);
        return "resume/results";
    }

    @GetMapping("/templates")
    public String templates() {
        return "resume/templates";
    }

    @GetMapping("/download/{type}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable String type) {
        byte[] pdfContent = resumeService.generateTemplatePdf(type);
        String filename = type.equalsIgnoreCase("fresher") ? "Fresher_Template.pdf" : "Corporate_Pro_Template.pdf";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> viewResume(@PathVariable Long id) {
        if (id == null) return ResponseEntity.badRequest().build();
        ResumeReview review = resumeReviewRepository.findById(id).orElse(null);
        if (review == null || review.getFileData() == null) return ResponseEntity.notFound().build();
        
        String fileName = review.getFileName() != null ? review.getFileName() : "resume.pdf";
        MediaType mediaType = MediaType.APPLICATION_PDF;
        if (fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".doc")) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(mediaType)
                .body(review.getFileData());
    }

    @PostMapping("/bulk/contact")
    @ResponseBody
    public ResponseEntity<String> sendBulkOutreach(@RequestBody BulkContactRequest request) {
        if (request == null || request.emails == null || request.emails.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"No candidates selected.\"}");
        }

        int successCount = 0;
        for (int i = 0; i < request.emails.size(); i++) {
            String email = request.emails.get(i);
            String candidateName = (request.names != null && request.names.size() > i) ? request.names.get(i) : "Candidate";
            
            // Personalize the generic message with the candidate's actual name!
            String customizedMessage = request.message.replace("[Candidate Name]", candidateName).replace("[Candidate]", candidateName);

            boolean sent = emailService.sendRecruiterInvite(
                email, 
                candidateName, 
                request.recruiterName, 
                request.recruiterEmail, 
                request.subject, 
                customizedMessage
            );
            if (sent) successCount++;
        }

        return ResponseEntity.ok("{\"success\":true,\"message\":\"Successfully dispatched " + successCount + " recruiter outreach invites!\"}");
    }

    public static class BulkContactRequest {
        public List<String> emails;
        public List<String> names;
        public String recruiterName;
        public String recruiterEmail;
        public String subject;
        public String message;
    }
}

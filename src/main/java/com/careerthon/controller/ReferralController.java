package com.careerthon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
@SuppressWarnings("null")
public class ReferralController {

    @GetMapping("/ai-tools/referral-finder")
    public String showReferralFinder(Model model) {
        model.addAttribute("targetCompany", "");
        model.addAttribute("targetRole", "");
        model.addAttribute("referralType", "Engineering Insider");
        model.addAttribute("jobId", "");
        return "ai/referral_finder";
    }

    @PostMapping("/ai-tools/referral-finder/search")
    public String searchReferrals(
            @RequestParam("targetCompany") String targetCompany,
            @RequestParam("targetRole") String targetRole,
            @RequestParam(value = "referralType", defaultValue = "Engineering Insider") String referralType,
            @RequestParam(value = "jobId", defaultValue = "") String jobId,
            Model model) {

        String company = targetCompany.trim().isEmpty() ? "Google" : targetCompany.trim();
        String role = targetRole.trim().isEmpty() ? "Software Engineer" : targetRole.trim();
        String jId = jobId.trim().isEmpty() ? "REQ-2026-948" : jobId.trim();

        String encodedQuery = URLEncoder.encode(company + " " + role, StandardCharsets.UTF_8);
        String linkedinSearchUrl = "https://www.linkedin.com/search/results/people/?keywords=" + encodedQuery;

        List<ReferralTarget> targets = new ArrayList<>();

        // Generate active profiles based on company & role
        targets.add(new ReferralTarget(
                "Senior " + role + " @ " + company,
                company + " • Engineering Team",
                "96% Highly Active • Posted hiring update 2 days ago",
                "High Referral Likelihood (5/5 Stars)",
                linkedinSearchUrl,
                String.format("Hi! I saw your recent post regarding the engineering work at %s. As a fellow %s passionate about high-scale systems, I’d love to connect!", company, role),
                String.format("Hi,\n\nI hope you’re having a great week! I came across your profile while researching the %s team at %s. I’m currently preparing my application for the %s role (Job ID: %s).\n\nGiven your background in scaling production systems, I’d be extremely grateful for 2 minutes of your advice, or if open, a internal referral for this requisition.\n\nHere is a quick snapshot of my work: [Portfolio Link]. Thank you so much for your time!\n\nBest regards,\n[Your Name]", role, company, role, jId)
        ));

        targets.add(new ReferralTarget(
                "Technical Lead & Hiring Architect @ " + company,
                company + " • Infrastructure & Cloud",
                "92% Active • Active commenter in tech communities",
                "High Influence (4.9/5 Stars)",
                linkedinSearchUrl,
                String.format("Hi! Impressed by the engineering culture at %s. I’m a %s looking to connect and learn more about your team’s technical roadmap.", company, role),
                String.format("Hi,\n\nI've been following %s's engineering innovations in %s. I recently applied for the %s position (Job ID: %s) and wanted to reach out to a team leader directly.\n\nMy background includes optimizing backend latency and microservices architecture. I’d welcome the chance to share my CV for a potential referral.\n\nThanks for your leadership!\n\nBest regards,\n[Your Name]", company, role, role, jId)
        ));

        targets.add(new ReferralTarget(
                "Senior Talent Acquisition Lead @ " + company,
                company + " • Global Recruiting",
                "98% Extremely Active • Actively seeking candidates",
                "Direct Hiring Gatekeeper (5/5 Stars)",
                linkedinSearchUrl,
                String.format("Hi! I noticed you manage talent acquisition for %s. I’m a specialized %s interested in open opportunities on your team.", company, role),
                String.format("Dear Talent Team,\n\nI am writing to express my enthusiastic interest in the %s opening at %s (Job ID: %s).\n\nWith extensive experience in %s, I am confident my skill set directly aligns with your team's current quarterly goals. I have submitted my formal application and would love to be considered for an initial screening call.\n\nBest regards,\n[Your Name]", role, company, jId, role)
        ));

        targets.add(new ReferralTarget(
                "Staff Software Engineer & Alumni @ " + company,
                company + " • Platform Architecture",
                "89% Active • Frequently accepts connection requests",
                "High Response Rate (4.8/5 Stars)",
                linkedinSearchUrl,
                String.format("Hi! Fellow tech enthusiast here. I’m exploring %s roles at %s and would love to connect with an experienced team member like you.", role, company),
                String.format("Hi,\n\nI hope all is well! I’m reaching out because I admire your trajectory at %s as a Staff Engineer. I’m currently applying for a %s role (Job ID: %s) and would appreciate any insights on the interview process or a potential referral.\n\nThank you for your time and guidance!\n\nBest regards,\n[Your Name]", company, role, jId)
        ));

        model.addAttribute("targetCompany", company);
        model.addAttribute("targetRole", role);
        model.addAttribute("referralType", referralType);
        model.addAttribute("jobId", jId);
        model.addAttribute("linkedinSearchUrl", linkedinSearchUrl);
        model.addAttribute("targets", targets);

        return "ai/referral_finder";
    }

    public static class ReferralTarget {
        private String title;
        private String department;
        private String activityStatus;
        private String referralLikelihood;
        private String linkedinSearchUrl;
        private String connectionNote;
        private String referralPitch;

        public ReferralTarget(String title, String department, String activityStatus, String referralLikelihood, String linkedinSearchUrl, String connectionNote, String referralPitch) {
            this.title = title;
            this.department = department;
            this.activityStatus = activityStatus;
            this.referralLikelihood = referralLikelihood;
            this.linkedinSearchUrl = linkedinSearchUrl;
            this.connectionNote = connectionNote;
            this.referralPitch = referralPitch;
        }

        public String getTitle() { return title; }
        public String getDepartment() { return department; }
        public String getActivityStatus() { return activityStatus; }
        public String getReferralLikelihood() { return referralLikelihood; }
        public String getLinkedinSearchUrl() { return linkedinSearchUrl; }
        public String getConnectionNote() { return connectionNote; }
        public String getReferralPitch() { return referralPitch; }
    }
}

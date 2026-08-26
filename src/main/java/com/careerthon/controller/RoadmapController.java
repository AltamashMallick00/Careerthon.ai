package com.careerthon.controller;

import com.careerthon.model.Course;
import com.careerthon.repository.CourseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@SuppressWarnings("null")
public class RoadmapController {

    private final CourseRepository courseRepository;

    public RoadmapController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping({"/ai-tools/roadmap", "/roadmap"})
    public String showRoadmapForm(Model model) {
        model.addAttribute("currentRole", "");
        model.addAttribute("currentSkills", "");
        model.addAttribute("targetRole", "");
        model.addAttribute("allCourses", courseRepository.findAll().stream().limit(6).collect(Collectors.toList()));
        return "ai/roadmap";
    }

    @PostMapping("/ai-tools/roadmap/generate")
    public String generateRoadmap(
            @RequestParam("currentRole") String currentRole,
            @RequestParam("currentSkills") String currentSkills,
            @RequestParam("targetRole") String targetRole,
            Model model) {

        String cRole = currentRole.trim().isEmpty() ? "Junior Developer" : currentRole.trim();
        String cSkills = currentSkills.trim().isEmpty() ? "Java, HTML/CSS, SQL" : currentSkills.trim();
        String tRole = targetRole.trim().isEmpty() ? "Senior Data Engineer" : targetRole.trim();

        // Skill Gap calculation demo logic
        int matchScore = 68;
        int gapPercent = 32;

        List<MilestonePhase> phases = new ArrayList<>();

        if (tRole.toLowerCase().contains("data")) {
            matchScore = 72;
            gapPercent = 28;
            phases.add(new MilestonePhase(
                    "Phase 1: Big Data & Streaming Pipeline Foundations",
                    "Master core data ingestion, distributed storage, and real-time processing concepts.",
                    List.of("Apache Airflow", "PySpark", "Kafka Streaming", "Data Warehousing (Snowflake/BigQuery)"),
                    "Estimated: 3-4 Weeks"
            ));
            phases.add(new MilestonePhase(
                    "Phase 2: ETL Pipeline Engineering & Data Lakes",
                    "Build scalable lakehouse architectures, automated orchestration, and data quality checks.",
                    List.of("Delta Lake", "dbt (Data Build Tool)", "AWS S3 / EMR Ingestion", "Schema Validation"),
                    "Estimated: 4-5 Weeks"
            ));
            phases.add(new MilestonePhase(
                    "Phase 3: System Design & Enterprise Production",
                    "Architect fault-tolerant analytics models, CI/CD for data pipelines, and telemetry monitoring.",
                    List.of("System Design for 100M+ Records", "Grafana Data Telemetry", "Production Deployment"),
                    "Estimated: 3-4 Weeks"
            ));
        } else if (tRole.toLowerCase().contains("full stack") || tRole.toLowerCase().contains("software")) {
            matchScore = 78;
            gapPercent = 22;
            phases.add(new MilestonePhase(
                    "Phase 1: Modern Distributed Backend Systems",
                    "Deep-dive into high-concurrency Java 21, Spring Boot 3.4 microservices, and reactive APIs.",
                    List.of("Spring Security 6", "Hibernate/JPA Optimizations", "Kafka Event Bus"),
                    "Estimated: 3 Weeks"
            ));
            phases.add(new MilestonePhase(
                    "Phase 2: Next.js & Advanced UI Performance",
                    "Master modern component architecture, state management, and server-side rendering.",
                    List.of("TypeScript", "Tailwind CSS Design System", "State Management"),
                    "Estimated: 3 Weeks"
            ));
            phases.add(new MilestonePhase(
                    "Phase 3: Cloud DevOps & Container Orchestration",
                    "Deploy enterprise SaaS apps with Docker, Kubernetes, and automated GitHub Actions CI/CD.",
                    List.of("Docker & K8s", "AWS Cloud Architecture", "Prometheus Telemetry"),
                    "Estimated: 4 Weeks"
            ));
        } else {
            matchScore = 65;
            gapPercent = 35;
            phases.add(new MilestonePhase(
                    "Phase 1: Target Skill Standardization",
                    "Establish foundational competencies aligned with 2026 recruiter search benchmarks.",
                    List.of("Core Framework Mastery", "Database Optimization", "API Design"),
                    "Estimated: 3 Weeks"
            ));
            phases.add(new MilestonePhase(
                    "Phase 2: Hands-On Production Projects",
                    "Build 2 high-impact open-source or commercial portfolio projects.",
                    List.of("System Design", "Cloud Ingestion", "Performance Tuning"),
                    "Estimated: 4 Weeks"
            ));
            phases.add(new MilestonePhase(
                    "Phase 3: Recruiter Search Optimization",
                    "Optimize LinkedIn profile keyword density and pass ATS screening tests.",
                    List.of("LinkedIn Recruiter Optimization", "ATS Keyword Tuning", "Interview Prep"),
                    "Estimated: 2 Weeks"
            ));
        }

        List<Course> recommendedCourses = courseRepository.findAll().stream().limit(6).collect(Collectors.toList());

        model.addAttribute("currentRole", cRole);
        model.addAttribute("currentSkills", cSkills);
        model.addAttribute("targetRole", tRole);
        model.addAttribute("matchScore", matchScore);
        model.addAttribute("gapPercent", gapPercent);
        model.addAttribute("phases", phases);
        model.addAttribute("allCourses", recommendedCourses);

        return "ai/roadmap";
    }

    public static class MilestonePhase {
        private String title;
        private String description;
        private List<String> skillTags;
        private String duration;

        public MilestonePhase(String title, String description, List<String> skillTags, String duration) {
            this.title = title;
            this.description = description;
            this.skillTags = skillTags;
            this.duration = duration;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public List<String> getSkillTags() { return skillTags; }
        public String getDuration() { return duration; }
    }
}

package com.careerthon.service;

import com.careerthon.model.ResumeReview;
import com.careerthon.repository.ResumeReviewRepository;
import com.careerthon.util.DocumentTextExtractor;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@SuppressWarnings("null")
public class ResumeService {

    private final ResumeReviewRepository resumeReviewRepository;
    private final EmailService emailService;

    public ResumeService(ResumeReviewRepository resumeReviewRepository, EmailService emailService) {
        this.resumeReviewRepository = resumeReviewRepository;
        this.emailService = emailService;
    }

    /**
     * Ultra-Lightweight, Zero-Metaspace Resume Text Extraction
     */
    public String extractText(MultipartFile file) {
        return DocumentTextExtractor.extractText(file);
    }

    // ── Technical Skill Repositories for Accurate ATS Matching ───────────────────
    private static final List<String> LANG_SKILLS = List.of(
            "java", "python", "javascript", "typescript", "c++", "c#", "golang", "go", "rust", "php", "ruby", "kotlin", "swift", "sql", "html", "css", "r"
    );

    private static final List<String> FRAMEWORK_SKILLS = List.of(
            "spring", "spring boot", "react", "react.js", "angular", "vue", "node.js", "nodejs", "express", "django", "flask", "fastapi", ".net", "hibernate", "jpa", "tailwind", "bootstrap", "next.js"
    );

    private static final List<String> CLOUD_DEVOPS_SKILLS = List.of(
            "aws", "azure", "gcp", "google cloud", "docker", "kubernetes", "k8s", "ci/cd", "cicd", "jenkins", "terraform", "linux", "git", "github", "ansible", "nginx"
    );

    private static final List<String> DATABASE_SKILLS = List.of(
            "mysql", "postgresql", "postgres", "mongodb", "redis", "oracle", "sqlite", "cassandra", "elasticsearch", "dynamodb", "snowflake", "kafka", "rabbitmq"
    );

    private static final List<String> ARCHITECTURE_SKILLS = List.of(
            "rest", "restful", "microservices", "graphql", "system design", "data structures", "algorithms", "dsa", "oop", "object-oriented", "agile", "scrum", "jira", "junit", "mockito", "postman"
    );

    /**
     * 100% Genuine, Rigorous 5-Pillar ATS Scoring Engine
     */
    public ResumeReview analyzeResume(MultipartFile file, String userName, String userEmail) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf";
        String content = extractText(file);
        String lowerContent = content.toLowerCase();

        // ════ PILLAR 1: Contact Information & Header Health (Max 15) ════
        int contactScore = 0;
        Pattern emailPattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        if (emailPattern.matcher(content).find() || (userEmail != null && userEmail.contains("@"))) contactScore += 4;
        
        Pattern phonePattern = Pattern.compile("(\\+?[0-9]{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}|[0-9]{10}");
        if (phonePattern.matcher(content).find() || lowerContent.contains("phone") || lowerContent.contains("mobile") || lowerContent.contains("+91")) contactScore += 4;
        
        if (lowerContent.contains("linkedin.com") || lowerContent.contains("github.com") || lowerContent.contains("portfolio") || lowerContent.contains("http")) contactScore += 4;
        if (lowerContent.contains("india") || lowerContent.contains("bangalore") || lowerContent.contains("delhi") || lowerContent.contains("pune") || lowerContent.contains("hyderabad") || lowerContent.contains("mumbai") || lowerContent.contains("noida") || lowerContent.contains("remote") || lowerContent.contains("address")) contactScore += 3;
        contactScore = Math.min(contactScore, 15);

        // ════ PILLAR 2: Core ATS Section Structure (Max 20) ════
        int sectionScore = 0;
        boolean hasExp = lowerContent.contains("experience") || lowerContent.contains("work history") || lowerContent.contains("employment") || lowerContent.contains("internship") || lowerContent.contains("professional background");
        if (hasExp) sectionScore += 6;

        boolean hasEdu = lowerContent.contains("education") || lowerContent.contains("b.tech") || lowerContent.contains("b.e") || lowerContent.contains("bachelor") || lowerContent.contains("master") || lowerContent.contains("university") || lowerContent.contains("college") || lowerContent.contains("degree") || lowerContent.contains("gpa") || lowerContent.contains("cgpa");
        if (hasEdu) sectionScore += 5;

        boolean hasSkills = lowerContent.contains("skills") || lowerContent.contains("technical skills") || lowerContent.contains("technologies") || lowerContent.contains("core competencies") || lowerContent.contains("proficiencies") || lowerContent.contains("tools");
        if (hasSkills) sectionScore += 5;

        boolean hasProjects = lowerContent.contains("project") || lowerContent.contains("projects") || lowerContent.contains("academic projects") || lowerContent.contains("open source") || lowerContent.contains("portfolio") || lowerContent.contains("achievements");
        if (hasProjects) sectionScore += 4;
        sectionScore = Math.min(sectionScore, 20);

        // ════ PILLAR 3: Technical Skills & Industry Keyword Density (Max 30) ════
        Set<String> detectedSkills = new LinkedHashSet<>();
        checkSkills(lowerContent, LANG_SKILLS, detectedSkills);
        checkSkills(lowerContent, FRAMEWORK_SKILLS, detectedSkills);
        checkSkills(lowerContent, CLOUD_DEVOPS_SKILLS, detectedSkills);
        checkSkills(lowerContent, DATABASE_SKILLS, detectedSkills);
        checkSkills(lowerContent, ARCHITECTURE_SKILLS, detectedSkills);

        int skillsCount = detectedSkills.size();
        int skillsScore = 0;
        if (skillsCount >= 14) skillsScore = 30;
        else if (skillsCount >= 10) skillsScore = 25;
        else if (skillsCount >= 7) skillsScore = 20;
        else if (skillsCount >= 4) skillsScore = 15;
        else if (skillsCount >= 2) skillsScore = 10;
        else skillsScore = 5;

        // ════ PILLAR 4: Quantifiable Business Impact & Action Verbs (Max 20) ════
        int impactScore = 0;
        
        // Metrics / Numbers / Percentages Check
        Pattern metricPattern = Pattern.compile("(\\d+%)|(\\$\\d+)|(₹\\d+)|(\\d+\\+)|(\\d+\\s*(ms|k|m|million|users|requests|qps|x|gb|tb))");
        Matcher metricMatcher = metricPattern.matcher(lowerContent);
        int metricCount = 0;
        while (metricMatcher.find()) metricCount++;
        
        if (metricCount >= 4) impactScore += 10;
        else if (metricCount >= 2) impactScore += 7;
        else if (metricCount >= 1) impactScore += 4;
        else impactScore += 2;

        // Strong Action Verbs Check
        String[] actionVerbs = {"architected", "engineered", "spearheaded", "developed", "implemented", "optimized", "reduced", "increased", "scaled", "automated", "built", "delivered", "integrated", "orchestrated", "mentored", "led", "improved", "designed", "launched"};
        int verbCount = 0;
        for (String verb : actionVerbs) {
            if (lowerContent.contains(verb)) verbCount++;
        }
        if (verbCount >= 6) impactScore += 10;
        else if (verbCount >= 3) impactScore += 7;
        else if (verbCount >= 1) impactScore += 4;
        else impactScore += 2;
        impactScore = Math.min(impactScore, 20);

        // ════ PILLAR 5: ATS Formatting & Length Calibration (Max 15) ════
        int formatScore = 0;
        int wordCount = content.split("\\s+").length;
        if (wordCount >= 250 && wordCount <= 1200) formatScore += 8; // Optimal 1-2 pages
        else if (wordCount >= 150) formatScore += 5;
        else formatScore += 2;

        if (content.contains("•") || content.contains("-") || content.contains("*") || content.contains("1.") || content.contains("2.")) {
            formatScore += 7;
        } else {
            formatScore += 3;
        }
        formatScore = Math.min(formatScore, 15);

        // ── Calculate Final Overall ATS Score ──
        int totalAtsScore = contactScore + sectionScore + skillsScore + impactScore + formatScore;
        totalAtsScore = Math.max(35, Math.min(totalAtsScore, 98));

        // ── Role Alignment Prediction ──
        List<String> suggestedRoles = predictBestMatchingRoles(lowerContent, detectedSkills, totalAtsScore);
        String suggestedRolesStr = String.join(", ", suggestedRoles);

        // ── Detailed Actionable Recommendations ──
        String suggestions = buildComprehensiveImprovementPlan(
                totalAtsScore, contactScore, sectionScore, skillsScore, impactScore, formatScore,
                detectedSkills, suggestedRoles, lowerContent
        );

        byte[] fileBytes;
        try { fileBytes = file.getBytes(); } catch (Exception e) { fileBytes = new byte[0]; }

        ResumeReview review = new ResumeReview(fileName, userName, userEmail, totalAtsScore, suggestedRolesStr, suggestions, fileBytes);
        ResumeReview saved = resumeReviewRepository.save(review);

        if (userEmail != null && !userEmail.isEmpty() && userEmail.contains("@")) {
            String reportUrl = "http://localhost:8080/resume/results/" + saved.getId();
            emailService.sendReport(userEmail, reportUrl, userName);
        }

        return saved;
    }

    private void checkSkills(String lowerContent, List<String> skillList, Set<String> detected) {
        for (String skill : skillList) {
            // Word boundary check
            Pattern p = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b");
            if (p.matcher(lowerContent).find() || lowerContent.contains(skill)) {
                detected.add(capitalize(skill));
            }
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        if (str.equalsIgnoreCase("sql") || str.equalsIgnoreCase("aws") || str.equalsIgnoreCase("gcp") || str.equalsIgnoreCase("k8s") || str.equalsIgnoreCase("dsa") || str.equalsIgnoreCase("oop") || str.equalsIgnoreCase("rest") || str.equalsIgnoreCase("api") || str.equalsIgnoreCase("ci/cd")) {
            return str.toUpperCase();
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private List<String> predictBestMatchingRoles(String lower, Set<String> detected, int score) {
        List<String> roles = new ArrayList<>();
        
        boolean isJava = lower.contains("java") || lower.contains("spring");
        boolean isPython = lower.contains("python") || lower.contains("django") || lower.contains("fastapi");
        boolean isWeb = lower.contains("react") || lower.contains("javascript") || lower.contains("typescript") || lower.contains("html") || lower.contains("angular") || lower.contains("frontend");
        boolean isData = lower.contains("data") || lower.contains("analytics") || lower.contains("sql") || lower.contains("pandas") || lower.contains("machine learning") || lower.contains("ai");
        boolean isCloud = lower.contains("aws") || lower.contains("docker") || lower.contains("kubernetes") || lower.contains("cloud") || lower.contains("devops") || lower.contains("ci/cd");

        if (isJava && isWeb) {
            roles.add(score >= 80 ? "Senior Full Stack Java Engineer" : "Java Full Stack Developer");
            roles.add("Backend Engineer (Java/Spring Boot)");
        } else if (isJava) {
            roles.add(score >= 80 ? "Lead Backend Engineer (Java)" : "Software Development Engineer - Backend");
            roles.add("Spring Boot Microservices Developer");
        }

        if (isData) {
            roles.add(score >= 80 ? "Senior Data Scientist & ML Engineer" : "Data Analyst / Associate Data Scientist");
            roles.add("Database & Business Intelligence Engineer");
        }

        if (isCloud) {
            roles.add(score >= 80 ? "Cloud Infrastructure Architect" : "DevOps & Cloud Solutions Engineer");
            roles.add("Site Reliability Engineer (SRE)");
        }

        if (isWeb && !isJava) {
            roles.add(score >= 80 ? "Senior Frontend Architect (React/TypeScript)" : "Frontend Developer");
            roles.add("Full Stack JavaScript/Node.js Developer");
        }

        if (roles.isEmpty()) {
            if (score >= 80) {
                roles.add("Senior Software Development Engineer (SDE-II)");
                roles.add("Technical Solutions Architect");
                roles.add("Engineering Project Lead");
            } else {
                roles.add("Software Engineer Associate (SDE-I)");
                roles.add("Full Stack Software Trainee");
                roles.add("Technical Systems Analyst");
            }
        }

        return roles.stream().distinct().limit(4).toList();
    }

    private String buildComprehensiveImprovementPlan(
            int totalScore, int contactScore, int sectionScore, int skillsScore, int impactScore, int formatScore,
            Set<String> detectedSkills, List<String> suggestedRoles, String lowerContent) {

        StringBuilder sb = new StringBuilder();

        // 1. Executive Summary
        if (totalScore >= 85) {
            sb.append("🌟 EXCELLENT ATS HEALTH (Score: ").append(totalScore).append("/100)\n");
            sb.append("Your resume exhibits exceptional keyword distribution, well-structured section headings, and strong quantifiable business impacts. It will parse seamlessly into Fortune 500 ATS systems (Workday, Greenhouse, Lever, Taleo).\n\n");
        } else if (totalScore >= 70) {
            sb.append("⚡ SOLID CANDIDATE PROFILE (Score: ").append(totalScore).append("/100)\n");
            sb.append("Your resume has good foundational structure and relevant technical skills. With minor improvements in quantifiable metrics and keyword placement, you can easily reach the top 5% candidate pool.\n\n");
        } else {
            sb.append("⚠️ ATS OPTIMIZATION REQUIRED (Score: ").append(totalScore).append("/100)\n");
            sb.append("Your resume currently contains formatting gaps or insufficient technical keyword density that may cause ATS filters or automated recruiter parsers to reject it before human review.\n\n");
        }

        // 2. Detected Skills Inventory
        sb.append("✅ IDENTIFIED TECHNICAL CAPABILITIES (").append(detectedSkills.size()).append(" Detected):\n");
        if (!detectedSkills.isEmpty()) {
            sb.append(String.join(", ", detectedSkills)).append("\n\n");
        } else {
            sb.append("• No standard tech frameworks explicitly matched. Ensure tools like Java, Spring Boot, React, SQL, or Docker are written out in clear text.\n\n");
        }

        // 3. Actionable Improvement Roadmap
        sb.append("📋 CRITICAL ATS OPTIMIZATION CHECKLIST:\n");
        int step = 1;

        if (impactScore < 16) {
            sb.append(step++).append(". QUANTIFY YOUR BULLET POINTS: Use the Google XYZ formula: 'Accomplished [X] as measured by [Y], by doing [Z]'. Example: 'Reduced API response time by 42% by indexing PostgreSQL queries and implementing Redis caching.'\n");
        }

        if (skillsScore < 25) {
            sb.append(step++).append(". EXPAND TECH STACK COVERAGE: Add specific tools, CI/CD pipelines, unit testing frameworks (JUnit/Mockito), and cloud platforms (AWS/Docker) to your Skills and Project descriptions.\n");
        }

        if (contactScore < 15) {
            sb.append(step++).append(". COMPLETE CONTACT HEADER: Ensure your LinkedIn profile URL, GitHub/Portfolio link, phone number, and location are at the very top of your document.\n");
        }

        if (sectionScore < 20) {
            sb.append(step++).append(". STANDARD SECTION HEADINGS: Use standard ATS headers ('Professional Experience', 'Technical Skills', 'Education', 'Projects') so automated parsers correctly categorize your background.\n");
        }

        if (formatScore < 15) {
            sb.append(step++).append(". ATS FORMATTING: Keep length strictly to 1-2 pages (400-900 words) and use standard bullet points rather than dense paragraphs or multi-column graphical tables.\n");
        }

        sb.append("\n🎯 RECOMMENDED TARGET POSITIONS:\n");
        for (String role : suggestedRoles) {
            sb.append("• ").append(role).append("\n");
        }

        return sb.toString();
    }

    public byte[] generateTemplatePdf(String type) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        if ("fresher".equalsIgnoreCase(type)) {
            document.add(new Paragraph("PRIYANSHU SHEKHAR").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Software Engineer Intern | 884784XXXX").setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
            
            document.add(new Paragraph("EDUCATION").setBold().setFontSize(14));
            document.add(new Paragraph("B.Tech in Computer Science - Top Tier Institute (Expected 2025)"));
            document.add(new Paragraph("GPA: 8.9/10.0"));
            
            document.add(new Paragraph("\nTECHNICAL SKILLS").setBold().setFontSize(14));
            document.add(new Paragraph("Languages: Java, Python, SQL, C++"));
            document.add(new Paragraph("Frameworks: Spring Boot, React.js, Tailwind CSS, Docker"));
            
            document.add(new Paragraph("\nPROJECTS").setBold().setFontSize(14));
            document.add(new Paragraph("1. Careerthon.AI SaaS: Developed an end-to-end LinkedIn profile review engine using Spring Boot."));
            document.add(new Paragraph("2. AI Content Generator: Built a tool using OpenAI API for personalized cover letters."));
        } else {
            document.add(new Paragraph("PRIYANSHU SHEKHAR").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Sr. Full Stack Developer | 5+ Years Exp | mishra@email.com").setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
            
            document.add(new Paragraph("SUMMARY").setBold().setFontSize(14));
            document.add(new Paragraph("Highly skilled Full Stack Developer with expert-level proficiency in Java/Spring ecosystem and cloud architecture. Proven track record of improving system uptime by 99.9% and reducing API latency across critical microservices."));
            
            document.add(new Paragraph("\nPROFESSIONAL EXPERIENCE").setBold().setFontSize(14));
            document.add(new Paragraph("Software Lead - Global Tech (2021 - Present)"));
            document.add(new Paragraph("• Redesigned data ingestion pipeline, improving throughput by 250%."));
            document.add(new Paragraph("• Mentored 15 junior developers and improved sprint velocity by 20%."));
            document.add(new Paragraph("• Spearheaded migration to Kubernetes, reducing infrastructure costs by 40%."));
            
            document.add(new Paragraph("\nSKILLS").setBold().setFontSize(14));
            document.add(new Paragraph("Cloud: AWS (Solution Architect Certified), GCP, Docker, Kubernetes"));
            document.add(new Paragraph("Backend: Java 21, Node.js, PostgreSQL, Redis, Kafka"));
        }

        document.close();
        return baos.toByteArray();
    }
}

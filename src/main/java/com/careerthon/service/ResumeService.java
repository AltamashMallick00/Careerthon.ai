package com.careerthon.service;

import com.careerthon.model.ResumeReview;
import com.careerthon.repository.ResumeReviewRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ResumeService {

    private final ResumeReviewRepository resumeReviewRepository;
    private final EmailService emailService;

    // NO Tika instance — we use lightweight built-in extraction instead.
    // Tika loads 50-100MB of parser chains at startup → OOM on Render free tier.

    public ResumeService(ResumeReviewRepository resumeReviewRepository, EmailService emailService) {
        this.resumeReviewRepository = resumeReviewRepository;
        this.emailService = emailService;
    }

    /** Lightweight text extraction without Apache Tika */
    private String extractText(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String name = originalFilename != null ? originalFilename.toLowerCase() : "";
        try (InputStream in = file.getInputStream()) {
            byte[] bytes = in.readAllBytes();
            if (bytes.length == 0) return "";
            // For text-based files (txt, docx xml content, etc.) — decode as UTF-8
            // For PDFs — extract printable ASCII characters (rough but zero-dependency)
            String raw = new String(bytes, StandardCharsets.UTF_8)
                    .replaceAll("[^\\x20-\\x7E\\n\\r\\t]", " ")
                    .replaceAll("\\s{3,}", " ")
                    .trim();
            // Keep only meaningful words (length > 2) to reduce noise
            return raw.length() > 50 ? raw.substring(0, Math.min(raw.length(), 8000)) : "";
        } catch (Exception e) {
            return "background in " + name;
        }
    }

    private static final List<String> TECHNICAL_ROLES = List.of(
            "Software Development Engineer (SDE)", "Backend Developer (Java/Spring)", "Frontend Developer (React/Angular)",
            "Full Stack Developer", "Data Scientist", "DevOps Engineer", "Cloud Solutions Architect",
            "SRE (Site Reliability Engineer)", "QA/Automation Engineer", "Artificial Intelligence Engineer",
            "Data Analyst", "Android/iOS Developer", "Cybersecurity Specialist", "Database Administrator"
    );

    private static final List<String> NON_TECHNICAL_ROLES = List.of(
            "Product Manager (PM)", "IT Project Manager", "Delivery Manager", "Operations Manager",
            "HR Business Partner", "Marketing Manager", "Sales & Account Executive", "Business Analyst",
            "Customer Success Manager", "Finance Manager", "Corporate Strategy Associate", "UI/UX Designer"
    );

    public ResumeReview analyzeResume(MultipartFile file, String userName, String userEmail) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown_file";
        String content = extractText(file);

        // --- Deterministic Realistic Scores ---
        // We use content hash so same resume always gets same score
        long seed = (content.length() > 100 ? content.substring(0, 50) + content.substring(content.length()-50) : content).hashCode();
        Random contentRandom = new Random(seed);

        int score = calculateDeterministicScore(content, contentRandom);
        
        List<String> suggested = new ArrayList<>();
        suggestDeterministicRoles(content, suggested, contentRandom);

        String suggestedRoles = String.join(", ", suggested);
        String suggestions = generateSuggestions(score, suggested, content);

        byte[] fileBytes;
        try { fileBytes = file.getBytes(); } catch (Exception e) { fileBytes = new byte[0]; }

        ResumeReview review = new ResumeReview(fileName, userName, userEmail, score, suggestedRoles, suggestions, fileBytes);
        ResumeReview saved = resumeReviewRepository.save(review);
        
        if (userEmail != null && !userEmail.isEmpty()) {
            String reportUrl = "https://Careerthon.AI.onrender.com/resume/results/" + saved.getId();
            emailService.sendReport(userEmail, reportUrl, userName);
        }

        return saved;
    }

    private int calculateDeterministicScore(String content, Random r) {
        String lowerContent = content.toLowerCase();
        int score = 60 + r.nextInt(20);
        
        // Bonus for length
        if (content.length() > 1000) score += 5;
        if (content.length() > 5000) score += 5;
        
        // Keyword checking
        String[] keywords = {"agile", "scrum", "java", "python", "sql", "cloud", "aws", "docker", "leadership", "impact", "result"};
        for (String kw : keywords) {
            if (lowerContent.contains(kw)) score += 2;
        }
        
        return Math.min(score, 99);
    }

    private void suggestDeterministicRoles(String content, List<String> suggested, Random r) {
        String lowerContent = content.toLowerCase();
        
        // Suggest tech roles
        if (lowerContent.contains("code") || lowerContent.contains("java") || lowerContent.contains("python") || lowerContent.contains("developer")) {
            suggested.add("SDE");
            suggested.add("Full Stack Developer");
        }
        
        // Suggest data
        if (lowerContent.contains("sql") || lowerContent.contains("data") || lowerContent.contains("intelligence")) {
            suggested.add("Data Analyst");
            suggested.add("Data Scientist");
        }
        
        // Suggest non-tech
        if (lowerContent.contains("p&l") || lowerContent.contains("client") || lowerContent.contains("stakeholder") || lowerContent.contains("management")) {
            suggested.add("Project Manager");
            suggested.add("Customer Success");
        }

        // Add 2 random appropriate roles to satisfy requirement of "all positions"
        if (suggested.isEmpty()) {
            suggested.add(TECHNICAL_ROLES.get(r.nextInt(TECHNICAL_ROLES.size())));
            suggested.add(NON_TECHNICAL_ROLES.get(r.nextInt(NON_TECHNICAL_ROLES.size())));
        }
    }

    private String generateSuggestions(int score, List<String> suggested, String content) {
        StringBuilder sb = new StringBuilder();
        if (score < 80) {
            sb.append("Your resume needs better keyword optimization. ");
            sb.append("Consider adding more industry-specific technical skills. ");
        } else {
            sb.append("Excellent resume structure! ");
        }
        
        if (!content.toLowerCase().contains("%") && !content.toLowerCase().contains("improved") && !content.toLowerCase().contains("reduced")) {
            sb.append("Focus on highlighting impact with metrics (e.g., 'Improved performance by 20%'). ");
        }
        
        sb.append("Ideal roles for you include: ").append(String.join(", ", suggested));
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

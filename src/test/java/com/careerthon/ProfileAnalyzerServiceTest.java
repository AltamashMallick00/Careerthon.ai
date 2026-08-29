package com.careerthon;

import com.careerthon.model.ProfileReview;
import com.careerthon.model.ScoreBreakdown;
import com.careerthon.repository.ProfileReviewRepository;
import com.careerthon.service.ProfileAnalyzerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProfileAnalyzerServiceTest {

    private ProfileReviewRepository reviewRepository;
    private ProfileAnalyzerService analyzerService;
    private Map<Long, ProfileReview> db = new HashMap<>();

    @BeforeEach
    void setUp() {
        db.clear();
        reviewRepository = (ProfileReviewRepository) Proxy.newProxyInstance(
                ProfileReviewRepository.class.getClassLoader(),
                new Class<?>[]{ProfileReviewRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name) && args.length == 1) {
                        return Optional.ofNullable(db.get(args[0]));
                    }
                    if ("save".equals(name) && args.length == 1) {
                        ProfileReview entity = (ProfileReview) args[0];
                        if (entity.getId() == null) {
                            entity.setId((long) (db.size() + 1));
                        }
                        db.put(entity.getId(), entity);
                        return entity;
                    }
                    if ("findAllByOrderByCreatedAtDesc".equals(name)) {
                        return new ArrayList<>(db.values());
                    }
                    return null;
                }
        );

        analyzerService = new ProfileAnalyzerService(reviewRepository, null);
    }

    @Test
    void testRealProfileTextScoring_RichDeveloper() {
        String richProfileText = "Priyanshu Shekhar\n" +
                "Lead Architect & Full Stack Engineer | Java, Spring Boot, React, AWS, Cloud Architecture\n" +
                "Bengaluru, Karnataka, India\n" +
                "Summary: Passionate Software Architect with track record of designing distributed scalable SaaS platforms serving 100k+ users. Experienced in Spring Boot microservices, React.js, Docker, Kubernetes and AWS cloud infrastructure.\n" +
                "Experience: Full Stack Lead at InnovateTech (2022 - Present). Spearheaded microservices migration, improved system latency by 45%, reduced cloud costs by 30%. Mentored 15 engineers.\n" +
                "Education: Bachelor of Technology (B.Tech) in Computer Science & Engineering, Top University (2022 - 2026).\n" +
                "Top Skills: Java, Python, Spring Boot, React, Docker, Kubernetes, AWS, SQL, PostgreSQL, Redis, Microservices, Git, CI/CD, REST API, Agile, Linux.\n" +
                "Certifications: AWS Certified Solutions Architect, Oracle Java SE Certified Professional.\n";

        ScoreBreakdown breakdown = analyzerService.evaluateRealProfileText(richProfileText, "https://linkedin.com/in/priyanshu-shekhar");
        assertNotNull(breakdown);
        assertEquals(10, breakdown.getSkills(), "Skills with 12+ tech keywords should score 10");
        assertTrue(breakdown.getExperience() >= 9, "Experience with metrics should score >= 9");
        assertEquals(9, breakdown.getEducation(), "B.Tech should score 9");
        assertEquals(9, breakdown.getLicensesAndCertifications(), "AWS Cert should score 9");
    }

    @Test
    void testRealProfileTextScoring_StarterProfile() {
        String starterProfileText = "Md Afroz Hassan\n" +
                "Student at University\n" +
                "India\n" +
                "Experience: None yet\n" +
                "Top Skills: HTML\n";

        ScoreBreakdown breakdown = analyzerService.evaluateRealProfileText(starterProfileText, "https://linkedin.com/in/md-afroz-hassan-3ab131297");
        assertNotNull(breakdown);
        assertTrue(breakdown.getSkills() <= 6, "Starter skills should score <= 6");
        assertTrue(breakdown.getExperience() <= 6, "Starter experience should score <= 6");
        assertTrue(breakdown.getLicensesAndCertifications() <= 5, "Missing certifications should score <= 5");
    }

    @Test
    void testUniversalArchitectAndSeniorProfileScoring() {
        ProfileReview review = analyzerService.createReview("https://www.linkedin.com/in/alex-cloud-architect/", null, "alex@test.com");
        ProfileReview result = analyzerService.analyzeProfile(review.getId());

        assertNotNull(result);
        assertEquals("Alex Cloud Architect", result.getUserName());
        assertTrue(result.getOverallScore() >= 88, "Architect score should be >= 88, was: " + result.getOverallScore());
        assertEquals("Excellent", result.getScoreLabel());
        assertTrue(result.getSuggestedRoles().contains("Senior Full Stack Engineer") || result.getSuggestedRoles().contains("Cloud Solutions Architect"));
    }

    @Test
    void testUniversalBeginnerUncustomizedProfileScoring() {
        ProfileReview review = analyzerService.createReview("https://www.linkedin.com/in/md-afroz-hassan-3ab131297/", null, "afroz@test.com");
        ProfileReview result = analyzerService.analyzeProfile(review.getId());

        assertNotNull(result);
        assertEquals("Md Afroz Hassan", result.getUserName());
        assertEquals("Emerging Professional", result.getUserTitle());
        assertTrue(result.getOverallScore() >= 45 && result.getOverallScore() <= 55, "Uncustomized new profile score should be 45-55, was: " + result.getOverallScore());
        assertTrue(result.getActionableInsights().contains("Customize your LinkedIn URL"));
    }
}

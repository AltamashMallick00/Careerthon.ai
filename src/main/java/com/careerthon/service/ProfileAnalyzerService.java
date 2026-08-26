package com.careerthon.service;

import com.careerthon.model.ProfileReview;
import com.careerthon.model.ScoreBreakdown;
import com.careerthon.repository.ProfileReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@SuppressWarnings("null")
public class ProfileAnalyzerService {

    private final ProfileReviewRepository reviewRepository;
    private final EmailService emailService;

    public ProfileAnalyzerService(ProfileReviewRepository reviewRepository, EmailService emailService) {
        this.reviewRepository = reviewRepository;
        this.emailService = emailService;
    }

    public ProfileReview createReview(String linkedinUrl, String email) {
        // Normalize URL for consistency
        String normalizedUrl = linkedinUrl.trim().toLowerCase().replaceAll("/$", "");
        
        // Check if an analysis for this URL already exists
        List<ProfileReview> existing = reviewRepository.findByLinkedinUrlOrderByCreatedAtDesc(normalizedUrl);
        for (ProfileReview r : existing) {
            if (r.getStatus() == ProfileReview.ReviewStatus.COMPLETED) {
                // If the user wants a revisit, we return the existing completed one
                return r;
            }
        }

        ProfileReview review = new ProfileReview();
        review.setLinkedinUrl(normalizedUrl);
        review.setEmailAddress(email);
        review.setStatus(ProfileReview.ReviewStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());

        // Extract username from URL
        String username = extractUsername(normalizedUrl);
        review.setUserName(formatUsername(username));
        review.setUserTitle("Professional");

        return reviewRepository.save(review);
    }

    public ProfileReview analyzeProfile(Long reviewId) {
        Optional<ProfileReview> optReview = reviewRepository.findById(reviewId);
        if (optReview.isEmpty()) return null;

        ProfileReview review = optReview.get();
        
        // If already completed, don't re-analyze (returns same data)
        if (review.getStatus() == ProfileReview.ReviewStatus.COMPLETED) {
            return review;
        }

        review.setStatus(ProfileReview.ReviewStatus.ANALYZING);
        reviewRepository.save(review);

        // --- Deterministic Realistic Scores ---
        // We use the URL as a seed so the same profile ALWAYS gets the same score
        long seed = review.getLinkedinUrl().hashCode();
        Random urlSafeRandom = new Random(seed);
        
        ScoreBreakdown breakdown = generateSeededScoreBreakdown(urlSafeRandom);
        review.setScoreBreakdown(breakdown);

        // Calculate weighted overall score
        int overall = calculateOverallScore(breakdown);
        review.setOverallScore(overall);

        // Generate recommendations for each section
        review.setHeadlineRecommendation(generateHeadlineRecommendation(breakdown.getHeadline()));
        review.setAboutRecommendation(generateAboutRecommendation(breakdown.getAboutSection()));
        review.setSkillsRecommendation(generateSkillsRecommendation(breakdown.getSkills()));
        review.setExperienceRecommendation(generateExperienceRecommendation(breakdown.getExperience()));
        review.setVisibilityRecommendation(generateVisibilityRecommendation(breakdown.getVisibilityScore()));
        review.setAtsRecommendation(generateAtsRecommendation(breakdown.getAtsScore()));
        review.setKeywordRecommendation(generateKeywordRecommendation(breakdown.getKeywordDensity()));
        review.setRecruiterRecommendation(generateRecruiterRecommendation(breakdown.getRecruiterMatch()));
        review.setIndustryBenchmark(generateIndustryBenchmark(overall));
        review.setActionableInsights(generateActionableInsights(breakdown));
        review.setSuggestedRoles(generateSuggestedRoles(overall, review.getUserTitle()));

        review.setStatus(ProfileReview.ReviewStatus.COMPLETED);
        review.setCompletedAt(LocalDateTime.now());
        ProfileReview saved = reviewRepository.save(review);
        
        // Trigger actual email if address is provided
        if (saved.getEmailAddress() != null && !saved.getEmailAddress().isEmpty()) {
            String reportUrl = "https://Careerthon.AI.onrender.com/report/" + saved.getId();
            emailService.sendReport(saved.getEmailAddress(), reportUrl, saved.getUserName());
            saved.setEmailSent(true);
            reviewRepository.save(saved);
        }

        return saved;
    }

    public Optional<ProfileReview> getReview(Long id) {
        return reviewRepository.findById(id);
    }

    public List<ProfileReview> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    private String extractUsername(String url) {
        if (url == null || url.isEmpty()) return "user";
        url = url.trim().replaceAll("/$", "");
        String[] parts = url.split("/");
        String last = parts[parts.length - 1];
        if (last.contains("?")) last = last.substring(0, last.indexOf("?"));
        return last.isEmpty() ? "user" : last;
    }

    private String formatUsername(String username) {
        if (username == null || username.isEmpty()) return "LinkedIn User";
        return username.substring(0, 1).toUpperCase() +
               username.substring(1).replace("-", " ").replace("_", " ");
    }

    private ScoreBreakdown generateSeededScoreBreakdown(Random seededRandom) {
        return new ScoreBreakdown(
            randomSeededScore(seededRandom, 6, 10),  // profilePhoto
            randomSeededScore(seededRandom, 5, 9),   // coverPhoto
            randomSeededScore(seededRandom, 5, 10),  // headline
            randomSeededScore(seededRandom, 4, 9),   // aboutSection
            randomSeededScore(seededRandom, 5, 10),  // experience
            randomSeededScore(seededRandom, 6, 10),  // education
            randomSeededScore(seededRandom, 4, 9),   // skills
            randomSeededScore(seededRandom, 5, 9),   // atsScore
            randomSeededScore(seededRandom, 4, 8),   // keywordDensity
            randomSeededScore(seededRandom, 4, 9),   // visibilityScore
            randomSeededScore(seededRandom, 5, 9),   // recruiterMatch
            randomSeededScore(seededRandom, 5, 9),   // industryBenchmark
            randomSeededScore(seededRandom, 3, 8),   // licensesAndCertifications
            randomSeededScore(seededRandom, 3, 8),   // recommendations
            randomSeededScore(seededRandom, 3, 8)    // activityEngagement
        );
    }

    private int randomSeededScore(Random r, int min, int max) {
        return min + r.nextInt(max - min + 1);
    }

    private int calculateOverallScore(ScoreBreakdown b) {
        double weighted = (
            b.getProfilePhoto() * 0.05 +
            b.getCoverPhoto() * 0.04 +
            b.getHeadline() * 0.12 +
            b.getAboutSection() * 0.12 +
            b.getExperience() * 0.15 +
            b.getEducation() * 0.06 +
            b.getSkills() * 0.10 +
            b.getAtsScore() * 0.10 +
            b.getKeywordDensity() * 0.06 +
            b.getVisibilityScore() * 0.06 +
            b.getRecruiterMatch() * 0.05 +
            b.getIndustryBenchmark() * 0.03 +
            b.getLicensesAndCertifications() * 0.02 +
            b.getRecommendations() * 0.02 +
            b.getActivityEngagement() * 0.02
        );
        return (int) Math.round(weighted * 10);
    }

    private String generateHeadlineRecommendation(int score) {
        if (score >= 8) {
            return "Your headline effectively communicates your professional identity. It includes relevant keywords and a clear value proposition. To further enhance it, consider adding a measurable achievement or a unique differentiator that sets you apart from others in your field.";
        } else if (score >= 5) {
            return "Your headline has room for improvement. Consider restructuring it to follow this formula: [Title] | [Key Skill] | [Value Proposition]. For example: 'Senior Software Engineer | Cloud Architecture Expert | Helping enterprises scale with 99.9% uptime'. Include industry-specific keywords that recruiters search for.";
        } else {
            return "Your headline needs significant improvement. Currently, it may be using a default job title which doesn't help you stand out. Create a compelling headline that includes: your current role, 2-3 key skills, and the value you bring. Avoid generic phrases and focus on specific, measurable achievements.";
        }
    }

    private String generateAboutRecommendation(int score) {
        if (score >= 8) {
            return "Your About section tells a compelling professional story. It effectively highlights your expertise and career trajectory. To make it even stronger, consider adding specific metrics (e.g., 'managed $2M budget' or 'led team of 15 engineers') and a clear call-to-action for visitors.";
        } else if (score >= 5) {
            return "Your About section could be more impactful. Structure it in 3-4 paragraphs: (1) An engaging hook about your professional passion, (2) Your key achievements with metrics, (3) Your current focus and expertise areas, (4) A call-to-action. Use first person and make it conversational yet professional.";
        } else {
            return "Your About section needs a complete rewrite. It's either too short, missing, or doesn't effectively communicate your value. Write 3-5 paragraphs covering: who you are professionally, your top 3 achievements with numbers, your key skills, and how people can reach you. Use keywords from your target job descriptions.";
        }
    }

    private String generateSkillsRecommendation(int score) {
        if (score >= 8) {
            return "Your skills section is well-optimized with relevant, in-demand skills. Continue to seek endorsements from colleagues, especially for your top 3 skills. Consider removing outdated skills and adding emerging technologies or methodologies relevant to your industry.";
        } else if (score >= 5) {
            return "Your skills section needs optimization. Add at least 20-30 relevant skills, prioritizing industry-specific and in-demand skills at the top. Request endorsements from colleagues for your top skills. Remove generic skills like 'Microsoft Office' and replace with specific tools and technologies you excel at.";
        } else {
            return "Your skills section is severely under-optimized. LinkedIn allows up to 50 skills — aim for at least 30. Research job descriptions in your target role and add all matching skills. Pin your top 3 most important skills. Actively request endorsements by endorsing colleagues' skills first.";
        }
    }

    private String generateExperienceRecommendation(int score) {
        if (score >= 8) {
            return "Your experience section effectively showcases your career progression with detailed descriptions and achievements. Enhance it further by adding media (presentations, articles, project demos) and ensure each role has 3-5 bullet points with quantifiable results using the STAR method.";
        } else if (score >= 5) {
            return "Your experience descriptions need more impact. For each role, include: (1) Scope of responsibility, (2) Key achievements with metrics (%, $, #), (3) Technologies/tools used, (4) Leadership or collaboration highlights. Use action verbs like 'Spearheaded', 'Optimized', 'Architected' instead of 'Responsible for'.";
        } else {
            return "Your experience section needs significant improvement. Each role should have at least 3 bullet points describing concrete achievements, not just job duties. Use the formula: 'Action Verb + Task + Result with Metric'. Example: 'Reduced page load time by 40% through implementing lazy loading and CDN optimization, serving 2M+ daily users.'";
        }
    }

    private String generateVisibilityRecommendation(int score) {
        if (score >= 8) {
            return "Your profile has strong visibility. You're appearing in relevant search results. To maintain this, continue posting content regularly (2-3 times per week), engage with industry posts, and join relevant LinkedIn groups. Consider publishing long-form articles to establish thought leadership.";
        } else if (score >= 5) {
            return "Your visibility can be improved significantly. Start by: (1) Customizing your profile URL, (2) Enabling 'Open to Work' or 'Providing Services' badges, (3) Publishing at least one post per week, (4) Commenting thoughtfully on 5+ industry posts daily, (5) Adding relevant hashtags to your posts.";
        } else {
            return "Your profile has very low visibility. Immediate actions needed: (1) Set your profile to 'Public', (2) Customize your URL to linkedin.com/in/yourname, (3) Complete all profile sections (LinkedIn favors 'All-Star' profiles), (4) Start engaging daily — like, comment, and share content, (5) Connect with 50+ relevant professionals.";
        }
    }

    private String generateAtsRecommendation(int score) {
        if (score >= 8) {
            return "Your profile is well-optimized for Applicant Tracking Systems. It contains relevant keywords naturally integrated throughout your headline, about section, and experience. Continue to update your keywords based on the latest job descriptions in your target roles.";
        } else if (score >= 5) {
            return "Your ATS compatibility needs improvement. Research 5-10 job descriptions for your target role and identify recurring keywords. Naturally incorporate these keywords into your headline, about section, skills, and experience descriptions. Avoid keyword stuffing — focus on natural integration.";
        } else {
            return "Your profile is poorly optimized for ATS systems. Many recruiters use ATS to filter LinkedIn profiles. Immediate fix: (1) Match your job title to industry-standard titles, (2) Add technical skills verbatim from job descriptions, (3) Include certifications and tools by name, (4) Use standard section headers.";
        }
    }

    private String generateKeywordRecommendation(int score) {
        if (score >= 7) {
            return "Your keyword density is good. Your profile contains relevant industry keywords distributed across sections. To further optimize, analyze top-performing profiles in your field and identify any keywords you might be missing. Update your profile quarterly with trending industry terms.";
        } else if (score >= 5) {
            return "Your keyword strategy needs refinement. Create a list of 20-30 target keywords from job descriptions in your field. Distribute them naturally: 3-5 in your headline, 8-10 in your about section, and the rest across experience and skills. Don't forget to include both abbreviations and full forms (e.g., 'AI' and 'Artificial Intelligence').";
        } else {
            return "Your profile lacks essential keywords. This severely impacts your discoverability. Action plan: (1) List 30 must-have keywords for your role, (2) Add your top 5 to the headline, (3) Weave 10-15 into a rewritten About section, (4) Include all of them across your experience descriptions, (5) Add remaining as skills.";
        }
    }

    private String generateRecruiterRecommendation(int score) {
        if (score >= 8) {
            return "Your profile is highly attractive to recruiters. You have strong engagement signals, a complete profile, and effective use of keywords. Continue building your network with recruiters in your industry and keep your profile updated with your latest achievements and skills.";
        } else if (score >= 5) {
            return "To attract more recruiters: (1) Enable 'Open to Work' with specific job preferences, (2) Connect with 10+ recruiters in your industry, (3) Add a professional headshot (profiles with photos get 21x more views), (4) Get at least 5 recommendations from managers or senior colleagues, (5) List specific location preferences.";
        } else {
            return "Your profile is not optimized for recruiter discovery. Critical fixes: (1) Complete ALL profile sections — incomplete profiles are filtered out, (2) Add a professional headshot immediately, (3) Write a keyword-rich headline with your target job title, (4) Build your network to 500+ connections, (5) Set location and industry correctly.";
        }
    }

    private String generateIndustryBenchmark(int overallScore) {
        if (overallScore >= 80) {
            return "Your profile ranks in the top 15% of professionals in your industry. You're well-positioned to attract recruiter attention and industry opportunities. Key strengths include a strong professional narrative and good keyword optimization.";
        } else if (overallScore >= 60) {
            return "Your profile is slightly above the industry average but falls short of top performers. The average optimized profile in your field scores around 75/100. Focus on strengthening your weakest sections to move into the top tier of professional profiles.";
        } else if (overallScore >= 40) {
            return "Your profile is below the industry average of 65/100. Most competitive candidates in your field have significantly more optimized profiles. Focus on the recommended improvements to increase your score to at least 70/100 for better visibility.";
        } else {
            return "Your profile falls significantly below industry standards. Most professionals in your field have profiles scoring 60+ out of 100. Prioritize the critical improvements in the recommendations above to bring your profile up to competitive standards.";
        }
    }

    private String generateActionableInsights(ScoreBreakdown breakdown) {
        StringBuilder insights = new StringBuilder();
        insights.append("📋 PRIORITY IMPROVEMENT PLAN:\n\n");

        int priority = 1;

        if (breakdown.getHeadline() < 7) {
            insights.append(priority++).append(". 🔴 HIGH PRIORITY — Rewrite your headline with target keywords and value proposition\n");
        }
        if (breakdown.getAboutSection() < 7) {
            insights.append(priority++).append(". 🔴 HIGH PRIORITY — Revamp your About section with achievements and metrics\n");
        }
        if (breakdown.getExperience() < 7) {
            insights.append(priority++).append(". 🔴 HIGH PRIORITY — Add quantifiable achievements to each experience entry\n");
        }
        if (breakdown.getProfilePhoto() < 7) {
            insights.append(priority++).append(". 🟡 MEDIUM PRIORITY — Upgrade to a professional headshot with good lighting\n");
        }
        if (breakdown.getSkills() < 7) {
            insights.append(priority++).append(". 🟡 MEDIUM PRIORITY — Add more industry-relevant skills and seek endorsements\n");
        }
        if (breakdown.getAtsScore() < 7) {
            insights.append(priority++).append(". 🟡 MEDIUM PRIORITY — Optimize for ATS by adding standard job title keywords\n");
        }
        if (breakdown.getCoverPhoto() < 7) {
            insights.append(priority++).append(". 🟢 LOW PRIORITY — Add a custom cover photo reflecting your professional brand\n");
        }
        if (breakdown.getRecommendations() < 7) {
            insights.append(priority++).append(". 🟢 LOW PRIORITY — Request recommendations from managers and senior colleagues\n");
        }
        if (breakdown.getActivityEngagement() < 7) {
            insights.append(priority++).append(". 🟢 LOW PRIORITY — Increase posting frequency and engagement on the platform\n");
        }

        if (priority == 1) {
            insights.append("✅ Your profile is well-optimized! Focus on maintaining regular activity and updating your achievements quarterly.");
        }

        return insights.toString();
    }

    private String generateSuggestedRoles(int overall, String userTitle) {
        if (overall >= 85) {
            return "Senior Product Manager, Solutions Architect, Engineering Lead, Global Operations Director";
        } else if (overall >= 70) {
            return "Sr. Software Engineer, Project Manager, Business Analyst, Marketing Specialist";
        } else if (overall >= 50) {
            return "Junior Developer, HR Generalist, Sales Executive, Customer Success Associate";
        } else {
            return "Internships in Corporate, Junior Analyst Roles, Assistant Project Coordinator";
        }
    }
}

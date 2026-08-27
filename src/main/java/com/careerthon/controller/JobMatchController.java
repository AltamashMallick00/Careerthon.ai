package com.careerthon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class JobMatchController {

    // Comprehensive list of job posting metadata, URLs, fillers, and administrative stop words
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "the", "a", "an", "and", "or", "but", "is", "are", "was", "were", "be", "been", "being",
        "to", "of", "in", "on", "at", "by", "for", "with", "about", "against", "between", "into",
        "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in",
        "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there",
        "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other",
        "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very",
        "s", "t", "can", "will", "just", "don", "should", "now", "d", "ll", "m", "o", "re", "ve", "y",
        "we", "you", "he", "she", "it", "they", "them", "their", "our", "us", "your", "i", "me", "my",
        "have", "has", "had", "do", "does", "did", "would", "could", "should", "this", "that", "these", "those",
        
        // Administrative & Job Posting Noise Words
        "responsibilities", "requirements", "qualifications", "experience", "years", "work", "job", "role", "roles",
        "hiring", "apply", "register", "link", "form", "forms", "batch", "lpa", "ctc", "salary", "stipend", "per",
        "month", "months", "annum", "year", "years", "date", "company", "location", "bangalore-based", "mumbai", "delhi",
        "remote", "hybrid", "workplace", "candidate", "applicant", "opportunity", "openings", "opening", "eligibility",
        "criteria", "btech", "mtech", "degree", "diploma", "bachelor", "bachelors", "master", "masters", "discipline",
        "preferred", "required", "overview", "category", "full-time", "part-time", "internship", "period", "probation",
        "pursuing", "final", "pre-final", "please", "refrain", "selected", "already", "following", "given", "must",
        "able", "using", "used", "based", "etc", "plus", "high", "good", "strong", "well", "key", "main", "overview",
        "description", "about", "services", "products", "team", "teams", "organization", "committed", "fostering",
        "delivering", "working", "ensure", "stay", "create", "work", "day", "days", "time", "schedule", "details",
        "info", "information", "notice", "joining", "immediate", "batch", "2024", "2025", "2026", "2027", "llp",
        "inc", "pvt", "ltd", "google", "whatsapp", "telegram", "phone", "email", "contact", "http", "https", "www",
        "com", "gle", "forms.gle", "url", "persevex", "edtech", "functions", "function", "mass", "recruiters", "recruiter",
        "opening", "overview", "stipend", "incentives", "incentive", "compensation", "learning", "exposure", "curriculum",
        "educators", "institutions", "solutions", "vision", "mission", "academic", "real-world"
    ));

    private static final Set<String> TECH_KEYWORDS = new HashSet<>(Arrays.asList(
        "java", "spring", "boot", "python", "javascript", "typescript", "react", "angular", "vue", "node",
        "express", "html", "css", "tailwind", "bootstrap", "sql", "nosql", "mysql", "postgresql", "mongodb",
        "redis", "oracle", "docker", "kubernetes", "aws", "azure", "gcp", "git", "github", "gitlab",
        "ci/cd", "jenkins", "rest", "api", "graphql", "microservices", "kafka", "rabbitmq", "maven", "gradle",
        "testing", "junit", "mockito", "selenium", "cypress", "jest", "devops", "linux", "bash", "architecture",
        "system design", "data structures", "algorithms", "ai", "ml", "tensorflow", "pytorch", "nlp", "llm"
    ));

    // High-value multi-word skill phrases to recognize intact
    private static final List<String> SKILL_PHRASES = Arrays.asList(
        "business development", "digital marketing", "lead conversion", "direct outreach",
        "strategic partnerships", "stakeholder negotiation", "client nurturing", "corporate clients",
        "critical thinking", "problem solving", "presentation skills", "interpersonal skills",
        "negotiation skills", "cross-functional leadership", "market research", "customer success",
        "project management", "data analytics", "cloud architecture", "system design",
        "restful apis", "ci/cd pipeline", "microservices architecture", "edtech sales",
        "content operations", "lead generation", "conversion strategies", "relationship management"
    );

    @GetMapping("/job-match")
    public String showJobMatchPage() {
        return "job_match";
    }

    @PostMapping("/api/job-match")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> performJobMatch(
            @RequestParam("jobDescription") String jobDescription,
            @RequestParam("resumeText") String resumeText) {

        Map<String, Object> response = new HashMap<>();

        if (jobDescription == null || jobDescription.trim().isEmpty() ||
            resumeText == null || resumeText.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Both Job Description and Resume text are required.");
            return ResponseEntity.badRequest().body(response);
        }

        // Clean and extract high-value career skills & phrases
        Set<String> targetKeywords = extractSmartCareerKeywords(jobDescription);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        List<String> matchedTech = new ArrayList<>();
        List<String> missingTech = new ArrayList<>();
        List<String> matchedSoft = new ArrayList<>();
        List<String> missingSoft = new ArrayList<>();

        for (String keyword : targetKeywords) {
            boolean isTech = isTechnicalKeyword(keyword);
            if (containsWordIgnoreCase(resumeText, keyword)) {
                matched.add(keyword);
                if (isTech) matchedTech.add(keyword);
                else matchedSoft.add(keyword);
            } else {
                missing.add(keyword);
                if (isTech) missingTech.add(keyword);
                else missingSoft.add(keyword);
            }
        }

        int totalCount = targetKeywords.size();
        int matchPct = totalCount == 0 ? 100 : (int) Math.round(((double) matched.size() / totalCount) * 100);

        // Sub-metric calculations
        int techTotal = matchedTech.size() + missingTech.size();
        int techMatchPct = techTotal == 0 ? 100 : (int) Math.round(((double) matchedTech.size() / techTotal) * 100);

        int expMatchPct = Math.min(100, Math.max(30, matchPct + 12));
        int densityPct = Math.min(100, Math.max(25, (int) (matchPct * 0.9 + (matchedTech.size() * 3))));
        int atsReadiness = Math.min(100, Math.max(40, (matchPct + techMatchPct) / 2));

        Map<String, Integer> subMetrics = new HashMap<>();
        subMetrics.put("technicalOverlap", techMatchPct);
        subMetrics.put("experienceAlignment", expMatchPct);
        subMetrics.put("keywordDensity", densityPct);
        subMetrics.put("atsReadiness", atsReadiness);

        // Highlight missing keywords in red within the original job description HTML-safe output
        String highlightedHtml = highlightMissingKeywords(jobDescription, missing);

        response.put("success", true);
        response.put("matchPercentage", matchPct);
        response.put("matchedKeywords", matched);
        response.put("missingKeywords", missing);
        response.put("matchedTechSkills", matchedTech);
        response.put("missingTechSkills", missingTech);
        response.put("matchedSoftSkills", matchedSoft);
        response.put("missingSoftSkills", missingSoft);
        response.put("subMetrics", subMetrics);
        response.put("highlightedJobDescription", highlightedHtml);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/job-match/generate-bullets")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateAiBullets(
            @RequestBody Map<String, Object> payload) {

        Map<String, Object> response = new HashMap<>();
        List<String> missingKeywords = (List<String>) payload.getOrDefault("missingKeywords", Collections.emptyList());
        String targetRole = (String) payload.getOrDefault("role", "Software Engineer");

        if (missingKeywords.isEmpty()) {
            response.put("success", true);
            response.put("bullets", Arrays.asList(
                "• Optimized core application modules resulting in a 35% boost in system performance.",
                "• Spearheaded cross-functional team initiatives to deliver high-quality production features.",
                "• Implemented automated CI/CD pipelines reducing deployment downtime by 50%."
            ));
            return ResponseEntity.ok(response);
        }

        List<String> keywordsToUse = missingKeywords.stream().limit(6).collect(Collectors.toList());
        String k1 = keywordsToUse.size() > 0 ? keywordsToUse.get(0) : "strategic outreach";
        String k2 = keywordsToUse.size() > 1 ? keywordsToUse.get(1) : "client conversion";
        String k3 = keywordsToUse.size() > 2 ? keywordsToUse.get(2) : "stakeholder negotiation";
        String k4 = keywordsToUse.size() > 3 ? keywordsToUse.get(3) : "pipeline optimization";

        List<String> bulletPoints = Arrays.asList(
            "• Spearheaded integration of " + capitalize(k1) + " and " + capitalize(k2) + " into key workflows, driving revenue growth.",
            "• Optimized execution of " + capitalize(k3) + " to expand strategic client partnerships and account pipelines.",
            "• Leveraged " + capitalize(k4) + " techniques to accelerate engagement performance and team efficiency."
        );

        response.put("success", true);
        response.put("bullets", bulletPoints);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/job-match/presets")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getPresetJobs() {
        Map<String, String> presets = new LinkedHashMap<>();
        presets.put("Full-Stack Developer", 
            "We are seeking a Senior Full-Stack Developer proficient in Java, Spring Boot, React, and PostgreSQL. " +
            "Responsibilities include designing RESTful APIs, containerizing microservices with Docker and Kubernetes, " +
            "and setting up CI/CD pipelines on AWS. Experience with Agile methodology, Git, and automated testing (JUnit, Jest) is required.");
        
        presets.put("AI / ML Engineer", 
            "Looking for an AI / Machine Learning Engineer skilled in Python, TensorFlow, PyTorch, and NLP. " +
            "You will build and deploy fine-tuned LLM models, design data pipelines with SQL and Docker, " +
            "and optimize real-time inference latency on AWS cloud infrastructure.");
        
        presets.put("Product Manager", 
            "Seeking a Technical Product Manager to drive product roadmap, stakeholder management, and agile user stories. " +
            "Must have experience with data analytics, cross-functional leadership, customer feedback loops, and API integrations.");
        
        return ResponseEntity.ok(presets);
    }

    private Set<String> extractSmartCareerKeywords(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) return Collections.emptySet();

        // 1. Strip URLs, links, email addresses, and garbage fragments
        String cleanedText = rawText
                .replaceAll("(?i)https?://\\S+", " ")
                .replaceAll("(?i)www\\.\\S+", " ")
                .replaceAll("(?i)forms\\.gle\\S+", " ")
                .replaceAll("(?i)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", " ");

        Set<String> extractedSkills = new LinkedHashSet<>();
        String lowerText = cleanedText.toLowerCase();

        // 2. Extract multi-word skill phrases first
        for (String phrase : SKILL_PHRASES) {
            if (lowerText.contains(phrase)) {
                extractedSkills.add(capitalizePhrase(phrase));
            }
        }

        // 3. Extract single high-value skill terms
        Pattern pattern = Pattern.compile("[a-zA-Z+#-]+");
        Matcher matcher = pattern.matcher(cleanedText);

        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            
            // Skip short words unless recognized as known tech (e.g. sql, aws, git, api, ai, ml)
            boolean isKnownShortTech = (word.length() <= 3) && TECH_KEYWORDS.contains(word);
            boolean isValidLength = word.length() >= 4 || isKnownShortTech;

            if (isValidLength && !STOP_WORDS.contains(word) && !isNumeric(word)) {
                // If it's already part of an extracted multi-word phrase, avoid duplicating raw single word
                boolean alreadyCoveredInPhrase = false;
                for (String phrase : SKILL_PHRASES) {
                    if (extractedSkills.contains(capitalizePhrase(phrase)) && phrase.contains(word)) {
                        alreadyCoveredInPhrase = true;
                        break;
                    }
                }
                if (!alreadyCoveredInPhrase) {
                    extractedSkills.add(capitalizePhrase(word));
                }
            }
        }

        return extractedSkills;
    }

    private boolean isTechnicalKeyword(String keyword) {
        String lower = keyword.toLowerCase();
        if (TECH_KEYWORDS.contains(lower)) return true;
        for (String tech : TECH_KEYWORDS) {
            if (lower.contains(tech)) return true;
        }
        return false;
    }

    private String capitalizePhrase(String phrase) {
        if (phrase == null || phrase.isEmpty()) return phrase;
        String[] words = phrase.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(capitalize(w));
        }
        return sb.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private boolean containsWordIgnoreCase(String source, String word) {
        if (source == null || word == null) return false;
        String sourceLower = source.toLowerCase();
        String wordLower = word.toLowerCase();

        Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(wordLower) + "\\b");
        return wordPattern.matcher(sourceLower).find() || sourceLower.contains(wordLower);
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private String highlightMissingKeywords(String originalText, List<String> missingKeywords) {
        if (originalText == null) return "";
        
        String safeText = originalText
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");

        if (missingKeywords.isEmpty()) {
            return safeText;
        }

        List<String> sortedMissing = missingKeywords.stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .collect(Collectors.toList());

        String result = safeText;
        for (String keyword : sortedMissing) {
            String regex = "(?i)\\b(" + Pattern.quote(keyword) + ")\\b";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(result);
            
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String matchedText = m.group(1);
                m.appendReplacement(sb, "<span class=\"text-rose-500 font-bold bg-rose-500/10 px-1 rounded border border-rose-500/20\">" + matchedText + "</span>");
            }
            m.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }
}



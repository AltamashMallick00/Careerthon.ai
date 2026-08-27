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
        "responsibilities", "requirements", "qualifications", "experience", "years", "work", "job", "role"
    ));

    private static final Set<String> TECH_KEYWORDS = new HashSet<>(Arrays.asList(
        "java", "spring", "boot", "python", "javascript", "typescript", "react", "angular", "vue", "node",
        "express", "html", "css", "tailwind", "bootstrap", "sql", "nosql", "mysql", "postgresql", "mongodb",
        "redis", "oracle", "docker", "kubernetes", "aws", "azure", "gcp", "git", "github", "gitlab",
        "ci/cd", "jenkins", "rest", "api", "graphql", "microservices", "kafka", "rabbitmq", "maven", "gradle",
        "testing", "junit", "mockito", "selenium", "cypress", "jest", "devops", "linux", "bash", "architecture",
        "system design", "data structures", "algorithms", "ai", "ml", "tensorflow", "pytorch", "nlp", "llm"
    ));

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

        // Clean and tokenize job description to extract target keywords
        Set<String> targetKeywords = extractKeywords(jobDescription);

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
        String k1 = keywordsToUse.size() > 0 ? keywordsToUse.get(0) : "microservices";
        String k2 = keywordsToUse.size() > 1 ? keywordsToUse.get(1) : "cloud architecture";
        String k3 = keywordsToUse.size() > 2 ? keywordsToUse.get(2) : "performance optimization";
        String k4 = keywordsToUse.size() > 3 ? keywordsToUse.get(3) : "agile workflows";

        List<String> bulletPoints = Arrays.asList(
            "• Spearheaded integration of " + capitalize(k1) + " and " + capitalize(k2) + " into core product workflows, improving system throughput by 40%.",
            "• Architected scalable solution utilizing " + capitalize(k3) + " to eliminate bottleneck issues across high-concurrency user flows.",
            "• Collaborated with cross-functional teams using " + capitalize(k4) + " to accelerate feature delivery cycles by 25%."
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

    private boolean isTechnicalKeyword(String keyword) {
        if (TECH_KEYWORDS.contains(keyword.toLowerCase())) return true;
        for (String tech : TECH_KEYWORDS) {
            if (keyword.toLowerCase().contains(tech)) return true;
        }
        return false;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private Set<String> extractKeywords(String text) {
        Set<String> keywords = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile("[a-zA-Z+#-]+");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            if (word.length() > 2 && !STOP_WORDS.contains(word) && !isNumeric(word)) {
                keywords.add(word);
            }
        }
        return keywords;
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


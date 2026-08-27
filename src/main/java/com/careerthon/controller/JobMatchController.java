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

    private static final Set<String> CRITICAL_CORE_TERMS = new HashSet<>(Arrays.asList(
        "java", "python", "javascript", "react", "aws", "sql", "spring boot", "docker", "kubernetes",
        "business development", "edtech sales", "lead conversion", "direct outreach", "digital marketing",
        "sales", "strategic partnerships", "client nurturing", "stakeholder negotiation"
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

        // Extract Role Title
        String roleTitle = extractRoleTitle(jobDescription);

        // Generate ATS Focus Summary
        String atsFocusSummary = generateAtsFocusSummary(jobDescription, roleTitle);

        // Extract clean smart career keywords
        Set<String> targetKeywords = extractSmartCareerKeywords(jobDescription);

        List<Map<String, String>> criticalKeywords = new ArrayList<>();
        List<Map<String, String>> importantKeywords = new ArrayList<>();
        List<Map<String, String>> niceToHaveKeywords = new ArrayList<>();
        List<String> topAtsKeywords = new ArrayList<>();

        List<Map<String, String>> missingList = new ArrayList<>();
        List<Map<String, String>> safeList = new ArrayList<>();

        List<String> rawMatched = new ArrayList<>();
        List<String> rawMissing = new ArrayList<>();

        int matchedCount = 0;

        for (String kw : targetKeywords) {
            String status = determineStatus(resumeText, kw);
            String whyItMatters = getWhyItMattersReason(kw, jobDescription);
            
            Map<String, String> item = new HashMap<>();
            item.put("keyword", kw);
            item.put("whyItMatters", whyItMatters);
            item.put("status", status);

            int priority = classifyPriority(kw, jobDescription);

            if (priority == 1) { // CRITICAL
                criticalKeywords.add(item);
            } else if (priority == 2) { // IMPORTANT
                importantKeywords.add(item);
            } else { // NICE_TO_HAVE
                niceToHaveKeywords.add(item);
            }

            // Top ATS Keywords (ranked)
            if (topAtsKeywords.size() < 10) {
                topAtsKeywords.add(kw);
            }

            if (status.equals("MATCHED") || status.equals("PARTIAL")) {
                matchedCount++;
                rawMatched.add(kw);
                Map<String, String> safeItem = new HashMap<>();
                safeItem.put("keyword", kw);
                safeItem.put("suggestedLocation", getSuggestedLocation(kw));
                safeItem.put("suggestion", getIntegrationSuggestion(kw));
                safeList.add(safeItem);
            } else {
                rawMissing.add(kw);
                Map<String, String> missingItem = new HashMap<>();
                missingItem.put("keyword", kw);
                missingItem.put("status", "MISSING — VERIFY BEFORE ADDING");
                missingItem.put("reason", whyItMatters);
                missingList.add(missingItem);
            }
        }

        int totalCount = targetKeywords.size();
        int matchPct = totalCount == 0 ? 100 : (int) Math.round(((double) matchedCount / totalCount) * 100);

        String highlightedHtml = highlightMissingKeywords(jobDescription, rawMissing);

        response.put("success", true);
        response.put("roleTitle", roleTitle);
        response.put("atsFocusSummary", atsFocusSummary);
        response.put("matchPercentage", matchPct);
        response.put("criticalKeywords", criticalKeywords);
        response.put("importantKeywords", importantKeywords);
        response.put("niceToHaveKeywords", niceToHaveKeywords);
        response.put("topAtsKeywords", topAtsKeywords);
        response.put("missingKeywordsList", missingList);
        response.put("safeToIntegrateList", safeList);
        response.put("atsWarning", "Adding a keyword without having the underlying skill will not make the candidate genuinely qualified and should not be recommended.");
        response.put("highlightedJobDescription", highlightedHtml);
        response.put("matchedKeywords", rawMatched);
        response.put("missingKeywords", rawMissing);

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
                "• Optimized core application workflows resulting in a 35% boost in system performance.",
                "• Spearheaded cross-functional initiatives to deliver high-quality client solutions.",
                "• Implemented strategic pipeline optimizations reducing client acquisition overhead."
            ));
            return ResponseEntity.ok(response);
        }

        List<String> keywordsToUse = missingKeywords.stream().limit(6).collect(Collectors.toList());
        String k1 = keywordsToUse.size() > 0 ? keywordsToUse.get(0) : "strategic outreach";
        String k2 = keywordsToUse.size() > 1 ? keywordsToUse.get(1) : "client conversion";
        String k3 = keywordsToUse.size() > 2 ? keywordsToUse.get(2) : "stakeholder negotiation";
        String k4 = keywordsToUse.size() > 3 ? keywordsToUse.get(3) : "pipeline optimization";

        List<String> bulletPoints = Arrays.asList(
            "• Spearheaded execution of " + capitalize(k1) + " and " + capitalize(k2) + " to expand account engagement.",
            "• Leveraged " + capitalize(k3) + " to establish high-value strategic partnerships.",
            "• Optimized workflows using " + capitalize(k4) + " methodologies to achieve target key performance metrics."
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

    private String extractRoleTitle(String jdText) {
        if (jdText == null) return "Professional Role";
        String lower = jdText.toLowerCase();
        
        Pattern titlePattern = Pattern.compile("(?i)(?:job profile|role|title|opening overview|position)\\s*[:\\-]?\\s*([^\\n,.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = titlePattern.matcher(jdText);
        if (matcher.find()) {
            String title = matcher.group(1).trim();
            if (title.length() > 3 && title.length() < 60) return title;
        }

        if (lower.contains("full-stack") || lower.contains("fullstack")) return "Full-Stack Developer";
        if (lower.contains("business development") || lower.contains("sales")) return "Business Development / EdTech Sales";
        if (lower.contains("data engineer") || lower.contains("data analyst")) return "Data Specialist";
        if (lower.contains("machine learning") || lower.contains("ai engineer")) return "AI / ML Engineer";
        if (lower.contains("product manager")) return "Product Manager";

        return "Target Professional Role";
    }

    private String generateAtsFocusSummary(String jdText, String roleTitle) {
        String lower = jdText.toLowerCase();
        if (lower.contains("sales") || lower.contains("business development") || lower.contains("outreach")) {
            return "The job description primarily evaluates candidates on lead conversion, direct client outreach, strategic partnership building, and stakeholder negotiation abilities.";
        } else if (lower.contains("java") || lower.contains("python") || lower.contains("developer") || lower.contains("engineer")) {
            return "The job description primarily evaluates core technical stack competency, software design architecture, API development, and automated deployment capabilities.";
        }
        return "The job description emphasizes key technical competencies, domain experience, and core functional responsibilities central to " + roleTitle + ".";
    }

    private String determineStatus(String resumeText, String keyword) {
        if (resumeText == null || keyword == null) return "MISSING — VERIFY BEFORE ADDING";
        String resLower = resumeText.toLowerCase();
        String kwLower = keyword.toLowerCase();

        Pattern exactPattern = Pattern.compile("\\b" + Pattern.quote(kwLower) + "\\b");
        if (exactPattern.matcher(resLower).find()) {
            return "MATCHED";
        }

        // Stem / Partial match
        String[] words = kwLower.split("\\s+");
        for (String w : words) {
            if (w.length() >= 4 && resLower.contains(w)) {
                return "PARTIAL";
            }
        }

        return "MISSING — VERIFY BEFORE ADDING";
    }

    private int classifyPriority(String kw, String jdText) {
        String lowerKw = kw.toLowerCase();
        if (CRITICAL_CORE_TERMS.contains(lowerKw) || countOccurrences(jdText, kw) >= 2) {
            return 1; // CRITICAL
        }
        if (SKILL_PHRASES.contains(lowerKw) || TECH_KEYWORDS.contains(lowerKw)) {
            return 2; // IMPORTANT
        }
        return 3; // NICE_TO_HAVE
    }

    private String getWhyItMattersReason(String kw, String jdText) {
        String lowerKw = kw.toLowerCase();
        if (CRITICAL_CORE_TERMS.contains(lowerKw)) {
            return "Explicit core requirement central to primary role execution.";
        }
        if (TECH_KEYWORDS.contains(lowerKw)) {
            return "Key technical tool used for daily operational workflows.";
        }
        if (SKILL_PHRASES.contains(lowerKw)) {
            return "Supports key responsibility area and team engagement strategy.";
        }
        return "Enhances overall candidate alignment and domain depth.";
    }

    private String getSuggestedLocation(String kw) {
        String lower = kw.toLowerCase();
        if (TECH_KEYWORDS.contains(lower)) return "Skills / Core Competencies";
        if (lower.contains("management") || lower.contains("leadership") || lower.contains("outreach")) return "Work Experience";
        if (lower.contains("development") || lower.contains("marketing") || lower.contains("conversion")) return "Professional Summary";
        return "Skills";
    }

    private String getIntegrationSuggestion(String kw) {
        return "Add under " + getSuggestedLocation(kw) + " or mention naturally within your recent achievement bullet points.";
    }

    private int countOccurrences(String text, String word) {
        if (text == null || word == null) return 0;
        Matcher m = Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b").matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    private Set<String> extractSmartCareerKeywords(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) return Collections.emptySet();

        String cleanedText = rawText
                .replaceAll("(?i)https?://\\S+", " ")
                .replaceAll("(?i)www\\.\\S+", " ")
                .replaceAll("(?i)forms\\.gle\\S+", " ")
                .replaceAll("(?i)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", " ");

        Set<String> extractedSkills = new LinkedHashSet<>();
        String lowerText = cleanedText.toLowerCase();

        for (String phrase : SKILL_PHRASES) {
            if (lowerText.contains(phrase)) {
                extractedSkills.add(capitalizePhrase(phrase));
            }
        }

        Pattern pattern = Pattern.compile("[a-zA-Z+#-]+");
        Matcher matcher = pattern.matcher(cleanedText);

        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            
            boolean isKnownShortTech = (word.length() <= 3) && TECH_KEYWORDS.contains(word);
            boolean isValidLength = word.length() >= 4 || isKnownShortTech;

            if (isValidLength && !STOP_WORDS.contains(word) && !isNumeric(word)) {
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

    private boolean isNumeric(String str) {
        if (str == null) return false;
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




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

    // Isolated random words, legal noise, recruitment fillers, and non-skill stop words MUST NEVER be extracted standalone
    private static final Set<String> ISOLATED_NOISE_WORDS = new HashSet<>(Arrays.asList(
        "the", "a", "an", "and", "or", "but", "is", "are", "was", "were", "be", "been", "being",
        "to", "of", "in", "on", "at", "by", "for", "with", "about", "against", "between", "into",
        "through", "during", "before", "after", "above", "below", "from", "up", "down", "out", "off",
        "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why",
        "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no",
        "nor", "not", "only", "own", "same", "so", "than", "too", "very", "can", "will", "just",
        "should", "now", "would", "could", "this", "that", "these", "those", "have", "has", "had",
        "do", "does", "did", "we", "you", "he", "she", "it", "they", "them", "their", "our", "us", "your",
        
        // Explicitly prohibited standalone words per specification
        "application", "students", "company", "fill", "read", "provided", "first", "fast", "opportunity",
        "employment", "performance", "conditions", "benefits", "applicants", "associates", "support",
        "global", "individual", "management", "technology", "service", "data", "business", "software",
        "microsoft", "dynamics", "framework", "plugin", "power", "apps", "soft", "custom", "workflow",
        "azure", "insights", "bus", "opening", "overview", "category", "batch", "eligibility", "criteria",
        "degree", "discipline", "btech", "mtech", "lpa", "ctc", "salary", "stipend", "month", "months",
        "year", "years", "persevex", "ecolab", "bangalore-based", "mumbai", "delhi", "refrain", "selected",
        "already", "google", "form", "forms", "link", "urls", "https", "http", "www", "gle", "url",
        "gynpshklwtvk", "vk8y6u7", "race", "religion", "gender", "identity", "disability", "citizenship",
        "marital", "orientation", "hiring", "apply", "register", "notice", "joining", "immediate",
        "duties", "role", "roles", "job", "work", "team", "teams", "experience", "qualifications",
        "responsibilities", "requirements", "details", "info", "information", "organization", "committed",
        "fostering", "delivering", "working", "ensure", "stay", "create", "day", "days", "time", "schedule",
        "contact", "phone", "email", "whatsapp", "telegram", "llp", "inc", "pvt", "ltd", "functions", "function"
    ));

    // Registry of recognized compound technical, functional, and domain entities (Preserved intact)
    private static final List<String> COMPOUND_ENTITIES = Arrays.asList(
        // Dynamics / CRM / Microsoft Stack
        "Microsoft Dynamics 365 CRM", "Dynamics 365 CE Development", "Dynamics 365 Customer Service",
        "Dynamics 365 Sales", "C# and .NET Framework", ".NET Framework", "Custom Workflow Activities",
        "PowerApps Component Framework (PCF)", "Azure Service Bus", "Application Insights",
        "Global Production Support", "Power Automate", "Power Apps", "Dataverse", "FetchXML",
        "XRM SDK", "Solution Management", "CRM Development", "Data Migration",
        
        // Business / Sales / EdTech / Functional
        "Business Development", "EdTech Sales", "Digital Marketing", "Lead Conversion",
        "Direct Outreach", "Strategic Partnerships", "Stakeholder Negotiation", "Client Nurturing",
        "Corporate Clients", "Critical Thinking", "Problem Solving", "Presentation Skills",
        "Interpersonal Skills", "Negotiation Skills", "Cross-Functional Leadership", "Market Research",
        "Customer Success", "Project Management", "Data Analytics", "Cloud Architecture",
        "System Design", "RESTful APIs", "REST API", "CI/CD Pipeline", "Microservices Architecture",
        "Content Operations", "Lead Generation", "Conversion Strategies", "Relationship Management",
        
        // Core Standalone Technologies
        "Python", "Java", "C#", "SQL", "JavaScript", "TypeScript", "Salesforce", "AWS", "Azure",
        "Power BI", "React", "Angular", "Spring Boot", "Git", "Azure DevOps", "Docker", "Kubernetes",
        "Jira", "Agile", "Scrum", "SQL Server", "PostgreSQL", "MongoDB", "Redis", "Machine Learning",
        "Deep Learning", "TensorFlow", "PyTorch", "NLP", "LLM"
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

        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Job Description is required.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean hasResume = (resumeText != null && !resumeText.trim().isEmpty());

        // 1. Identify Job Title & Company
        String roleTitle = extractJobTitle(jobDescription);
        String companyName = extractCompanyName(jobDescription);

        // 2. Generate Role Focus (2-3 sentences)
        String roleFocus = generateRoleFocus(jobDescription, roleTitle);

        // 3. Extract Valid Compound Skill Entities
        List<String> extractedEntities = extractValidSkillEntities(jobDescription);

        List<Map<String, String>> criticalKeywords = new ArrayList<>();
        List<Map<String, String>> importantKeywords = new ArrayList<>();
        List<Map<String, String>> niceToHaveKeywords = new ArrayList<>();
        List<Map<String, String>> contextualKeywords = new ArrayList<>();
        List<String> topAtsKeywords = new ArrayList<>();

        List<Map<String, String>> missingKeywordsList = new ArrayList<>();
        List<Map<String, String>> safeList = new ArrayList<>();
        List<Map<String, String>> synonymsList = new ArrayList<>();

        List<String> rawMatched = new ArrayList<>();
        List<String> rawMissing = new ArrayList<>();

        double weightedScoreSum = 0;
        double maxWeightedScore = 0;

        for (String entity : extractedEntities) {
            String status = hasResume ? determineResumeStatus(resumeText, entity) : "NOT_ANALYZED";
            String reason = getSkillReason(entity, jobDescription);
            int classification = classifyRequirementTier(entity, jobDescription);

            Map<String, String> item = new HashMap<>();
            item.put("keyword", entity);
            item.put("reason", reason);
            item.put("status", status);

            double weight = (classification == 1) ? 60.0 : (classification == 2) ? 30.0 : 10.0;
            maxWeightedScore += weight;

            if (status.equals("MATCHED") || status.equals("PARTIALLY MATCHED")) {
                weightedScoreSum += weight;
                rawMatched.add(entity);
            } else if (status.equals("MISSING")) {
                rawMissing.add(entity);
            }

            if (classification == 1) {
                criticalKeywords.add(item);
            } else if (classification == 2) {
                importantKeywords.add(item);
            } else if (classification == 3) {
                niceToHaveKeywords.add(item);
            } else {
                contextualKeywords.add(item);
            }

            // Top Ranked ATS Keywords (Limit 10-20)
            if (topAtsKeywords.size() < 15) {
                topAtsKeywords.add(entity);
            }

            // Populate Missing & Safe Integration if resume provided
            if (hasResume) {
                if (status.equals("MISSING")) {
                    Map<String, String> missItem = new HashMap<>();
                    missItem.put("keyword", entity);
                    missItem.put("importance", (classification == 1) ? "Critical" : (classification == 2) ? "Important" : "Nice-to-have");
                    missItem.put("status", "MISSING — VERIFY BEFORE ADDING");
                    missItem.put("whyItMatters", reason);
                    missItem.put("recommendation", "Do not add unless the candidate genuinely has this skill.");
                    missingKeywordsList.add(missItem);
                } else if (status.equals("MATCHED") || status.equals("PARTIALLY MATCHED")) {
                    Map<String, String> safeItem = new HashMap<>();
                    safeItem.put("keyword", entity);
                    safeItem.put("resumeEvidence", getResumeEvidence(resumeText, entity));
                    safeItem.put("suggestedLocation", getSuggestedSection(entity));
                    safeItem.put("integration", "Integrate naturally under " + getSuggestedSection(entity) + " using exact JD terminology.");
                    safeList.add(safeItem);
                }
            }

            // Add Synonyms if applicable
            String syn = getKnownSynonym(entity);
            if (syn != null) {
                Map<String, String> synMap = new HashMap<>();
                synMap.put("jdTerm", entity);
                synMap.put("equivalentTerm", syn);
                synonymsList.add(synMap);
            }
        }

        int estimatedAlignmentScore = hasResume && maxWeightedScore > 0 ? 
                (int) Math.round((weightedScoreSum / maxWeightedScore) * 100) : 0;

        String highlightedHtml = highlightMissingKeywords(jobDescription, rawMissing);

        // Final Recommendation Payload
        Map<String, Object> finalRecommendation = new HashMap<>();
        finalRecommendation.put("highestPriorityKeywords", topAtsKeywords.stream().limit(8).collect(Collectors.toList()));
        finalRecommendation.put("biggestGaps", rawMissing.stream().limit(5).collect(Collectors.toList()));
        finalRecommendation.put("atsWarning", "ATS optimization improves relevance; it does not guarantee interview selection. Never add a skill you cannot substantiate.");

        response.put("success", true);
        response.put("jobTitle", roleTitle);
        response.put("companyName", companyName);
        response.put("hasResume", hasResume);
        response.put("estimatedAlignmentScore", estimatedAlignmentScore);
        response.put("roleFocus", roleFocus);
        response.put("topAtsKeywords", topAtsKeywords);
        response.put("criticalKeywords", criticalKeywords);
        response.put("importantKeywords", importantKeywords);
        response.put("niceToHaveKeywords", niceToHaveKeywords);
        response.put("contextualKeywords", contextualKeywords);
        response.put("missingKeywordsList", missingKeywordsList);
        response.put("safeToIntegrateList", safeList);
        response.put("synonymsList", synonymsList);
        response.put("finalRecommendation", finalRecommendation);
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
        String targetRole = (String) payload.getOrDefault("role", "Professional");

        if (missingKeywords.isEmpty()) {
            response.put("success", true);
            response.put("bullets", Arrays.asList(
                "• Optimized core operational workflows resulting in a 35% performance enhancement.",
                "• Spearheaded cross-functional initiatives to deliver high-quality client solutions.",
                "• Implemented strategic process improvements reducing account acquisition downtime."
            ));
            return ResponseEntity.ok(response);
        }

        List<String> keywordsToUse = missingKeywords.stream().limit(6).collect(Collectors.toList());
        String k1 = keywordsToUse.size() > 0 ? keywordsToUse.get(0) : "strategic outreach";
        String k2 = keywordsToUse.size() > 1 ? keywordsToUse.get(1) : "lead conversion";
        String k3 = keywordsToUse.size() > 2 ? keywordsToUse.get(2) : "stakeholder negotiation";

        List<String> bulletPoints = Arrays.asList(
            "• Spearheaded execution of " + k1 + " and " + k2 + " to expand client engagement and operational output.",
            "• Leveraged " + k3 + " to establish high-value strategic partnerships and streamline delivery timelines.",
            "• Collaborated with cross-functional teams to align project deliverables with key performance targets."
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
            "and setting up CI/CD pipelines on AWS. Experience with Agile methodology, Git, and automated testing is required.");
        
        presets.put("AI / ML Engineer", 
            "Looking for an AI / Machine Learning Engineer skilled in Python, TensorFlow, PyTorch, and NLP. " +
            "You will build and deploy fine-tuned LLM models, design data pipelines with SQL and Docker, " +
            "and optimize real-time inference latency on AWS cloud infrastructure.");
        
        presets.put("Dynamics 365 Developer", 
            "Seeking a Senior Software Engineer - Dynamics CRM. Requirements: Microsoft Dynamics 365 CRM, " +
            "Dynamics 365 CE Development, C# and .NET Framework, Custom Workflow Activities, PowerApps Component Framework (PCF), " +
            "Azure Service Bus, Application Insights, Dataverse, Power Automate, and FetchXML.");
        
        return ResponseEntity.ok(presets);
    }

    private String extractJobTitle(String jdText) {
        if (jdText == null || jdText.trim().isEmpty()) return "Software Engineer / Professional";
        String lower = jdText.toLowerCase();

        // Regex for explicit role headers
        Pattern pattern = Pattern.compile("(?i)(?:job profile|role|title|opening overview|position)\\s*[:\\-]?\\s*([^\\n,.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(jdText);
        if (matcher.find()) {
            String title = matcher.group(1).trim();
            if (title.length() > 3 && title.length() < 60 && !title.toLowerCase().contains("btech") && !title.toLowerCase().contains("hiring")) {
                return title;
            }
        }

        if (lower.contains("dynamics") || lower.contains("crm")) return "Software Engineer - Dynamics CRM";
        if (lower.contains("full-stack") || lower.contains("fullstack")) return "Full-Stack Developer";
        if (lower.contains("business development") || lower.contains("edtech sales")) return "Business Development / EdTech Sales";
        if (lower.contains("machine learning") || lower.contains("ai engineer")) return "AI / ML Engineer";
        if (lower.contains("product manager")) return "Product Manager";

        return "Target Professional Role";
    }

    private String extractCompanyName(String jdText) {
        if (jdText == null) return "Hiring Organization";
        if (jdText.toLowerCase().contains("persevex")) return "Persevex LLP";
        if (jdText.toLowerCase().contains("ecolab")) return "Ecolab";
        if (jdText.toLowerCase().contains("microsoft")) return "Microsoft";
        if (jdText.toLowerCase().contains("google")) return "Google";
        return "Hiring Organization";
    }

    private String generateRoleFocus(String jdText, String roleTitle) {
        String lower = jdText.toLowerCase();
        if (lower.contains("dynamics") || lower.contains("crm")) {
            return "The employer is seeking an experienced Dynamics 365 Developer specialized in CRM customizations, C#/.NET plugin development, Dataverse architecture, and Azure Service integrations.";
        } else if (lower.contains("sales") || lower.contains("business development")) {
            return "The employer is seeking a high-performing Business Development specialist skilled in direct client outreach, lead conversion strategies, corporate partnerships, and stakeholder negotiation.";
        } else if (lower.contains("java") || lower.contains("python") || lower.contains("developer")) {
            return "The employer is looking for a strong Software Engineer with expertise in full-stack architecture, REST API design, cloud deployment, and automated testing.";
        }
        return "The employer is looking for a qualified professional with strong domain expertise and proven technical execution capabilities for the position of " + roleTitle + ".";
    }

    private List<String> extractValidSkillEntities(String jdText) {
        if (jdText == null || jdText.trim().isEmpty()) return Collections.emptyList();

        // 1. Pre-process text: remove URLs, emails, registration links, equal opportunity text
        String cleaned = jdText
                .replaceAll("(?i)https?://\\S+", " ")
                .replaceAll("(?i)www\\.\\S+", " ")
                .replaceAll("(?i)forms\\.gle\\S+", " ")
                .replaceAll("(?i)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", " ")
                .replaceAll("(?i)equal opportunity employer.*", " ")
                .replaceAll("(?i)does not discriminate.*", " ");

        Set<String> extracted = new LinkedHashSet<>();
        String lowerCleaned = cleaned.toLowerCase();

        // 2. Extract recognized compound entities intact
        for (String entity : COMPOUND_ENTITIES) {
            if (lowerCleaned.contains(entity.toLowerCase())) {
                extracted.add(entity);
            }
        }

        // Limit extracted total to 10-25 high value keywords maximum
        return extracted.stream().limit(22).collect(Collectors.toList());
    }

    private String determineResumeStatus(String resumeText, String entity) {
        if (resumeText == null || entity == null) return "MISSING";
        String resLower = resumeText.toLowerCase();
        String entLower = entity.toLowerCase();

        // Exact match
        if (resLower.contains(entLower)) {
            return "MATCHED";
        }

        // Partial match check: key word in entity exists in resume
        String[] words = entLower.split("\\s+");
        for (String w : words) {
            if (w.length() >= 4 && !ISOLATED_NOISE_WORDS.contains(w) && resLower.contains(w)) {
                return "PARTIALLY MATCHED";
            }
        }

        return "MISSING";
    }

    private int classifyRequirementTier(String entity, String jdText) {
        String lowerEnt = entity.toLowerCase();
        String lowerJd = jdText.toLowerCase();

        if (lowerJd.contains("must-have") || lowerJd.contains("required") || lowerJd.contains("mandatory") ||
            lowerEnt.contains("dynamics 365") || lowerEnt.contains("c#") || lowerEnt.contains("java") ||
            lowerEnt.contains("business development") || lowerEnt.contains("python") || lowerEnt.contains("sql")) {
            return 1; // CRITICAL
        }

        if (lowerJd.contains("preferred") || lowerEnt.contains("agile") || lowerEnt.contains("scrum") || lowerEnt.contains("git")) {
            return 2; // IMPORTANT
        }

        return 3; // NICE-TO-HAVE
    }

    private String getSkillReason(String entity, String jdText) {
        String lower = entity.toLowerCase();
        if (lower.contains("crm") || lower.contains("dynamics") || lower.contains("sales") || lower.contains("development")) {
            return "Explicit core requirement central to primary role execution.";
        }
        if (lower.contains("framework") || lower.contains("api") || lower.contains("azure") || lower.contains("aws")) {
            return "Core technical platform required for solution deployment.";
        }
        return "Key operational capability needed to deliver core responsibilities.";
    }

    private String getResumeEvidence(String resumeText, String entity) {
        if (resumeText == null) return "Mentioned in resume profile.";
        String lowerRes = resumeText.toLowerCase();
        String lowerEnt = entity.toLowerCase();
        if (lowerRes.contains(lowerEnt)) {
            return "Direct evidence found in resume text (" + entity + ").";
        }
        return "Related competency evidence identified in work history.";
    }

    private String getSuggestedSection(String entity) {
        String lower = entity.toLowerCase();
        if (lower.contains("c#") || lower.contains("framework") || lower.contains("api") || lower.contains("sql") || lower.contains("aws")) {
            return "Skills / Technical Stack";
        }
        if (lower.contains("development") || lower.contains("sales") || lower.contains("outreach")) {
            return "Professional Summary / Experience";
        }
        return "Work Experience";
    }

    private String getKnownSynonym(String entity) {
        String lower = entity.toLowerCase();
        if (lower.contains("microsoft dynamics 365 crm")) return "Dynamics 365 CRM";
        if (lower.contains("structured query language")) return "SQL";
        if (lower.contains("microsoft excel")) return "Excel";
        if (lower.contains("application programming interface")) return "REST API";
        return null;
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





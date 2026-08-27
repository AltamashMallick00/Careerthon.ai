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
        "company", "opportunity", "application", "students", "employee", "team", "work", "business",
        "technology", "information", "support", "management", "development", "communication",
        "professional", "environment", "organization", "experience", "candidate", "position",
        "responsibility", "responsibilities", "requirements", "qualifications", "hiring", "apply",
        "register", "link", "form", "forms", "batch", "lpa", "ctc", "salary", "stipend", "per",
        "month", "months", "annum", "year", "years", "date", "location", "bangalore-based", "mumbai",
        "delhi", "remote", "hybrid", "workplace", "applicant", "openings", "opening", "eligibility",
        "criteria", "btech", "mtech", "degree", "diploma", "bachelor", "bachelors", "master", "masters",
        "discipline", "preferred", "required", "overview", "category", "full-time", "part-time",
        "internship", "period", "probation", "pursuing", "final", "pre-final", "please", "refrain",
        "selected", "already", "following", "given", "must", "able", "using", "used", "based", "etc",
        "plus", "high", "good", "strong", "well", "key", "main", "description", "about", "services",
        "products", "committed", "fostering", "delivering", "working", "ensure", "stay", "create",
        "day", "days", "time", "schedule", "details", "info", "notice", "joining", "immediate",
        "llp", "inc", "pvt", "ltd", "google", "whatsapp", "telegram", "phone", "email", "contact",
        "http", "https", "www", "com", "gle", "forms.gle", "url", "persevex", "edtech", "functions",
        "mass", "recruiters", "recruiter", "race", "religion", "gender", "identity", "disability",
        "citizenship", "marital", "orientation", "analysis", "analysing", "gathering", "assist"
    ));

    // Recognized Compound Skill Entities Registry (Preserved intact as single terms)
    private static final List<String> COMPOUND_ENTITIES = Arrays.asList(
        // Business Analyst / Functional Requirements Entities
        "Business Requirement Documents (BRDs)", "Functional Requirement Documents (FRDs)",
        "Business Requirements", "Functional Requirements", "Requirements Analysis", "Business Analysis",
        "Software Development Life Cycle (SDLC)", "User Stories", "Acceptance Criteria", "Use Cases",
        "Process Flows", "Stakeholder Communication", "Stakeholder Management", "Generative AI",
        
        // Dynamics / CRM / Microsoft Stack
        "Microsoft Dynamics 365 CRM", "Dynamics 365 CE Development", "Dynamics 365 Customer Service",
        "Dynamics 365 Sales", "C# and .NET Framework", ".NET Framework", "Custom Workflow Activities",
        "PowerApps Component Framework (PCF)", "Azure Service Bus", "Application Insights",
        "Global Production Support", "Power Automate", "Power Apps", "Dataverse", "FetchXML",
        
        // Business / Sales / EdTech / Functional
        "Business Development", "EdTech Sales", "Digital Marketing", "Lead Conversion",
        "Direct Outreach", "Strategic Partnerships", "Stakeholder Negotiation", "Client Nurturing",
        "Corporate Clients", "Critical Thinking", "Problem Solving", "Presentation Skills",
        "Interpersonal Skills", "Negotiation Skills", "Cross-Functional Leadership", "Market Research",
        "Customer Success", "Project Management", "Data Analytics", "Cloud Architecture",
        "System Design", "RESTful APIs", "REST API", "CI/CD Pipeline", "Microservices Architecture",
        "Content Operations", "Lead Generation", "Conversion Strategies", "Relationship Management",
        
        // Technical Platforms, Frameworks & Tools
        "Python", "Java", "C#", "SQL", "JavaScript", "TypeScript", "Salesforce", "AWS", "Azure",
        "Power BI", "React", "Angular", "Spring Boot", "Git", "Azure DevOps", "Docker", "Kubernetes",
        "Jira", "Confluence", "Miro", "Figma", "Agile", "Scrum", "SQL Server", "PostgreSQL", "MongoDB",
        "Redis", "Machine Learning", "Deep Learning", "TensorFlow", "PyTorch", "NLP", "LLM", "Computer Vision"
    );

    @GetMapping("/job-match")
    public String showJobMatchPage() {
        return "job_match";
    }

    @PostMapping("/api/job-match")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> performJobMatch(
            @RequestParam("jobDescription") String jobDescription,
            @RequestParam(value = "resumeText", required = false) String resumeText) {

        Map<String, Object> response = new HashMap<>();

        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Job Description is required.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean hasResume = (resumeText != null && !resumeText.trim().isEmpty());

        // 1. Identify Job Title & Company Name
        String roleTitle = extractJobTitle(jobDescription);
        String companyName = extractCompanyName(jobDescription);

        // 2. Determine Role Focus (2-3 sentences)
        String roleFocus = generateRoleFocus(jobDescription, roleTitle);

        // 3. Extract Valid Compound Skill Entities INDEPENDENTLY from the Resume
        List<String> extractedEntities = extractValidSkillEntities(jobDescription);

        List<Map<String, String>> criticalKeywords = new ArrayList<>();
        List<Map<String, String>> importantKeywords = new ArrayList<>();
        List<Map<String, String>> niceToHaveKeywords = new ArrayList<>();
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
            String whyItMatters = getSkillReason(entity, jobDescription);
            int classification = classifyRequirementTier(entity, jobDescription);

            Map<String, String> item = new HashMap<>();
            item.put("keyword", entity);
            item.put("whyItMatters", whyItMatters);
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
            } else {
                niceToHaveKeywords.add(item);
            }

            // Top Ranked ATS Keywords (Target: 10-20 highest value)
            if (topAtsKeywords.size() < 15) {
                topAtsKeywords.add(entity);
            }

            // Populate Missing Requirements & Safe Integration if resume provided
            if (hasResume) {
                if (status.equals("MISSING")) {
                    Map<String, String> missItem = new HashMap<>();
                    missItem.put("keyword", entity);
                    missItem.put("importance", (classification == 1) ? "Critical" : (classification == 2) ? "Important" : "Nice-to-Have");
                    missItem.put("whyItMatters", whyItMatters);
                    missItem.put("action", "MISSING — VERIFY BEFORE ADDING");
                    missingKeywordsList.add(missItem);
                } else if (status.equals("MATCHED") || status.equals("PARTIALLY MATCHED")) {
                    Map<String, String> safeItem = new HashMap<>();
                    safeItem.put("keyword", entity);
                    safeItem.put("evidence", getResumeEvidence(resumeText, entity));
                    safeItem.put("recommendedLocation", getSuggestedSection(entity));
                    safeItem.put("suggestedIntegration", "Integrate naturally under " + getSuggestedSection(entity) + " using exact JD terminology.");
                    safeList.add(safeItem);
                }
            }

            // Synonyms & ATS Variations
            String syn = getKnownSynonym(entity);
            if (syn != null) {
                Map<String, String> synMap = new HashMap<>();
                synMap.put("jdTerm", entity);
                synMap.put("equivalentTerm", syn);
                synonymsList.add(synMap);
            }
        }

        String alignmentScoreText = hasResume && maxWeightedScore > 0 ? 
                Math.round((weightedScoreSum / maxWeightedScore) * 100) + "%" : 
                "Not Available — Resume Not Provided";

        String highlightedHtml = highlightMissingKeywords(jobDescription, rawMissing);

        // Final Recommendation Payload
        Map<String, Object> finalRecommendation = new HashMap<>();
        finalRecommendation.put("highestPriorityKeywords", topAtsKeywords.stream().limit(8).collect(Collectors.toList()));
        finalRecommendation.put("biggestGaps", rawMissing.stream().limit(5).collect(Collectors.toList()));
        finalRecommendation.put("candidateStrategy", "Focus on integrating matched competencies naturally under your Experience section. For missing skills, verify you possess the underlying capability before adding them.");
        finalRecommendation.put("atsWarning", "ATS optimization improves relevance; it does not guarantee interview selection. Never add a skill you cannot substantiate.");

        response.put("success", true);
        response.put("jobTitle", roleTitle);
        response.put("company", companyName);
        response.put("hasResume", hasResume);
        response.put("estimatedJobAlignment", alignmentScoreText);
        response.put("roleFocus", roleFocus);
        response.put("topAtsKeywords", topAtsKeywords);
        response.put("criticalKeywords", criticalKeywords);
        response.put("importantKeywords", importantKeywords);
        response.put("niceToHaveKeywords", niceToHaveKeywords);
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

        if (missingKeywords.isEmpty()) {
            response.put("success", true);
            response.put("bullets", Arrays.asList(
                "• Prepared Business Requirement Documents (BRDs) and Functional Requirement Documents (FRDs) to streamline project deliverables.",
                "• Collaborated with cross-functional teams to define User Stories and Acceptance Criteria.",
                "• Facilitate process flows mapping to eliminate operational bottlenecks and improve project delivery cycles."
            ));
            return ResponseEntity.ok(response);
        }

        List<String> keywordsToUse = missingKeywords.stream().limit(6).collect(Collectors.toList());
        String k1 = keywordsToUse.size() > 0 ? keywordsToUse.get(0) : "Requirements Analysis";
        String k2 = keywordsToUse.size() > 1 ? keywordsToUse.get(1) : "User Stories";
        String k3 = keywordsToUse.size() > 2 ? keywordsToUse.get(2) : "Process Flows";

        List<String> bulletPoints = Arrays.asList(
            "• Spearheaded " + k1 + " and prepared detailed project documentation to guide development teams.",
            "• Defined comprehensive " + k2 + " and acceptance criteria to ensure high-quality feature delivery.",
            "• Modeled " + k3 + " to optimize team workflows and accelerate stakeholder sign-off."
        );

        response.put("success", true);
        response.put("bullets", bulletPoints);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/job-match/presets")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getPresetJobs() {
        Map<String, String> presets = new LinkedHashMap<>();
        presets.put("Business Analyst", 
            "We are looking for a Business Analyst Intern. Responsibilities: Assist Business Analysts in gathering and analysing business and functional requirements. " +
            "Assist in preparing Business Requirement Documents (BRDs), Functional Requirement Documents (FRDs), User Stories, Acceptance Criteria, Use Cases, and Process Flows. " +
            "Gain exposure to Generative AI, Agile SDLC, Jira, and Stakeholder Communication.");

        presets.put("Dynamics 365 Developer", 
            "Seeking a Senior Software Engineer - Dynamics CRM. Requirements: Microsoft Dynamics 365 CRM, " +
            "Dynamics 365 CE Development, C# and .NET Framework, Custom Workflow Activities, PowerApps Component Framework (PCF), " +
            "Azure Service Bus, Application Insights, Dataverse, Power Automate, and FetchXML.");

        presets.put("Full-Stack Developer", 
            "We are seeking a Senior Full-Stack Developer proficient in Java, Spring Boot, React, and PostgreSQL. " +
            "Responsibilities include designing RESTful APIs, containerizing microservices with Docker and Kubernetes, " +
            "and setting up CI/CD pipelines on AWS. Experience with Agile methodology, Git, and automated testing is required.");

        return ResponseEntity.ok(presets);
    }

    private String extractJobTitle(String jdText) {
        if (jdText == null || jdText.trim().isEmpty()) return "Software Engineer / Professional";
        String lower = jdText.toLowerCase();

        // Check for explicit role headers
        Pattern pattern = Pattern.compile("(?i)(?:job profile|role|title|opening overview|position)\\s*[:\\-]?\\s*([^\\n,.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(jdText);
        if (matcher.find()) {
            String title = matcher.group(1).trim();
            if (title.length() > 3 && title.length() < 60 && !title.toLowerCase().contains("btech") && !title.toLowerCase().contains("hiring")) {
                return title;
            }
        }

        if (lower.contains("business analyst")) return "Business Analyst Intern";
        if (lower.contains("dynamics") || lower.contains("crm")) return "Software Engineer - Dynamics CRM";
        if (lower.contains("full-stack") || lower.contains("fullstack")) return "Full-Stack Developer";
        if (lower.contains("business development") || lower.contains("edtech sales")) return "Business Development / EdTech Sales";
        if (lower.contains("machine learning") || lower.contains("ai engineer")) return "AI / ML Engineer";

        return "Target Professional Role";
    }

    private String extractCompanyName(String jdText) {
        if (jdText == null) return "Hiring Organization";
        if (jdText.toLowerCase().contains("codemonk")) return "Codemonk";
        if (jdText.toLowerCase().contains("persevex")) return "Persevex LLP";
        if (jdText.toLowerCase().contains("ecolab")) return "Ecolab";
        if (jdText.toLowerCase().contains("microsoft")) return "Microsoft";
        return "Hiring Organization";
    }

    private String generateRoleFocus(String jdText, String roleTitle) {
        String lower = jdText.toLowerCase();
        if (lower.contains("business analyst")) {
            return "The employer is looking for a Business Analyst candidate skilled in gathering requirements, modeling process flows, and creating BRDs, FRDs, User Stories, and Acceptance Criteria.";
        } else if (lower.contains("dynamics") || lower.contains("crm")) {
            return "The employer is seeking an experienced Dynamics 365 Developer specialized in CRM customizations, C#/.NET plugin development, Dataverse architecture, and Azure Service integrations.";
        } else if (lower.contains("sales") || lower.contains("business development")) {
            return "The employer is seeking a high-performing Business Development specialist skilled in direct client outreach, lead conversion strategies, corporate partnerships, and stakeholder negotiation.";
        }
        return "The employer is looking for a qualified professional with strong domain expertise and proven technical execution capabilities for the position of " + roleTitle + ".";
    }

    private List<String> extractValidSkillEntities(String jdText) {
        if (jdText == null || jdText.trim().isEmpty()) return Collections.emptyList();

        // 1. Pre-process text: remove URLs, links, email addresses, registration instructions, equal opportunity text
        String cleaned = jdText
                .replaceAll("(?i)https?://\\S+", " ")
                .replaceAll("(?i)www\\.\\S+", " ")
                .replaceAll("(?i)forms\\.gle\\S+", " ")
                .replaceAll("(?i)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", " ")
                .replaceAll("(?i)equal opportunity employer.*", " ")
                .replaceAll("(?i)does not discriminate.*", " ");

        Set<String> extracted = new LinkedHashSet<>();
        String lowerCleaned = cleaned.toLowerCase();

        // 2. Match recognized compound entities intact
        for (String entity : COMPOUND_ENTITIES) {
            if (lowerCleaned.contains(entity.toLowerCase())) {
                extracted.add(entity);
            }
        }

        // Limit output to 10-20 high-value keywords maximum
        return extracted.stream().limit(18).collect(Collectors.toList());
    }

    private String determineResumeStatus(String resumeText, String entity) {
        if (resumeText == null || entity == null) return "MISSING";
        String resLower = resumeText.toLowerCase();
        String entLower = entity.toLowerCase();

        // Direct exact match
        if (resLower.contains(entLower)) {
            return "MATCHED";
        }

        // Partial match check: key distinct term in entity exists in resume
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
            lowerEnt.contains("business analysis") || lowerEnt.contains("requirements analysis") || lowerEnt.contains("brd") ||
            lowerEnt.contains("dynamics 365") || lowerEnt.contains("c#") || lowerEnt.contains("java") || lowerEnt.contains("python")) {
            return 1; // CRITICAL / MUST-HAVE
        }

        if (lowerEnt.contains("user stories") || lowerEnt.contains("acceptance criteria") || lowerEnt.contains("process flows") || lowerEnt.contains("stakeholder")) {
            return 2; // IMPORTANT
        }

        return 3; // NICE-TO-HAVE
    }

    private String getSkillReason(String entity, String jdText) {
        String lower = entity.toLowerCase();
        if (lower.contains("brd") || lower.contains("frd") || lower.contains("requirements") || lower.contains("crm")) {
            return "Essential core deliverable central to primary role responsibilities.";
        }
        if (lower.contains("user stories") || lower.contains("process flows") || lower.contains("stakeholder")) {
            return "Key functional skill directly supporting daily project execution.";
        }
        return "Enhances candidate capability and domain depth for this position.";
    }

    private String getResumeEvidence(String resumeText, String entity) {
        if (resumeText == null) return "Supported by candidate background.";
        String lowerRes = resumeText.toLowerCase();
        String lowerEnt = entity.toLowerCase();
        if (lowerRes.contains(lowerEnt)) {
            return "Direct evidence identified in resume text (" + entity + ").";
        }
        return "Related functional experience demonstrated in candidate project work.";
    }

    private String getSuggestedSection(String entity) {
        String lower = entity.toLowerCase();
        if (lower.contains("brd") || lower.contains("frd") || lower.contains("user stories") || lower.contains("requirements")) {
            return "Project Experience / Work History";
        }
        if (lower.contains("python") || lower.contains("c#") || lower.contains("sql") || lower.contains("jira")) {
            return "Skills / Technical Stack";
        }
        return "Work Experience";
    }

    private String getKnownSynonym(String entity) {
        String lower = entity.toLowerCase();
        if (lower.contains("business requirement documents")) return "BRDs";
        if (lower.contains("functional requirement documents")) return "FRDs";
        if (lower.contains("microsoft dynamics 365 crm")) return "Dynamics 365 CRM";
        if (lower.contains("software development life cycle")) return "SDLC";
        if (lower.contains("structured query language")) return "SQL";
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

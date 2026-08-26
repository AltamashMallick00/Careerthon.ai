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
        "have", "has", "had", "do", "does", "did", "would", "could", "should", "this", "that", "these", "those"
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

        for (String keyword : targetKeywords) {
            if (containsWordIgnoreCase(resumeText, keyword)) {
                matched.add(keyword);
            } else {
                missing.add(keyword);
            }
        }

        int totalCount = targetKeywords.size();
        int matchPct = totalCount == 0 ? 100 : (int) Math.round(((double) matched.size() / totalCount) * 100);

        // Highlight missing keywords in red within the original job description HTML-safe output
        String highlightedHtml = highlightMissingKeywords(jobDescription, missing);

        response.put("success", true);
        response.put("matchPercentage", matchPct);
        response.put("matchedKeywords", matched);
        response.put("missingKeywords", missing);
        response.put("highlightedJobDescription", highlightedHtml);

        return ResponseEntity.ok(response);
    }

    private Set<String> extractKeywords(String text) {
        Set<String> keywords = new LinkedHashSet<>();
        // Match standard words, including languages with symbols like C++, C#
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

        // Exact word match or subphrase match
        Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(wordLower) + "\\b");
        return wordPattern.matcher(sourceLower).find() || sourceLower.contains(wordLower);
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private String highlightMissingKeywords(String originalText, List<String> missingKeywords) {
        if (originalText == null) return "";
        
        // Escape basic HTML elements to prevent HTML Injection
        String safeText = originalText
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");

        if (missingKeywords.isEmpty()) {
            return safeText;
        }

        // Sort keywords by length descending to replace longer phrases/words first
        List<String> sortedMissing = missingKeywords.stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .collect(Collectors.toList());

        String result = safeText;
        for (String keyword : sortedMissing) {
            // Regex to match exact word boundary case-insensitively
            // Using a unique replacement placeholder to prevent recursive replacement of HTML tags themselves
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

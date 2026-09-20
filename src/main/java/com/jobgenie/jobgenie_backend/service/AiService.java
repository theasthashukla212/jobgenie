package com.jobgenie.jobgenie_backend.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobgenie.jobgenie_backend.dto.AtsScoreResponse;
import com.jobgenie.jobgenie_backend.dto.TailorResumeResponse;

@Service
public class AiService {
    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String apiUrl;

    public AiService(ObjectMapper objectMapper,
                     @Value("${app.ai.api-key:}") String apiKey,
                     @Value("${app.ai.model:gpt-4o-mini}") String model,
                     @Value("${app.ai.api-url:https://api.openai.com/v1/chat/completions}") String apiUrl) {
        this.client = RestClient.builder().build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
    }

    public AtsScoreResponse score(String resumeText, String jobDescription) {
        return fallbackScore(resumeText, jobDescription);
    }

    public TailorResumeResponse tailor(String resumeText, String jobDescription) {
        if (apiKey.isBlank()) {
            return fallbackTailoredResume(resumeText, jobDescription);
        }
        try {
            String tailored = chat("Rewrite the resume for the job. Preserve facts, do not invent experience. Return only the resume text.",
                    "Resume:\n" + resumeText + "\nJob description:\n" + jobDescription);
            if (tailored == null || tailored.isBlank()) {
                return fallbackTailoredResume(resumeText, jobDescription);
            }
            AtsScoreResponse score = score(tailored, jobDescription);
            return new TailorResumeResponse(tailored, score, model, false);
        } catch (RestClientException | IllegalStateException | IllegalArgumentException exception) {
            return fallbackTailoredResume(resumeText, jobDescription);
        }
    }

    private TailorResumeResponse fallbackTailoredResume(String resumeText, String jobDescription) {
        AtsScoreResponse score = fallbackScore(resumeText, jobDescription);
        return new TailorResumeResponse(resumeText, score, "local-keyword-fallback", true);
    }

    private String chat(String system, String user) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("temperature", 0.2);
        payload.put("messages", List.of(Map.of("role", "system", "content", system), Map.of("role", "user", "content", user)));
        JsonNode response = client.post().uri(apiUrl).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey).body(payload).retrieve().body(JsonNode.class);
        if (response == null || response.path("choices").isEmpty()) {
            throw new IllegalStateException("AI provider returned no response");
        }

        JsonNode firstChoice = response.path("choices").get(0);
        if (firstChoice == null || firstChoice.isNull()) {
            throw new IllegalStateException("AI provider returned an empty choice");
        }

        JsonNode message = firstChoice.path("message");
        if (message == null || message.isNull()) {
            throw new IllegalStateException("AI provider returned an empty message");
        }

        JsonNode content = message.path("content");
        if (content.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode node : content) {
                if (node.path("text").isTextual()) {
                    builder.append(node.path("text").asText());
                }
            }
            if (builder.length() > 0) {
                return builder.toString().trim();
            }
        }

        String text = content.asText();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("AI provider returned empty content");
        }
        return stripMarkdown(text);
    }

    private AtsScoreResponse fallbackScore(String resumeText, String jobDescription) {
        Set<String> resumeWords = words(resumeText);
        Set<String> jobWords = words(jobDescription);
        Set<String> meaningfulJobWords = jobWords.stream().filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toCollection(TreeSet::new));
        List<String> matching = meaningfulJobWords.stream().filter(resumeWords::contains).limit(20).toList();
        List<String> missing = meaningfulJobWords.stream().filter(word -> !resumeWords.contains(word)).limit(12).toList();
        int keywordScore = meaningfulJobWords.isEmpty() ? 0 : matching.size() * 55 / meaningfulJobWords.size();
        int structureScore = structureScore(resumeText);
        int lengthScore = resumeText.trim().length() >= 250 ? 15 : 5;
        int score = Math.min(85, keywordScore + structureScore + lengthScore);
        return new AtsScoreResponse(score, matching, missing, "local-keyword-fallback", true);
    }

    private int structureScore(String resumeText) {
        String normalized = resumeText.toLowerCase(Locale.ROOT);
        int sections = 0;
        for (String section : List.of("experience", "education", "skills", "summary")) {
            if (normalized.contains(section)) sections++;
        }
        return sections * 5;
    }

    private Set<String> words(String text) {
        Set<String> words = new TreeSet<>();
        Pattern.compile("[a-zA-Z][a-zA-Z0-9+#.-]{2,}").matcher(text.toLowerCase(Locale.ROOT))
                .results().forEach(match -> words.add(match.group()));
        return words;
    }

    private static final Set<String> STOP_WORDS = Set.of(
            "about", "after", "again", "also", "been", "being", "between", "both", "could", "from",
            "have", "into", "more", "most", "other", "over", "should", "some", "such", "than",
            "that", "their", "there", "these", "they", "this", "those", "through", "under", "using",
            "want", "with", "will", "work", "your"
    );

    private List<String> strings(JsonNode node) {
        List<String> values = new java.util.ArrayList<>();
        node.forEach(value -> values.add(value.asText()));
        return values;
    }

    private String stripMarkdown(String value) {
        return value.replace("```json", "").replace("```", "").trim();
    }
}
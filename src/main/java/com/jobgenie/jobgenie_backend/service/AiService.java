package com.jobgenie.jobgenie_backend.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        if (apiKey.isBlank()) return fallbackScore(resumeText, jobDescription);
        try {
            String content = chat("Return only JSON with integer score 0-100, arrays matchingSkills and missingSkills.",
                    "Score this resume against this job description. Resume:\n" + resumeText + "\nJob:\n" + jobDescription);
            JsonNode json = objectMapper.readTree(stripMarkdown(content));
                return new AtsScoreResponse(json.path("score").asInt(), strings(json.path("matchingSkills")),
                    strings(json.path("missingSkills")), model, false);
        } catch (JsonProcessingException | RestClientException exception) {
            return fallbackScore(resumeText, jobDescription);
        }
    }

    public TailorResumeResponse tailor(String resumeText, String jobDescription) {
        if (apiKey.isBlank()) {
            AtsScoreResponse score = fallbackScore(resumeText, jobDescription);
            return new TailorResumeResponse(resumeText, score, "local-keyword-fallback", true);
        }
        try {
            String tailored = chat("Rewrite the resume for the job. Preserve facts, do not invent experience. Return only the resume text.",
                    "Resume:\n" + resumeText + "\nJob description:\n" + jobDescription);
            AtsScoreResponse score = score(tailored, jobDescription);
            return new TailorResumeResponse(tailored, score, model, false);
        } catch (RestClientException exception) {
            AtsScoreResponse score = fallbackScore(resumeText, jobDescription);
            return new TailorResumeResponse(resumeText, score, "local-keyword-fallback", true);
        }
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
        return response.path("choices").path(0).path("message").path("content").asText();
    }

    private AtsScoreResponse fallbackScore(String resumeText, String jobDescription) {
        Set<String> resumeWords = words(resumeText);
        Set<String> jobWords = words(jobDescription);
        List<String> matching = jobWords.stream().filter(resumeWords::contains).limit(20).toList();
        List<String> missing = jobWords.stream().filter(word -> !resumeWords.contains(word)).limit(12).toList();
        int score = jobWords.isEmpty() ? 0 : Math.min(100, Math.max(10, matching.size() * 100 / jobWords.size()));
        return new AtsScoreResponse(score, matching, missing, "local-keyword-fallback", true);
    }

    private Set<String> words(String text) {
        Set<String> words = new TreeSet<>();
        Pattern.compile("[a-zA-Z][a-zA-Z0-9+#.-]{2,}").matcher(text.toLowerCase(Locale.ROOT))
                .results().forEach(match -> words.add(match.group()));
        return words;
    }

    private List<String> strings(JsonNode node) {
        List<String> values = new java.util.ArrayList<>();
        node.forEach(value -> values.add(value.asText()));
        return values;
    }

    private String stripMarkdown(String value) {
        return value.replace("```json", "").replace("```", "").trim();
    }
}
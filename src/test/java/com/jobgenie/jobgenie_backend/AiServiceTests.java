package com.jobgenie.jobgenie_backend;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobgenie.jobgenie_backend.dto.AtsScoreResponse;
import com.jobgenie.jobgenie_backend.service.AiService;

class AiServiceTests {

    @Test
    void atsScoreIsConservativeAndReportsKeywordGaps() {
        AiService service = new AiService(new ObjectMapper(), "", "test-model", "http://localhost:1");

        AtsScoreResponse result = service.score(
                "Alex Johnson\nSUMMARY\nFrontend developer\nSKILLS\nReact, JavaScript",
                "Senior backend engineer with Java, Spring Boot, PostgreSQL, AWS, and Kubernetes experience");

        assertTrue(result.score() <= 60);
        assertTrue(result.missingSkills().contains("aws"));
        assertTrue(result.missingSkills().contains("java"));
    }
}
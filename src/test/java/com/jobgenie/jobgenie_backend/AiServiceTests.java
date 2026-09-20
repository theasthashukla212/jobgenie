package com.jobgenie.jobgenie_backend;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobgenie.jobgenie_backend.dto.AtsScoreResponse;
import com.jobgenie.jobgenie_backend.dto.TailorResumeResponse;
import com.jobgenie.jobgenie_backend.service.AiService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

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

    @Test
    void malformedAiResponseFallsBackWithoutThrowing() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/chat", (HttpExchange exchange) -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            byte[] payload = "{}".getBytes();
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(payload);
            }
        });
        server.start();

        try {
            AiService service = new AiService(new ObjectMapper(), "key-123", "test-model", "http://localhost:" + server.getAddress().getPort() + "/chat");
            TailorResumeResponse response = assertDoesNotThrow(() -> service.tailor("resume text", "job description"));
            assertEquals("resume text", response.tailoredResume());
            assertTrue(response.atsScore().score() >= 0);
        } finally {
            server.stop(0);
        }
    }
}
package com.jobgenie.jobgenie_backend.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobgenie.jobgenie_backend.dto.ExternalJobResponse;

@Service
public class ExternalJobService {
    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String jobsUrl;

    public ExternalJobService(ObjectMapper objectMapper,
                               @Value("${app.jobs.external-url:https://www.arbeitnow.com/api/job-board-api}") String jobsUrl) {
        this.client = RestClient.builder().build();
        this.objectMapper = objectMapper;
        this.jobsUrl = jobsUrl;
    }

    public List<ExternalJobResponse> search(String query) {
        String body = client.get().uri(jobsUrl).accept(MediaType.APPLICATION_JSON).retrieve().body(String.class);
        try {
            JsonNode data = objectMapper.readTree(body).path("data");
            List<ExternalJobResponse> jobs = new ArrayList<>();
            for (JsonNode job : data) {
                String title = job.path("title").asText("");
                String company = job.path("company_name").asText("Unknown company");
                String description = job.path("description").asText("");
                String searchable = (title + " " + company + " " + description).toLowerCase();
                if (query != null && !query.isBlank() && !searchable.contains(query.toLowerCase().trim())) continue;
                List<String> tags = new ArrayList<>();
                job.path("tags").forEach(tag -> tags.add(tag.asText()));
                String type = job.path("job_types").isArray() && job.path("job_types").size() > 0
                        ? job.path("job_types").get(0).asText() : "Full-time";
                Instant postedAt = job.hasNonNull("created_at") ? Instant.ofEpochSecond(job.path("created_at").asLong()) : null;
                jobs.add(new ExternalJobResponse(job.path("slug").asText(job.path("url").asText()), title,
                        company, job.path("location").asText("Remote"), description, type,
                        job.path("url").asText(), postedAt, tags, "Arbeitnow"));
            }
            return jobs;
        } catch (JsonProcessingException | RestClientException exception) {
            throw new IllegalStateException("External job provider returned an invalid response", exception);
        }
    }
}
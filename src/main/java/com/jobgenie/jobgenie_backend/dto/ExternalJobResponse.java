package com.jobgenie.jobgenie_backend.dto;

import java.time.Instant;
import java.util.List;

public record ExternalJobResponse(String id, String title, String company, String location,
                                  String description, String type, String url,
                                  Instant postedAt, List<String> tags, String source) {}
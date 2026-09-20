package com.jobgenie.jobgenie_backend.dto;

import java.util.List;

public record AtsScoreResponse(int score, List<String> matchingSkills, List<String> missingSkills,
                               String provider, boolean fallback, AtsBreakdown breakdown) {}
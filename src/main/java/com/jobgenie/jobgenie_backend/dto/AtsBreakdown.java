package com.jobgenie.jobgenie_backend.dto;

import java.util.List;

public record AtsBreakdown(int keywordScore, int structureScore, int lengthScore,
                           int keywordCoverage, List<String> recommendations) {}
package com.jobgenie.jobgenie_backend.dto;

public record TailorResumeResponse(String tailoredResume, AtsScoreResponse atsScore,
                                   String provider, boolean fallback) {}
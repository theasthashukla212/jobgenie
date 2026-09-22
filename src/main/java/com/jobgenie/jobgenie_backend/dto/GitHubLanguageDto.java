package com.jobgenie.jobgenie_backend.dto;

public class GitHubLanguageDto {
    private String name;
    private Double percentage;
    private String color;

    public GitHubLanguageDto() {}

    public GitHubLanguageDto(String name, Double percentage, String color) {
        this.name = name;
        this.percentage = percentage;
        this.color = color;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}

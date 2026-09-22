package com.jobgenie.jobgenie_backend.dto;

import java.util.List;

public class GitHubRepoDto {
    private Long id;
    private String name;
    private String description;
    private String language;
    private Integer stars;
    private Integer forks;
    private String updatedAt;
    private List<String> topics;
    private String url;

    public GitHubRepoDto() {}

    public GitHubRepoDto(Long id, String name, String description, String language,
                         Integer stars, Integer forks, String updatedAt,
                         List<String> topics, String url) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.language = language;
        this.stars = stars;
        this.forks = forks;
        this.updatedAt = updatedAt;
        this.topics = topics;
        this.url = url;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }

    public Integer getForks() { return forks; }
    public void setForks(Integer forks) { this.forks = forks; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}

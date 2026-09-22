package com.jobgenie.jobgenie_backend.dto;

import java.util.List;

public class GitHubProfileDto {
    private boolean connected;
    private String username;
    private String name;
    private String avatar;
    private String bio;
    private String profileUrl;
    private Integer followers;
    private Integer following;
    private Integer publicRepos;
    private Integer totalStars;
    private Integer totalForks;
    private Integer contributions;
    private List<GitHubLanguageDto> languages;
    private List<GitHubRepoDto> repos;
    private GitHubContributionCalendarDto contributionCalendar;

    public GitHubProfileDto() {}

    public static GitHubProfileDto disconnected() {
        GitHubProfileDto dto = new GitHubProfileDto();
        dto.setConnected(false);
        return dto;
    }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }

    public Integer getFollowers() { return followers; }
    public void setFollowers(Integer followers) { this.followers = followers; }

    public Integer getFollowing() { return following; }
    public void setFollowing(Integer following) { this.following = following; }

    public Integer getPublicRepos() { return publicRepos; }
    public void setPublicRepos(Integer publicRepos) { this.publicRepos = publicRepos; }

    public Integer getTotalStars() { return totalStars; }
    public void setTotalStars(Integer totalStars) { this.totalStars = totalStars; }

    public Integer getTotalForks() { return totalForks; }
    public void setTotalForks(Integer totalForks) { this.totalForks = totalForks; }

    public Integer getContributions() { return contributions; }
    public void setContributions(Integer contributions) { this.contributions = contributions; }

    public List<GitHubLanguageDto> getLanguages() { return languages; }
    public void setLanguages(List<GitHubLanguageDto> languages) { this.languages = languages; }

    public List<GitHubRepoDto> getRepos() { return repos; }
    public void setRepos(List<GitHubRepoDto> repos) { this.repos = repos; }

    public GitHubContributionCalendarDto getContributionCalendar() { return contributionCalendar; }
    public void setContributionCalendar(GitHubContributionCalendarDto contributionCalendar) { this.contributionCalendar = contributionCalendar; }
}

package com.jobgenie.jobgenie_backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "github_connections")
public class GitHubConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Long githubUserId;

    @Column(nullable = false)
    private String githubUsername;

    private String githubName;

    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String htmlUrl;

    @Column(nullable = false)
    private String accessToken;

    private Integer followers = 0;

    private Integer following = 0;

    private Integer publicRepos = 0;

    @Column(nullable = false)
    private LocalDateTime connectedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public GitHubConnection() {}

    public GitHubConnection(User user, Long githubUserId, String githubUsername, String githubName,
                            String avatarUrl, String bio, String htmlUrl, String accessToken,
                            Integer followers, Integer following, Integer publicRepos) {
        this.user = user;
        this.githubUserId = githubUserId;
        this.githubUsername = githubUsername;
        this.githubName = githubName;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.htmlUrl = htmlUrl;
        this.accessToken = accessToken;
        this.followers = followers != null ? followers : 0;
        this.following = following != null ? following : 0;
        this.publicRepos = publicRepos != null ? publicRepos : 0;
        this.connectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Long getGithubUserId() { return githubUserId; }
    public void setGithubUserId(Long githubUserId) { this.githubUserId = githubUserId; }

    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }

    public String getGithubName() { return githubName; }
    public void setGithubName(String githubName) { this.githubName = githubName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getHtmlUrl() { return htmlUrl; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public Integer getFollowers() { return followers; }
    public void setFollowers(Integer followers) { this.followers = followers; }

    public Integer getFollowing() { return following; }
    public void setFollowing(Integer following) { this.following = following; }

    public Integer getPublicRepos() { return publicRepos; }
    public void setPublicRepos(Integer publicRepos) { this.publicRepos = publicRepos; }

    public LocalDateTime getConnectedAt() { return connectedAt; }
    public void setConnectedAt(LocalDateTime connectedAt) { this.connectedAt = connectedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    void touchUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}

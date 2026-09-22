package com.jobgenie.jobgenie_backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobgenie.jobgenie_backend.config.JwtService;
import com.jobgenie.jobgenie_backend.dto.GitHubContributionCalendarDto;
import com.jobgenie.jobgenie_backend.dto.GitHubLanguageDto;
import com.jobgenie.jobgenie_backend.dto.GitHubProfileDto;
import com.jobgenie.jobgenie_backend.dto.GitHubRepoDto;
import com.jobgenie.jobgenie_backend.model.GitHubConnection;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.repository.GitHubConnectionRepository;
import com.jobgenie.jobgenie_backend.repository.UserRepository;

@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);

    @Value("${app.github.client-id:}")
    private String clientId;

    @Value("${app.github.client-secret:}")
    private String clientSecret;

    @Value("${app.github.redirect-uri:http://localhost:8080/api/github/callback}")
    private String redirectUri;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final GitHubConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> LANGUAGE_COLORS = Map.ofEntries(
            Map.entry("JavaScript", "#F7DF1E"),
            Map.entry("TypeScript", "#3178C6"),
            Map.entry("Python", "#3776AB"),
            Map.entry("Java", "#B07219"),
            Map.entry("C++", "#F34B7D"),
            Map.entry("C#", "#178600"),
            Map.entry("Go", "#00ADD8"),
            Map.entry("Rust", "#DEA584"),
            Map.entry("HTML", "#E34C26"),
            Map.entry("CSS", "#1572B6"),
            Map.entry("PHP", "#4F5D95"),
            Map.entry("Ruby", "#701516"),
            Map.entry("Kotlin", "#A97BFF"),
            Map.entry("Swift", "#F05138")
    );

    public GitHubService(GitHubConnectionRepository connectionRepository,
                         UserRepository userRepository,
                         JwtService jwtService,
                         ObjectMapper objectMapper) {
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String generateConnectUrl(User user) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("GitHub Client ID is not configured on the server.");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("purpose", "github_oauth");
        String stateToken = jwtService.generateToken(claims, user);

        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode("read:user repo", StandardCharsets.UTF_8) +
                "&state=" + URLEncoder.encode(stateToken, StandardCharsets.UTF_8);
    }

    @Transactional
    public String handleOAuthCallback(String code, String state) {
        String targetFrontendUrl = frontendUrl.replaceAll("/+$", "") + "/github";

        try {
            if (state == null || state.isBlank()) {
                return targetFrontendUrl + "?error=" + URLEncoder.encode("Missing state parameter", StandardCharsets.UTF_8);
            }

            String userEmail = jwtService.extractUsername(state);
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found for state token."));

            if (!jwtService.isTokenValid(state, user)) {
                return targetFrontendUrl + "?error=" + URLEncoder.encode("Invalid or expired OAuth state token", StandardCharsets.UTF_8);
            }

            String accessToken = exchangeCodeForAccessToken(code);
            if (accessToken == null || accessToken.isBlank()) {
                return targetFrontendUrl + "?error=" + URLEncoder.encode("Failed to obtain GitHub access token", StandardCharsets.UTF_8);
            }

            JsonNode userInfo = fetchGitHubUser(accessToken);
            if (userInfo == null) {
                return targetFrontendUrl + "?error=" + URLEncoder.encode("Failed to fetch GitHub profile info", StandardCharsets.UTF_8);
            }

            Long githubUserId = userInfo.hasNonNull("id") ? userInfo.get("id").asLong() : null;
            String githubUsername = userInfo.hasNonNull("login") ? userInfo.get("login").asText() : "";
            String githubName = userInfo.hasNonNull("name") ? userInfo.get("name").asText() : githubUsername;
            String avatarUrl = userInfo.hasNonNull("avatar_url") ? userInfo.get("avatar_url").asText() : null;
            String bio = userInfo.hasNonNull("bio") ? userInfo.get("bio").asText() : "";
            String htmlUrl = userInfo.hasNonNull("html_url") ? userInfo.get("html_url").asText() : "";
            int followers = userInfo.hasNonNull("followers") ? userInfo.get("followers").asInt() : 0;
            int following = userInfo.hasNonNull("following") ? userInfo.get("following").asInt() : 0;
            int publicRepos = userInfo.hasNonNull("public_repos") ? userInfo.get("public_repos").asInt() : 0;

            Optional<GitHubConnection> existingConn = connectionRepository.findByUser(user);
            GitHubConnection conn;
            if (existingConn.isPresent()) {
                conn = existingConn.get();
                conn.setGithubUserId(githubUserId);
                conn.setGithubUsername(githubUsername);
                conn.setGithubName(githubName);
                conn.setAvatarUrl(avatarUrl);
                conn.setBio(bio);
                conn.setHtmlUrl(htmlUrl);
                conn.setAccessToken(accessToken);
                conn.setFollowers(followers);
                conn.setFollowing(following);
                conn.setPublicRepos(publicRepos);
            } else {
                conn = new GitHubConnection(
                        user, githubUserId, githubUsername, githubName, avatarUrl,
                        bio, htmlUrl, accessToken, followers, following, publicRepos
                );
            }
            connectionRepository.save(conn);

            return targetFrontendUrl + "?connected=true";

        } catch (Exception e) {
            log.error("Error processing GitHub OAuth callback", e);
            return targetFrontendUrl + "?error=" + URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "OAuth processing error", StandardCharsets.UTF_8);
        }
    }

    public GitHubProfileDto getProfile(User user) {
        Optional<GitHubConnection> optionalConn = connectionRepository.findByUser(user);
        if (optionalConn.isEmpty()) {
            return GitHubProfileDto.disconnected();
        }

        GitHubConnection conn = optionalConn.get();
        GitHubProfileDto dto = new GitHubProfileDto();
        dto.setConnected(true);
        dto.setUsername(conn.getGithubUsername());
        dto.setName(conn.getGithubName() != null && !conn.getGithubName().isBlank() ? conn.getGithubName() : conn.getGithubUsername());
        dto.setAvatar(conn.getAvatarUrl());
        dto.setBio(conn.getBio());
        dto.setProfileUrl(conn.getHtmlUrl());
        dto.setFollowers(conn.getFollowers());
        dto.setFollowing(conn.getFollowing());
        dto.setPublicRepos(conn.getPublicRepos());

        try {
            List<JsonNode> reposList = fetchUserRepositories(conn.getAccessToken());
            int totalStars = 0;
            int totalForks = 0;
            Map<String, Integer> langCount = new HashMap<>();
            List<GitHubRepoDto> repoDtos = new ArrayList<>();

            for (JsonNode repo : reposList) {
                int stars = repo.hasNonNull("stargazers_count") ? repo.get("stargazers_count").asInt() : 0;
                int forks = repo.hasNonNull("forks_count") ? repo.get("forks_count").asInt() : 0;
                totalStars += stars;
                totalForks += forks;

                String lang = repo.hasNonNull("language") ? repo.get("language").asText() : null;
                if (lang != null && !lang.isBlank()) {
                    langCount.put(lang, langCount.getOrDefault(lang, 0) + 1);
                }

                List<String> topics = new ArrayList<>();
                if (repo.has("topics") && repo.get("topics").isArray()) {
                    for (JsonNode topicNode : repo.get("topics")) {
                        topics.add(topicNode.asText());
                    }
                }

                String name = repo.hasNonNull("name") ? repo.get("name").asText() : "";
                String desc = repo.hasNonNull("description") ? repo.get("description").asText() : "";
                String url = repo.hasNonNull("html_url") ? repo.get("html_url").asText() : "";
                String updatedAt = repo.hasNonNull("updated_at") ? repo.get("updated_at").asText() : "";
                Long repoId = repo.hasNonNull("id") ? repo.get("id").asLong() : 0L;

                repoDtos.add(new GitHubRepoDto(repoId, name, desc, lang != null ? lang : "Other", stars, forks, updatedAt, topics, url));
            }

            dto.setTotalStars(totalStars);
            dto.setTotalForks(totalForks);

            // Sort top repos by stars / recency
            repoDtos.sort((a, b) -> Integer.compare(b.getStars(), a.getStars()));
            dto.setRepos(repoDtos.stream().limit(6).collect(Collectors.toList()));

            // Languages calculation
            int totalLangRepos = langCount.values().stream().mapToInt(Integer::intValue).sum();
            List<GitHubLanguageDto> languages = new ArrayList<>();
            if (totalLangRepos > 0) {
                for (Map.Entry<String, Integer> entry : langCount.entrySet()) {
                    double percentage = Math.round((entry.getValue() * 100.0 / totalLangRepos) * 10.0) / 10.0;
                    String color = LANGUAGE_COLORS.getOrDefault(entry.getKey(), "#94A3B8");
                    languages.add(new GitHubLanguageDto(entry.getKey(), percentage, color));
                }
                languages.sort((a, b) -> Double.compare(b.getPercentage(), a.getPercentage()));
            }
            dto.setLanguages(languages);

            // Fetch Real Contribution Calendar via GitHub GraphQL API
            GitHubContributionCalendarDto contributionCalendar = fetchContributionCalendar(conn.getAccessToken());
            if (contributionCalendar != null) {
                dto.setContributionCalendar(contributionCalendar);
                dto.setContributions(contributionCalendar.getTotalContributions());
            } else {
                dto.setContributions(0);
            }

        } catch (Exception e) {
            log.error("Failed to load GitHub repos or contribution calendar for user {}", conn.getGithubUsername(), e);
            dto.setTotalStars(0);
            dto.setTotalForks(0);
            dto.setContributions(0);
            dto.setLanguages(Collections.emptyList());
            dto.setRepos(Collections.emptyList());
        }

        return dto;
    }

    @Transactional
    public void disconnect(User user) {
        connectionRepository.deleteByUser(user);
    }

    private String exchangeCodeForAccessToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("client_id", clientId);
            requestBody.put("client_secret", clientSecret);
            requestBody.put("code", code);
            requestBody.put("redirect_uri", redirectUri);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://github.com/login/oauth/access_token",
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.hasNonNull("access_token")) {
                    return root.get("access_token").asText();
                }
            }
        } catch (Exception e) {
            log.error("Failed to exchange code for GitHub access token", e);
        }
        return null;
    }

    private JsonNode fetchGitHubUser(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("User-Agent", "JobGenie-App");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            log.error("Error fetching user from GitHub API", e);
        }
        return null;
    }

    private List<JsonNode> fetchUserRepositories(String accessToken) {
        List<JsonNode> reposList = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("User-Agent", "JobGenie-App");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            String url = UriComponentsBuilder.fromHttpUrl("https://api.github.com/user/repos")
                    .queryParam("sort", "updated")
                    .queryParam("per_page", "100")
                    .queryParam("type", "owner")
                    .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode arr = objectMapper.readTree(response.getBody());
                if (arr.isArray()) {
                    for (JsonNode repo : arr) {
                        reposList.add(repo);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching user repos from GitHub API", e);
        }
        return reposList;
    }

    private GitHubContributionCalendarDto fetchContributionCalendar(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "JobGenie-App");

            String query = "{\"query\":\"query { viewer { contributionsCollection { contributionCalendar { totalContributions weeks { contributionDays { contributionCount date color } } } } } }\"}";
            HttpEntity<String> request = new HttpEntity<>(query, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.github.com/graphql",
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode calendarNode = root.path("data").path("viewer").path("contributionsCollection").path("contributionCalendar");

                if (!calendarNode.isMissingNode()) {
                    int totalContributions = calendarNode.path("totalContributions").asInt(0);
                    JsonNode weeksNode = calendarNode.path("weeks");
                    List<GitHubContributionCalendarDto.Week> weeks = new ArrayList<>();

                    if (weeksNode.isArray()) {
                        for (JsonNode weekNode : weeksNode) {
                            JsonNode daysNode = weekNode.path("contributionDays");
                            List<GitHubContributionCalendarDto.ContributionDay> days = new ArrayList<>();
                            if (daysNode.isArray()) {
                                for (JsonNode dayNode : daysNode) {
                                    int count = dayNode.path("contributionCount").asInt(0);
                                    String date = dayNode.path("date").asText("");
                                    String color = dayNode.path("color").asText("#ebedf0");
                                    days.add(new GitHubContributionCalendarDto.ContributionDay(count, date, color));
                                }
                            }
                            weeks.add(new GitHubContributionCalendarDto.Week(days));
                        }
                    }
                    return new GitHubContributionCalendarDto(totalContributions, weeks);
                }
            }
        } catch (Exception e) {
            log.warn("Unable to fetch GitHub GraphQL contribution calendar", e);
        }
        return null;
    }
}

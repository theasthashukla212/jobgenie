package com.jobgenie.jobgenie_backend.dto;

import java.util.List;

public class GitHubContributionCalendarDto {
    private Integer totalContributions;
    private List<Week> weeks;

    public static class Week {
        private List<ContributionDay> contributionDays;

        public Week() {}
        public Week(List<ContributionDay> contributionDays) {
            this.contributionDays = contributionDays;
        }

        public List<ContributionDay> getContributionDays() { return contributionDays; }
        public void setContributionDays(List<ContributionDay> contributionDays) { this.contributionDays = contributionDays; }
    }

    public static class ContributionDay {
        private Integer contributionCount;
        private String date;
        private String color;

        public ContributionDay() {}
        public ContributionDay(Integer contributionCount, String date, String color) {
            this.contributionCount = contributionCount;
            this.date = date;
            this.color = color;
        }

        public Integer getContributionCount() { return contributionCount; }
        public void setContributionCount(Integer contributionCount) { this.contributionCount = contributionCount; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public GitHubContributionCalendarDto() {}

    public GitHubContributionCalendarDto(Integer totalContributions, List<Week> weeks) {
        this.totalContributions = totalContributions;
        this.weeks = weeks;
    }

    public Integer getTotalContributions() { return totalContributions; }
    public void setTotalContributions(Integer totalContributions) { this.totalContributions = totalContributions; }

    public List<Week> getWeeks() { return weeks; }
    public void setWeeks(List<Week> weeks) { this.weeks = weeks; }
}

// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.model;

/**
 * מחלקת מודל: סיכום כללי של משתמש (תוצאה של שאילתת JOIN בין users ל-shots).
 */
public class UserSummary {
    private final String username;
    private final int totalShots;
    private final double averageScore;
    private final int bestScore;

    public UserSummary(String username, int totalShots, double averageScore, int bestScore) {
        this.username = username;
        this.totalShots = totalShots;
        this.averageScore = averageScore;
        this.bestScore = bestScore;
    }

    public String getUsername() { return username; }
    public int getTotalShots() { return totalShots; }
    public double getAverageScore() { return averageScore; }
    public int getBestScore() { return bestScore; }
}

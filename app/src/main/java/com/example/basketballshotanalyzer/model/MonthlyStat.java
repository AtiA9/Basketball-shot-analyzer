// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.model;

/**
 * מחלקת מודל: שורה אחת בסטטיסטיקה החודשית (תוצאה של שאילתת GROUP BY).
 */
public class MonthlyStat {
    private final String month;
    private final double averageScore;
    private final int shotCount;

    /** @param month חודש בפורמט yyyy-MM */
    public MonthlyStat(String month, double averageScore, int shotCount) {
        this.month = month;
        this.averageScore = averageScore;
        this.shotCount = shotCount;
    }

    public String getMonth() { return month; }
    public double getAverageScore() { return averageScore; }
    public int getShotCount() { return shotCount; }
}

// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.model;

/**
 * מחלקת מודל: מייצגת זריקה אחת שנותחה ונשמרה בהיסטוריה של משתמש.
 */
public class ShotData {
    private long id;
    private long userId;
    private double elbowAngle;
    private double releaseAngle;
    private int score;
    private String feedback;
    private String note;
    private long timestamp;

    /** בנאי לזריקה חדשה (לפני שמירה) - הזמן הוא עכשיו וההערה ריקה. */
    public ShotData(long userId, double elbowAngle, double releaseAngle, int score, String feedback) {
        this(-1, userId, elbowAngle, releaseAngle, score, feedback, "", System.currentTimeMillis());
    }

    /** בנאי מלא - משמש כשקוראים זריקה קיימת ממסד הנתונים. */
    public ShotData(long id, long userId, double elbowAngle, double releaseAngle,
                    int score, String feedback, String note, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.elbowAngle = elbowAngle;
        this.releaseAngle = releaseAngle;
        setScore(score);
        this.feedback = feedback;
        this.note = note;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public double getElbowAngle() { return elbowAngle; }
    public void setElbowAngle(double elbowAngle) { this.elbowAngle = elbowAngle; }

    public double getReleaseAngle() { return releaseAngle; }
    public void setReleaseAngle(double releaseAngle) { this.releaseAngle = releaseAngle; }

    public int getScore() { return score; }

    /** קובע ציון ומגביל אותו לטווח 0-100 (הגנה מפני ערכים לא חוקיים). */
    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));
    }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.model;

/**
 * מחלקת מודל: מייצגת משתמש רשום באפליקציה.
 * הסיסמה עצמה לעולם לא נשמרת - רק ה-hash שלה וה-salt (מלח) שבו השתמשנו.
 */
public class User {
    private long id;
    private String username;
    private String passwordHash;
    private String salt;

    /** בנאי למשתמש חדש שעוד לא נשמר במסד (אין לו id). */
    public User(String username, String passwordHash, String salt) {
        this(-1, username, passwordHash, salt);
    }

    /** בנאי מלא - משמש כשקוראים משתמש קיים ממסד הנתונים. */
    public User(long id, String username, String passwordHash, String salt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
}

// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * ניהול המשתמש המחובר בעזרת SharedPreferences (קובץ מפתח-ערך קטן שנשמר במכשיר).
 * שומר מי מחובר כרגע, והאם הוא ביקש "הישאר מחובר" לפעם הבאה שהאפליקציה נפתחת.
 */
public class SessionManager {

    private static final String PREFS_NAME = "session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_REMEMBER = "remember_me";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** שומר את המשתמש שהתחבר. apply() כותב לקובץ ברקע. */
    public void login(long userId, String username, boolean rememberMe) {
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putBoolean(KEY_REMEMBER, rememberMe)
                .apply();
    }

    /** מוחק את כל נתוני ההתחברות. */
    public void logout() {
        prefs.edit().clear().apply();
    }

    /** האם לדלג על מסך ההתחברות (המשתמש ביקש להישאר מחובר). */
    public boolean shouldAutoLogin() {
        return prefs.getBoolean(KEY_REMEMBER, false) && getUserId() != -1;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }
}

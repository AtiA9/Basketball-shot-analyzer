// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * כלים להרשמה והתחברות: בדיקת קלט והצפנה חד-כיוונית (hash) של סיסמאות.
 * את ה-hash אי אפשר להפוך חזרה לסיסמה. בהתחברות מחשבים hash מחדש ומשווים.
 */
public final class AuthUtils {

    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 6;
    private static final int SALT_BYTES = 16;

    private AuthUtils() { }

    /** שם משתמש תקין: לפחות 3 תווים, רק אותיות אנגליות, ספרות וקו תחתון. */
    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= MIN_USERNAME_LENGTH
                && username.matches("[A-Za-z0-9_]+");
    }

    /** סיסמה תקינה: לפחות 6 תווים, ולפחות ספרה אחת. */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH
                && password.matches(".*[0-9].*");
    }

    /**
     * יוצר salt אקראי: מחרוזת שמצורפת לסיסמה לפני ה-hash.
     * כך לשני משתמשים עם אותה סיסמה יהיה hash שונה.
     */
    public static String generateSalt() {
        byte[] bytes = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(bytes);
        return toHex(bytes);
    }

    /** מחשב SHA-256 של (salt + סיסמה) ומחזיר אותו כמחרוזת הקסדצימלית. */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** בודק סיסמה: מחשב hash עם אותו salt ומשווה ל-hash השמור. */
    public static boolean verifyPassword(String password, String salt, String storedHash) {
        return hashPassword(password, salt).equals(storedHash);
    }

    /** ממיר מערך בתים למחרוזת הקסדצימלית (כל בית = 2 תווים). */
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

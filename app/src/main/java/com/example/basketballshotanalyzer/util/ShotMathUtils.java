// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.util;

/**
 * פונקציות מתמטיות לניתוח זריקה: חישוב זוויות, ציון ומשוב.
 * מחלקה "טהורה" - לא תלויה באנדרואיד, ולכן קל לבדוק ולהסביר אותה.
 *
 * שימו לב: במסך/בתמונה ציר ה-Y גדל כלפי מטה (y=0 בראש התמונה).
 */
public final class ShotMathUtils {

    /** זווית השחרור האידיאלית (במעלות) שאליה משווים. */
    public static final double IDEAL_RELEASE_ANGLE = 48.5;
    /** כמה נקודות מורידים על כל מעלה של סטייה מהאידיאל. */
    public static final double POINTS_PER_DEGREE = 2.0;
    /** סטייה (במעלות) שעד אליה הזריקה נחשבת "בטווח הטוב". */
    public static final double GOOD_RANGE = 5.0;
    /** מתחת לזווית מרפק זו היד לא יושרה מספיק ברגע השחרור. */
    public static final double MIN_ELBOW_EXTENSION = 150.0;

    private ShotMathUtils() { }

    /**
     * מחשב את הזווית בנקודה B שנוצרת בין הקטעים BA ו-BC (למשל כתף-מרפק-שורש כף יד).
     * Math.atan2(dy, dx) מחזיר את כיוון הווקטור ברדיאנים (בין -π ל-π).
     * ההפרש בין שני הכיוונים הוא הזווית ביניהם. ממירים למעלות ומחזירים ערך בין 0 ל-180.
     */
    public static double angleAtPoint(double ax, double ay, double bx, double by,
                                      double cx, double cy) {
        double angleToA = Math.atan2(ay - by, ax - bx);
        double angleToC = Math.atan2(cy - by, cx - bx);
        double degrees = Math.abs(Math.toDegrees(angleToA - angleToC));
        return degrees > 180 ? 360 - degrees : degrees;
    }

    /**
     * זווית השחרור: הזווית בין האמה (מרפק אל שורש כף היד) לבין הקו האופקי.
     * הופכים את סימן dy כי ב-Y של התמונה "למעלה" הוא ערך קטן יותר.
     * Math.abs על dx כדי שהתוצאה לא תלויה בכיוון שאליו השחקן פונה (ימין/שמאל).
     */
    public static double releaseAngle(double elbowX, double elbowY, double wristX, double wristY) {
        double dx = Math.abs(wristX - elbowX);
        double dy = elbowY - wristY;
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    /** כמה מעלות הזווית רחוקה מהאידיאל (חיובי = גבוה מדי, שלילי = נמוך מדי). */
    public static double deviationFromIdeal(double releaseAngle) {
        return releaseAngle - IDEAL_RELEASE_ANGLE;
    }

    /**
     * נוסחת הציון: score = 100 - |סטייה| × 2, מוגבל לטווח 0-100.
     * לדוגמה: זווית 53.5 → סטייה 5 → ציון 90.
     */
    public static int calculateScore(double releaseAngle) {
        double deviation = Math.abs(deviationFromIdeal(releaseAngle));
        long score = Math.round(100 - deviation * POINTS_PER_DEGREE);
        return (int) Math.max(0, Math.min(100, score));
    }

    /** יוצר משוב טקסטואלי בעברית לפי זווית השחרור וזווית המרפק. */
    public static String generateFeedback(double releaseAngle, double elbowAngle) {
        double deviation = deviationFromIdeal(releaseAngle);
        StringBuilder feedback = new StringBuilder();
        if (deviation < -GOOD_RANGE) {
            feedback.append("זווית השחרור נמוכה מדי - נסה לשחרר את הכדור בקשת גבוהה יותר.");
        } else if (deviation > GOOD_RANGE) {
            feedback.append("זווית השחרור גבוהה מדי - כוון יותר קדימה אל הסל.");
        } else {
            feedback.append("זווית שחרור מצוינת! המשך כך.");
        }
        if (elbowAngle < MIN_ELBOW_EXTENSION) {
            feedback.append("\nהמרפק לא מיושר עד הסוף ברגע השחרור - סיים את התנועה עם יד ישרה.");
        }
        return feedback.toString();
    }
}

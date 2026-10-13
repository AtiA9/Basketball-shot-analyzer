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
    /** נטיית גוף (מעלות מהקו האנכי) שעד אליה הגוף נחשב יציב. */
    public static final double MAX_TORSO_LEAN = 10.0;
    /** זווית מרפק מינימלית בסוף התנועה - היד צריכה להישאר כמעט ישרה (follow-through). */
    public static final double MIN_FOLLOW_THROUGH = 160.0;
    /** משקלות הציון המשוקלל (סכומם 1): זווית שחרור, יציבות גוף, המשך תנועה. */
    public static final double RELEASE_WEIGHT = 0.6;
    public static final double STABILITY_WEIGHT = 0.2;
    public static final double FOLLOW_WEIGHT = 0.2;

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
     * נטיית הגוף: הזווית בין קו הגו (ירך אל כתף) לבין הקו האנכי. 0 = גוף זקוף לגמרי.
     * כאן atan2(dx, dy) ולא atan2(dy, dx), כי מודדים מול האנך ולא מול האופק.
     */
    public static double torsoLean(double shoulderX, double shoulderY, double hipX, double hipY) {
        double dx = Math.abs(shoulderX - hipX);
        double dy = hipY - shoulderY; // הירך מתחת לכתף, לכן dy חיובי
        return Math.toDegrees(Math.atan2(dx, dy));
    }

    /**
     * ציון זווית השחרור: score = 100 - |סטייה| × 2, מוגבל לטווח 0-100.
     * לדוגמה: זווית 53.5 → סטייה 5 → ציון 90.
     */
    public static int calculateScore(double releaseAngle) {
        return partScore(Math.abs(deviationFromIdeal(releaseAngle)));
    }

    /**
     * ציון משוקלל: 60% זווית שחרור + 20% יציבות גוף + 20% המשך תנועה.
     * בכל רכיב מורידים 2 נקודות על כל מעלה מעבר לטווח המותר.
     * torsoLean = NaN אם הירך לא זוהתה - אז לא מורידים נקודות על יציבות.
     */
    public static int calculateTotalScore(double releaseAngle, double torsoLean, double followThroughAngle) {
        int stability = Double.isNaN(torsoLean) ? 100 : partScore(torsoLean - MAX_TORSO_LEAN);
        int follow = partScore(MIN_FOLLOW_THROUGH - followThroughAngle);
        double total = RELEASE_WEIGHT * calculateScore(releaseAngle)
                + STABILITY_WEIGHT * stability + FOLLOW_WEIGHT * follow;
        return (int) Math.round(total);
    }

    /** ציון של רכיב אחד: 100 פחות 2 נקודות לכל מעלת חריגה (חריגה שלילית = אין קנס), בטווח 0-100. */
    private static int partScore(double degreesOver) {
        long score = Math.round(100 - Math.max(0, degreesOver) * POINTS_PER_DEGREE);
        return (int) Math.max(0, Math.min(100, score));
    }

    /** יוצר משוב טקסטואלי בעברית לפי זווית השחרור, נטיית הגוף והמשך התנועה. */
    public static String generateFeedback(double releaseAngle, double torsoLean, double followThroughAngle) {
        double deviation = deviationFromIdeal(releaseAngle);
        StringBuilder feedback = new StringBuilder();
        if (deviation < -GOOD_RANGE) {
            feedback.append("זווית השחרור נמוכה מדי - נסה לשחרר את הכדור בקשת גבוהה יותר.");
        } else if (deviation > GOOD_RANGE) {
            feedback.append("זווית השחרור גבוהה מדי - כוון יותר קדימה אל הסל.");
        } else {
            feedback.append("זווית שחרור מצוינת! המשך כך.");
        }
        if (torsoLean > MAX_TORSO_LEAN) {
            feedback.append("\nהגוף נוטה בזמן הזריקה - נסה לקפוץ ישר למעלה ולנחות באותה נקודה.");
        }
        if (followThroughAngle < MIN_FOLLOW_THROUGH) {
            feedback.append("\nהיד לא נשארה ישרה אחרי השחרור - סיים את התנועה עם יד מושטת אל הסל.");
        }
        return feedback.toString();
    }
}

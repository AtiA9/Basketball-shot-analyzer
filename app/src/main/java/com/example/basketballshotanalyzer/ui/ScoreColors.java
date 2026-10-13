// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.ui;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.basketballshotanalyzer.R;

/**
 * בוחר צבע לציון, כדי שכל המסכים יציגו ציונים באותם צבעים:
 * ירוק כהה = טוב, ברונזה = בינוני, בורדו = צריך שיפור (גוונים מאופקים).
 */
public final class ScoreColors {

    /** מהציון הזה ומעלה הזריקה נחשבת טובה. */
    public static final int GOOD_SCORE = 80;
    /** מהציון הזה ומעלה הזריקה נחשבת בינונית. */
    public static final int MEDIUM_SCORE = 60;

    private ScoreColors() { }

    /** מחזיר את הצבע (כמספר int של אנדרואיד) שמתאים לציון. */
    public static int forScore(Context context, int score) {
        int colorRes;
        if (score >= GOOD_SCORE) {
            colorRes = R.color.score_good;
        } else if (score >= MEDIUM_SCORE) {
            colorRes = R.color.score_medium;
        } else {
            colorRes = R.color.score_bad;
        }
        return ContextCompat.getColor(context, colorRes);
    }
}

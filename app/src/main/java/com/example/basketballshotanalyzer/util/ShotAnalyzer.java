// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.List;

/**
 * "מנוע" הניתוח: מחלץ פריימים מהסרטון, מזהה בכל פריים את תנוחת הגוף עם ML Kit,
 * בוחר את רגע השחרור ומחשב זוויות, ציון ומשוב.
 * חשוב: analyze() איטית וחוסמת, ולכן מותר לקרוא לה רק מ-thread ברקע!
 */
public class ShotAnalyzer {

    /** אורך הסרטון המקסימלי שמנותח (דקה). מה שאחרי זה לא נבדק. */
    private static final long MAX_DURATION_MS = 60_000;
    /** המרווח בין פריימים בחיפוש הגס (4 פריימים בשנייה) - פשרה בין דיוק למהירות. */
    private static final long COARSE_STEP_MS = 250;
    /** רוחב מקסימלי לפריים - תמונה קטנה יותר = זיהוי מהיר יותר. */
    private static final int MAX_FRAME_WIDTH = 640;
    /** סף ביטחון מינימלי של ML Kit שנקודה באמת נראית בתמונה. */
    private static final float MIN_LIKELIHOOD = 0.5f;
    /** כמה זמן (מילישניות) לפני שיא התנועה מחפשים את רגע השחרור. */
    private static final long RELEASE_WINDOW_MS = 500;
    /** המרווח בין פריימים בחיפוש המדויק (בערך 30 פריימים בשנייה). */
    private static final long FINE_STEP_MS = 33;

    /** תוצאת ניתוח: הפריים של רגע השחרור + כל מה שחושב עליו. */
    public static class Result {
        public Bitmap frame;
        public long timeMs;
        public boolean rightHand;
        public List<PoseLandmark> landmarks;
        public PointF shoulder, elbow, wrist;
        /** הירך בצד של יד הזריקה, או null אם לא זוהתה. */
        public PointF hip;
        public double elbowAngle, releaseAngle;
        /** נטיית הגוף ברגע השחרור (NaN אם הירך לא זוהתה). */
        public double torsoLean;
        /** זווית המרפק בסוף התנועה - בדיקת ה-follow-through. */
        public double followThroughAngle;
        public int score;
        public String feedback;
    }

    private final PoseDetector detector;

    public ShotAnalyzer() {
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build();
        detector = PoseDetection.getClient(options);
    }

    /**
     * מנתח סרטון ומחזיר את התוצאה ברגע שלפני שהכדור עוזב את היד,
     * או null אם לא זוהה שחקן באף פריים.
     */
    public Result analyze(Context context, Uri videoUri) throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            long durationMs = Math.min(getDurationMs(retriever), MAX_DURATION_MS);
            Result peak = findPeak(retriever, durationMs);
            if (peak == null) {
                return null;
            }
            Result release = findRelease(retriever, peak);
            fillScore(release, peak);
            return release;
        } finally {
            retriever.release();
        }
    }

    /**
     * שלב 1 (חיפוש גס): דוגם פריים כל COARSE_STEP_MS לאורך הסרטון ומוצא את שיא התנועה -
     * הפריים שבו שורש כף היד הכי גבוה מעל הכתף. זה סוף התנועה, כבר אחרי השחרור.
     */
    private Result findPeak(MediaMetadataRetriever retriever, long durationMs) throws Exception {
        Result peak = null;
        for (long t = 0; t <= durationMs; t += COARSE_STEP_MS) {
            Result candidate = analyzeAt(retriever, t, null);
            if (candidate != null && (peak == null || handHeight(candidate) > handHeight(peak))) {
                peak = candidate;
            }
        }
        return peak;
    }

    /**
     * שלב 2 (חיפוש מדויק): סורק פריים-פריים את חצי השנייה שלפני השיא.
     * הכדור עוזב את היד כשהיד נעה למעלה הכי מהר - אחרי זה היד מאטה והכדור ממשיך לבד.
     * לכן מחפשים את זוג הפריימים העוקבים שביניהם היד עלתה הכי הרבה (יחסית לכתף),
     * ומחזירים את הראשון מביניהם: הרגע האחרון שבו הכדור עדיין ביד.
     * בודקים רק תנועה שמסתיימת מעל הכתף, כדי לא לבלבל עם הרמת הכדור מהמותן.
     */
    private Result findRelease(MediaMetadataRetriever retriever, Result peak) throws Exception {
        Result release = peak;
        Result previous = null;
        double maxRise = 0;
        for (long t = Math.max(0, peak.timeMs - RELEASE_WINDOW_MS); t <= peak.timeMs; t += FINE_STEP_MS) {
            Result current = analyzeAt(retriever, t, peak.rightHand);
            if (current != null && previous != null && handHeight(current) > 0) {
                double rise = handHeight(current) - handHeight(previous);
                if (rise > maxRise) {
                    maxRise = rise;
                    release = previous;
                }
            }
            previous = current;
        }
        return release;
    }

    /**
     * גובה שורש כף היד מעל הכתף, בפיקסלים (חיובי = היד מעל הכתף).
     * מודדים יחסית לכתף ולא לראש התמונה, כדי שהקפיצה של כל הגוף לא תיחשב כתנועת יד.
     */
    private double handHeight(Result r) {
        return r.shoulder.y - r.wrist.y;
    }

    /** מחלץ את הפריים בזמן הנתון (מילישניות) ומריץ עליו זיהוי תנוחה. */
    private Result analyzeAt(MediaMetadataRetriever retriever, long timeMs, Boolean rightHand)
            throws Exception {
        Bitmap frame = retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        if (frame == null) {
            return null;
        }
        Result result = analyzeFrame(scaleDown(frame), rightHand);
        if (result != null) {
            result.timeMs = timeMs;
        }
        return result;
    }

    /** משחרר את משאבי ML Kit. */
    public void close() {
        detector.close();
    }

    /**
     * מריץ זיהוי תנוחה על פריים אחד ומחזיר את נקודות יד הזריקה (או null).
     * rightHand = איזו יד לבדוק; null = לבחור לבד (היד ששורש כף היד שלה גבוה יותר).
     */
    private Result analyzeFrame(Bitmap frame, Boolean rightHand) throws Exception {
        // Tasks.await מחכה לתוצאה של ML Kit באותו thread (מותר רק ברקע)
        Pose pose = Tasks.await(detector.process(InputImage.fromBitmap(frame, 0)));
        PoseLandmark right = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark left = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        boolean useRight;
        if (rightHand != null) {
            useRight = rightHand; // בחיפוש המדויק נשארים עם אותה יד שנמצאה בשיא
        } else {
            useRight = !isVisible(left)
                    || (isVisible(right) && right.getPosition().y < left.getPosition().y);
        }
        PoseLandmark shoulder = pose.getPoseLandmark(useRight ? PoseLandmark.RIGHT_SHOULDER : PoseLandmark.LEFT_SHOULDER);
        PoseLandmark elbow = pose.getPoseLandmark(useRight ? PoseLandmark.RIGHT_ELBOW : PoseLandmark.LEFT_ELBOW);
        PoseLandmark wrist = useRight ? right : left;
        if (!isVisible(wrist) || !isVisible(shoulder) || !isVisible(elbow)) {
            return null;
        }
        PoseLandmark hip = pose.getPoseLandmark(useRight ? PoseLandmark.RIGHT_HIP : PoseLandmark.LEFT_HIP);
        Result result = new Result();
        result.frame = frame;
        result.rightHand = useRight;
        result.landmarks = pose.getAllPoseLandmarks();
        result.shoulder = shoulder.getPosition();
        result.elbow = elbow.getPosition();
        result.wrist = wrist.getPosition();
        result.hip = isVisible(hip) ? hip.getPosition() : null; // הירך לא חובה
        return result;
    }

    /** מחשב זוויות, ציון ומשוב בעזרת ShotMathUtils. peak = הפריים של סוף התנועה. */
    private void fillScore(Result r, Result peak) {
        r.elbowAngle = elbowAngle(r);
        r.releaseAngle = ShotMathUtils.releaseAngle(r.elbow.x, r.elbow.y, r.wrist.x, r.wrist.y);
        r.torsoLean = r.hip == null ? Double.NaN
                : ShotMathUtils.torsoLean(r.shoulder.x, r.shoulder.y, r.hip.x, r.hip.y);
        r.followThroughAngle = elbowAngle(peak);
        r.score = ShotMathUtils.calculateTotalScore(r.releaseAngle, r.torsoLean, r.followThroughAngle);
        r.feedback = ShotMathUtils.generateFeedback(r.releaseAngle, r.torsoLean, r.followThroughAngle);
    }

    /** זווית המרפק (כתף-מרפק-שורש כף יד) בפריים נתון. */
    private double elbowAngle(Result r) {
        return ShotMathUtils.angleAtPoint(r.shoulder.x, r.shoulder.y,
                r.elbow.x, r.elbow.y, r.wrist.x, r.wrist.y);
    }

    private boolean isVisible(PoseLandmark landmark) {
        return landmark != null && landmark.getInFrameLikelihood() >= MIN_LIKELIHOOD;
    }

    /** קורא את אורך הסרטון במילישניות. */
    private long getDurationMs(MediaMetadataRetriever retriever) {
        String value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return value == null ? 0 : Long.parseLong(value);
    }

    /** מקטין את הפריים לרוחב מקסימלי, תוך שמירה על יחס גובה-רוחב. */
    private Bitmap scaleDown(Bitmap frame) {
        if (frame.getWidth() <= MAX_FRAME_WIDTH) {
            return frame;
        }
        int height = frame.getHeight() * MAX_FRAME_WIDTH / frame.getWidth();
        return Bitmap.createScaledBitmap(frame, MAX_FRAME_WIDTH, height, true);
    }
}

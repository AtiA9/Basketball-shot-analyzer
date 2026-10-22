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

import java.util.ArrayList;
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
    /** כמה זמן (מילישניות) לפני שיא התנועה מתחילים לחפש את רגע השחרור. */
    private static final long RELEASE_WINDOW_MS = 700;
    /** כמה זמן אחרי השיא ממשיכים לחפש (החיפוש הגס דוגם רק כל 250ms, אז השיא האמיתי יכול להיות קצת אחריו). */
    private static final long AFTER_PEAK_MS = 250;
    /** המרווח בין פריימים בחיפוש המדויק (בערך 30 פריימים בשנייה). */
    private static final long FINE_STEP_MS = 33;
    /** בתיקון ידני: כמה פריימים לכל היותר לדלג אם בפריים אין זיהוי של היד. */
    private static final int MAX_STEP_TRIES = 3;
    /** תחילת "היד למעלה": הדגימה הראשונה שבה היד כבר בגובה של לפחות 90% מהשיא. */
    private static final double PLATEAU_RATIO = 0.9;
    /** אורך ירך מינימלי ביחס לאורך הגו - גוף של אדם אמיתי (ולא, למשל, רשת של סל). */
    private static final double MIN_THIGH_TO_TORSO = 0.5;

    /** תוצאת ניתוח: הפריים של רגע השחרור + כל מה שחושב עליו. */
    public static class Result {
        public Bitmap frame;
        public long timeMs;
        public boolean rightHand;
        public List<PoseLandmark> landmarks;
        public PointF shoulder, elbow, wrist;
        /** הירך בצד של יד הזריקה (חייבת להיות מזוהה, משמשת לחישוב גובה היד ונטיית הגוף). */
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
     * מנתח סרטון ומחזיר את התוצאה ברגע שהכדור עוזב את היד,
     * או null אם לא זוהה שחקן באף פריים.
     */
    public Result analyze(Context context, Uri videoUri) throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            long durationMs = Math.min(getDurationMs(retriever), MAX_DURATION_MS);
            List<Result> samples = coarseSamples(retriever, durationMs);
            int top = highestHandIndex(samples);
            if (top == -1) {
                return null;
            }
            Result start = samples.get(plateauStartIndex(samples, top));
            Result release = findRelease(retriever, start, durationMs);
            fillScore(release, elbowAngle(samples.get(top)));
            return release;
        } finally {
            retriever.release();
        }
    }

    /**
     * שלב 1 (חיפוש גס): דוגם פריים כל COARSE_STEP_MS לאורך הסרטון.
     * מכל דגימה שומרים רק את נקודות הגוף, לא את התמונה, כדי לא למלא את הזיכרון בסרטון ארוך.
     * במקום שבו לא זוהה שחקן נשמר null.
     */
    private List<Result> coarseSamples(MediaMetadataRetriever retriever, long durationMs) throws Exception {
        List<Result> samples = new ArrayList<>();
        for (long t = 0; t <= durationMs; t += COARSE_STEP_MS) {
            Result r = analyzeAt(retriever, t, null);
            if (r != null) {
                r.frame = null;
            }
            samples.add(r);
        }
        return samples;
    }

    /** האינדקס של הדגימה שבה היד הכי גבוהה (סוף התנועה, אחרי השחרור), או -1 אם אין אף זיהוי. */
    private int highestHandIndex(List<Result> samples) {
        int best = -1;
        for (int i = 0; i < samples.size(); i++) {
            Result r = samples.get(i);
            if (r != null && (best == -1 || handHeight(r) > handHeight(samples.get(best)))) {
                best = i;
            }
        }
        return best;
    }

    /**
     * הרבה שחקנים מחזיקים את היד למעלה אחרי הזריקה (follow-through), ולכן השיא יכול להגיע
     * שנייה אחרי שהכדור כבר עזב. הולכים אחורה מהשיא כל עוד היד עדיין בגובה של לפחות 90% ממנו,
     * ומחזירים את הדגימה הראשונה של "היד למעלה". נעצרים בדגימה נמוכה - שם מתחילה זריקה אחרת.
     */
    private int plateauStartIndex(List<Result> samples, int top) {
        double peakHeight = handHeight(samples.get(top));
        int start = top;
        for (int i = top - 1; i >= 0; i--) {
            Result r = samples.get(i);
            if (r == null) {
                continue;
            }
            if (handHeight(r) < PLATEAU_RATIO * peakHeight) {
                break;
            }
            start = i;
        }
        return start;
    }

    /**
     * שלב 2 (חיפוש מדויק): סורק פריים-פריים מ-0.7 שניות לפני השיא ועד 0.25 שניות אחריו.
     * הכדור עוזב את היד כשהמרפק מסיים להתיישר, ולכן לכל פריים שומרים את זווית המרפק,
     * ו-ShotMathUtils.releaseIndex בוחר את הפריים הראשון שבו היד כמעט ישרה.
     * בודקים רק פריימים שבהם היד מעל הכתף, כדי לא לבלבל עם הרמת הכדור מהמותן.
     * שומרים רק זמן וזווית (לא את התמונות, כדי לחסוך זיכרון), ובסוף שולפים שוב רק את פריים השחרור.
     */
    private Result findRelease(MediaMetadataRetriever retriever, Result peak, long durationMs)
            throws Exception {
        List<Long> times = new ArrayList<>();
        List<Double> angles = new ArrayList<>();
        long end = Math.min(durationMs, peak.timeMs + AFTER_PEAK_MS);
        for (long t = Math.max(0, peak.timeMs - RELEASE_WINDOW_MS); t <= end; t += FINE_STEP_MS) {
            Result frame = analyzeAt(retriever, t, peak.rightHand);
            if (frame != null && handHeight(frame) > 0) {
                times.add(t);
                angles.add(elbowAngle(frame));
            }
        }
        if (times.isEmpty()) {
            Result fallback = analyzeAt(retriever, peak.timeMs, peak.rightHand); // הדגימה בלי תמונה
            return fallback != null ? fallback : peak;
        }
        double[] elbowAngles = new double[angles.size()];
        for (int i = 0; i < elbowAngles.length; i++) {
            elbowAngles[i] = angles.get(i);
        }
        int index = ShotMathUtils.releaseIndex(elbowAngles);
        Result release = analyzeAt(retriever, times.get(index), peak.rightHand);
        return release != null ? release : analyzeAt(retriever, peak.timeMs, peak.rightHand);
    }

    /**
     * גובה שורש כף היד מעל הכתף, ביחידות של "אורך גו" (המרחק כתף-ירך). חיובי = היד מעל הכתף.
     * מודדים יחסית לכתף, כדי שהקפיצה של כל הגוף לא תיחשב כתנועת יד,
     * ומחלקים באורך הגו, כדי שצילום מקרוב (גוף גדול בתמונה) לא "ינצח" זריקה שצולמה מרחוק.
     */
    private double handHeight(Result r) {
        double torso = Math.hypot(r.shoulder.x - r.hip.x, r.shoulder.y - r.hip.y);
        return torso > 0 ? (r.shoulder.y - r.wrist.y) / torso : 0;
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
        PoseLandmark hip = pose.getPoseLandmark(useRight ? PoseLandmark.RIGHT_HIP : PoseLandmark.LEFT_HIP);
        // כל הנקודות חייבות להיות מזוהות ובתוך התמונה. כך פוסלים פריימים שבהם רק חלק מהגוף נראה
        // (למשל כשהשחקן עומד צמוד למצלמה) - שם ML Kit "מנחש" נקודות מחוץ לתמונה.
        if (!isInFrame(wrist, frame) || !isInFrame(shoulder, frame)
                || !isInFrame(elbow, frame) || !isInFrame(hip, frame)) {
            return null;
        }
        // בדיקת היגיון: ML Kit מחזיר גוף אחד לתמונה, ולפעמים "רואה" אדם במקום שאין -
        // למשל ברשת של הסל כשהכדור בתוכה. גוף אמיתי עומד: כתף מעל ירך מעל ברך, ועם רגליים באורך סביר.
        PoseLandmark knee = pose.getPoseLandmark(useRight ? PoseLandmark.RIGHT_KNEE : PoseLandmark.LEFT_KNEE);
        if (!isInFrame(knee, frame) || !isUprightBody(shoulder.getPosition(), hip.getPosition(), knee.getPosition())) {
            return null;
        }
        Result result = new Result();
        result.frame = frame;
        result.rightHand = useRight;
        result.landmarks = pose.getAllPoseLandmarks();
        result.shoulder = shoulder.getPosition();
        result.elbow = elbow.getPosition();
        result.wrist = wrist.getPosition();
        result.hip = hip.getPosition();
        return result;
    }

    /**
     * תיקון ידני של רגע השחרור: עובר פריים אחד אחורה (direction = -1) או קדימה (+1)
     * מהפריים הנוכחי, עם אותה יד, ומחשב לו מחדש זוויות, ציון ומשוב.
     * אם בפריים הזה ML Kit לא זיהה את היד, ממשיכים לפריים הבא באותו כיוון (עד MAX_STEP_TRIES).
     * ה-follow-through נשאר מהניתוח המקורי, כי הוא נמדד בסוף התנועה ולא ברגע השחרור.
     * מחזיר null אם לא נמצא פריים מתאים. איטית, ולכן רק מ-thread ברקע.
     */
    public Result stepFrame(Context context, Uri videoUri, Result current, int direction) throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            long durationMs = getDurationMs(retriever);
            for (int k = 1; k <= MAX_STEP_TRIES; k++) {
                long t = current.timeMs + direction * k * FINE_STEP_MS;
                if (t < 0 || t > durationMs) {
                    break;
                }
                Result r = analyzeAt(retriever, t, current.rightHand);
                if (r != null) {
                    fillScore(r, current.followThroughAngle);
                    return r;
                }
            }
            return null;
        } finally {
            retriever.release();
        }
    }

    /** מחשב זוויות, ציון ומשוב בעזרת ShotMathUtils. followThrough = זווית המרפק בסוף התנועה. */
    private void fillScore(Result r, double followThrough) {
        r.elbowAngle = elbowAngle(r);
        r.releaseAngle = ShotMathUtils.releaseAngle(r.elbow.x, r.elbow.y, r.wrist.x, r.wrist.y);
        r.torsoLean = r.hip == null ? Double.NaN
                : ShotMathUtils.torsoLean(r.shoulder.x, r.shoulder.y, r.hip.x, r.hip.y);
        r.followThroughAngle = followThrough;
        r.score = ShotMathUtils.calculateTotalScore(r.releaseAngle, r.torsoLean, r.followThroughAngle);
        r.feedback = ShotMathUtils.generateFeedback(r.releaseAngle, r.torsoLean, r.followThroughAngle);
    }

    /** זווית המרפק (כתף-מרפק-שורש כף יד) בפריים נתון. */
    private double elbowAngle(Result r) {
        return ShotMathUtils.angleAtPoint(r.shoulder.x, r.shoulder.y,
                r.elbow.x, r.elbow.y, r.wrist.x, r.wrist.y);
    }

    /**
     * האם הנקודות נראות כמו גוף של אדם עומד: הכתף מעל הירך, הירך מעל הברך
     * (בתמונה y גדל כלפי מטה), והירך (ירך-ברך) באורך של לפחות חצי מהגו (כתף-ירך).
     */
    private boolean isUprightBody(PointF shoulder, PointF hip, PointF knee) {
        double torso = Math.hypot(shoulder.x - hip.x, shoulder.y - hip.y);
        double thigh = Math.hypot(hip.x - knee.x, hip.y - knee.y);
        return shoulder.y < hip.y && hip.y < knee.y && thigh >= MIN_THIGH_TO_TORSO * torso;
    }

    /** נקודה מזוהה בביטחון מספיק וגם נמצאת בתוך גבולות התמונה. */
    private boolean isInFrame(PoseLandmark landmark, Bitmap frame) {
        if (!isVisible(landmark)) {
            return false;
        }
        PointF p = landmark.getPosition();
        return p.x >= 0 && p.y >= 0 && p.x < frame.getWidth() && p.y < frame.getHeight();
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

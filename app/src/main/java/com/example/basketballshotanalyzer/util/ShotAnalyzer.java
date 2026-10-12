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

    /** כמה פריימים לדגום מהסרטון (פשרה בין דיוק למהירות). */
    private static final int FRAME_COUNT = 20;
    /** רוחב מקסימלי לפריים - תמונה קטנה יותר = זיהוי מהיר יותר. */
    private static final int MAX_FRAME_WIDTH = 640;
    /** סף ביטחון מינימלי של ML Kit שנקודה באמת נראית בתמונה. */
    private static final float MIN_LIKELIHOOD = 0.5f;

    /** תוצאת ניתוח: הפריים של רגע השחרור + כל מה שחושב עליו. */
    public static class Result {
        public Bitmap frame;
        public List<PoseLandmark> landmarks;
        public PointF shoulder, elbow, wrist;
        public double elbowAngle, releaseAngle;
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
     * מנתח סרטון ומחזיר תוצאה, או null אם לא זוהה שחקן באף פריים.
     * רגע השחרור = הפריים שבו שורש כף היד של יד הזריקה הכי גבוה (Y הכי קטן).
     */
    public Result analyze(Context context, Uri videoUri) throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            long durationUs = getDurationMs(retriever) * 1000;
            Result best = null;
            for (int i = 0; i < FRAME_COUNT; i++) {
                long timeUs = durationUs * i / (FRAME_COUNT - 1);
                Bitmap frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST);
                if (frame == null) {
                    continue;
                }
                Result candidate = analyzeFrame(scaleDown(frame));
                if (candidate != null && (best == null || candidate.wrist.y < best.wrist.y)) {
                    best = candidate;
                }
            }
            if (best != null) {
                fillScore(best);
            }
            return best;
        } finally {
            retriever.release();
        }
    }

    /** משחרר את משאבי ML Kit. */
    public void close() {
        detector.close();
    }

    /** מריץ זיהוי תנוחה על פריים אחד ומחזיר את נקודות יד הזריקה (או null). */
    private Result analyzeFrame(Bitmap frame) throws Exception {
        // Tasks.await מחכה לתוצאה של ML Kit באותו thread (מותר רק ברקע)
        Pose pose = Tasks.await(detector.process(InputImage.fromBitmap(frame, 0)));
        PoseLandmark right = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark left = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        if (!isVisible(right) && !isVisible(left)) {
            return null;
        }
        // יד הזריקה = היד ששורש כף היד שלה גבוה יותר בתמונה
        boolean useRight = !isVisible(left)
                || (isVisible(right) && right.getPosition().y < left.getPosition().y);
        PoseLandmark shoulder = pose.getPoseLandmark(useRight ? PoseLandmark.RIGHT_SHOULDER : PoseLandmark.LEFT_SHOULDER);
        PoseLandmark elbow = pose.getPoseLandmark(useRight ? PoseLandmark.RIGHT_ELBOW : PoseLandmark.LEFT_ELBOW);
        PoseLandmark wrist = useRight ? right : left;
        if (!isVisible(shoulder) || !isVisible(elbow)) {
            return null;
        }
        Result result = new Result();
        result.frame = frame;
        result.landmarks = pose.getAllPoseLandmarks();
        result.shoulder = shoulder.getPosition();
        result.elbow = elbow.getPosition();
        result.wrist = wrist.getPosition();
        return result;
    }

    /** מחשב זוויות, ציון ומשוב בעזרת ShotMathUtils. */
    private void fillScore(Result r) {
        r.elbowAngle = ShotMathUtils.angleAtPoint(r.shoulder.x, r.shoulder.y,
                r.elbow.x, r.elbow.y, r.wrist.x, r.wrist.y);
        r.releaseAngle = ShotMathUtils.releaseAngle(r.elbow.x, r.elbow.y, r.wrist.x, r.wrist.y);
        r.score = ShotMathUtils.calculateScore(r.releaseAngle);
        r.feedback = ShotMathUtils.generateFeedback(r.releaseAngle, r.elbowAngle);
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

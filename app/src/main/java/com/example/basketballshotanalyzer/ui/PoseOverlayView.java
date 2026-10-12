// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.basketballshotanalyzer.R;
import com.example.basketballshotanalyzer.util.ShotAnalyzer;
import com.google.mlkit.vision.pose.PoseLandmark;

/**
 * View מותאם אישית (יורש מ-View): מצייר את פריים השחרור, ועליו את השלד -
 * נקודות הגוף, "עצמות" (קווים בין נקודות) ותוויות של הזוויות.
 * כל הציור קורה ב-onDraw שמערכת אנדרואיד קוראת לה בכל פעם שצריך לצייר את ה-View.
 */
public class PoseOverlayView extends View {

    /** זוגות נקודות שמחוברות בקו (עצם). */
    private static final int[][] BONES = {
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER},
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW},
            {PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST},
            {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW},
            {PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST},
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP},
            {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP},
            {PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP},
            {PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE},
            {PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE},
            {PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE},
            {PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE}
    };

    private final Paint bonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint armPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ShotAnalyzer.Result result;
    /** פרמטרי ההתאמה מקואורדינטות התמונה לקואורדינטות ה-View. */
    private float scale, offsetX, offsetY;

    /** הבנאי שאנדרואיד קורא לו כשה-View מוגדר בקובץ XML. */
    public PoseOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bonePaint.setColor(Color.WHITE);
        bonePaint.setStrokeWidth(5f);
        armPaint.setColor(ContextCompat.getColor(context, R.color.primary));
        armPaint.setStrokeWidth(9f);
        pointPaint.setColor(Color.YELLOW);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setShadowLayer(4f, 0f, 0f, Color.BLACK);
    }

    /** מקבל תוצאת ניתוח חדשה ומבקש ציור מחדש (invalidate גורם לקריאה ל-onDraw). */
    public void setResult(ShotAnalyzer.Result result) {
        this.result = result;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (result == null) {
            return;
        }
        drawFrame(canvas);
        for (int[] bone : BONES) {
            drawBone(canvas, bone[0], bone[1]);
        }
        drawLine(canvas, result.shoulder, result.elbow, armPaint);
        drawLine(canvas, result.elbow, result.wrist, armPaint);
        for (PoseLandmark landmark : result.landmarks) {
            canvas.drawCircle(toViewX(landmark.getPosition().x), toViewY(landmark.getPosition().y), 7f, pointPaint);
        }
        drawLabel(canvas, getContext().getString(R.string.label_elbow, result.elbowAngle), result.elbow);
        drawLabel(canvas, getContext().getString(R.string.label_release, result.releaseAngle), result.wrist);
    }

    /** מצייר את התמונה במרכז ה-View בגודל המקסימלי שנכנס, ושומר את יחס ההגדלה. */
    private void drawFrame(Canvas canvas) {
        float bw = result.frame.getWidth();
        float bh = result.frame.getHeight();
        scale = Math.min(getWidth() / bw, getHeight() / bh);
        offsetX = (getWidth() - bw * scale) / 2f;
        offsetY = (getHeight() - bh * scale) / 2f;
        RectF dest = new RectF(offsetX, offsetY, offsetX + bw * scale, offsetY + bh * scale);
        canvas.drawBitmap(result.frame, null, dest, null);
    }

    /** מצייר עצם בין שתי נקודות, אם שתיהן זוהו. */
    private void drawBone(Canvas canvas, int fromType, int toType) {
        PoseLandmark from = findLandmark(fromType);
        PoseLandmark to = findLandmark(toType);
        if (from != null && to != null) {
            drawLine(canvas, from.getPosition(), to.getPosition(), bonePaint);
        }
    }

    private void drawLine(Canvas canvas, PointF a, PointF b, Paint paint) {
        canvas.drawLine(toViewX(a.x), toViewY(a.y), toViewX(b.x), toViewY(b.y), paint);
    }

    private void drawLabel(Canvas canvas, String text, PointF at) {
        canvas.drawText(text, toViewX(at.x) + 15f, toViewY(at.y) - 15f, textPaint);
    }

    private PoseLandmark findLandmark(int type) {
        for (PoseLandmark landmark : result.landmarks) {
            if (landmark.getLandmarkType() == type) {
                return landmark;
            }
        }
        return null;
    }

    private float toViewX(float imageX) { return imageX * scale + offsetX; }
    private float toViewY(float imageY) { return imageY * scale + offsetY; }
}

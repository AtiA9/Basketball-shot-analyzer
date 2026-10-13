// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.basketballshotanalyzer.R;
import com.example.basketballshotanalyzer.util.ShotAnalyzer;
import com.google.mlkit.vision.pose.PoseLandmark;

/**
 * View מותאם אישית (יורש מ-View): מצייר את פריים השחרור, ועליו את השלד -
 * נקודות הגוף, "עצמות" (קווים בין נקודות) ותוויות של הזוויות.
 * כל הציור קורה ב-onDraw שמערכת אנדרואיד קוראת לה בכל פעם שצריך לצייר את ה-View.
 * תומך בזום: צביטה בשתי אצבעות מגדילה, גרירה מזיזה, הקשה כפולה מאפסת.
 */
public class PoseOverlayView extends View {

    /** הגדלה מקסימלית (פי 4). */
    private static final float MAX_ZOOM = 4f;

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

    /** מטריצת הזום: שומרת כמה הוגדל וכמה הוזז. מוחלת על כל הציור ב-onDraw. */
    private final Matrix zoomMatrix = new Matrix();
    private float zoom = 1f;
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;

    /** הבנאי שאנדרואיד קורא לו כשה-View מוגדר בקובץ XML. */
    public PoseOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // זיהוי צביטה (שתי אצבעות) - getScaleFactor אומר פי כמה האצבעות התרחקו
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                zoomBy(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                return true;
            }
        });
        // זיהוי גרירה (להזזת התמונה המוגדלת) והקשה כפולה (איפוס)
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
                if (zoom > 1f) {
                    zoomMatrix.postTranslate(-dx, -dy);
                    invalidate();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                resetZoom();
                return true;
            }
        });
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
        resetZoom();
    }

    /** מעביר כל נגיעה למזהי המחוות (צביטה / גרירה / הקשה כפולה). */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (result == null) {
            return false;
        }
        // בזמן זום - לא לתת ל-ScrollView של המסך "לגנוב" את הנגיעה ולגלול
        getParent().requestDisallowInterceptTouchEvent(event.getPointerCount() > 1 || zoom > 1f);
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    /** מגדיל/מקטין סביב הנקודה שבין האצבעות, בטווח 1 עד MAX_ZOOM. */
    private void zoomBy(float factor, float focusX, float focusY) {
        float newZoom = Math.max(1f, Math.min(MAX_ZOOM, zoom * factor));
        zoomMatrix.postScale(newZoom / zoom, newZoom / zoom, focusX, focusY);
        zoom = newZoom;
        if (zoom == 1f) {
            zoomMatrix.reset(); // חזרנו לגודל רגיל - מבטלים גם את ההזזה
        }
        invalidate();
    }

    private void resetZoom() {
        zoom = 1f;
        zoomMatrix.reset();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (result == null) {
            return;
        }
        canvas.save();
        canvas.concat(zoomMatrix); // כל מה שמצויר מכאן והלאה מוגדל ומוזז לפי הזום
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
        canvas.restore();
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

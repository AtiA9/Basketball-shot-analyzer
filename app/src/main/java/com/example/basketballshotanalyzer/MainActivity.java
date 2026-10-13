// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.basketballshotanalyzer.db.DatabaseHelper;
import com.example.basketballshotanalyzer.model.ShotData;
import com.example.basketballshotanalyzer.ui.PoseOverlayView;
import com.example.basketballshotanalyzer.ui.ScoreColors;
import com.example.basketballshotanalyzer.util.SessionManager;
import com.example.basketballshotanalyzer.util.ShotAnalyzer;
import com.example.basketballshotanalyzer.util.ShotMathUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * מסך ראשי: בקשת הרשאה, בחירה/צילום סרטון, הרצת הניתוח ברקע והצגת התוצאה.
 * הניתוח רץ ב-ExecutorService (thread ברקע) כדי שהמסך לא "יקפא",
 * והתוצאה חוזרת ל-thread הראשי (UI) דרך Handler.
 */
public class MainActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private SessionManager session;
    private DatabaseHelper dbHelper;
    private ShotAnalyzer analyzer;

    private View layoutProgress, cardResult;
    private PoseOverlayView poseOverlay;
    private CircularProgressIndicator scoreRing;
    private TextView textScore, textAngles, textFeedback;

    /** בקשת הרשאה בזמן ריצה. אם אושרה - פותחים את הגלריה. */
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    openGallery();
                } else {
                    showError(R.string.error_permission);
                }
            });

    /** בחירת סרטון מהגלריה. מחזיר Uri של הסרטון (או null אם בוטל). */
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::startAnalysis);

    /** צילום סרטון באפליקציית המצלמה של המכשיר. */
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    startAnalysis(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);
        analyzer = new ShotAnalyzer();
        bindViews();
        TextView textWelcome = findViewById(R.id.textWelcome);
        textWelcome.setText(getString(R.string.welcome_user, session.getUsername()));
    }

    /** מקשר בין משתני Java לרכיבים שבקובץ ה-XML ומגדיר לחיצות. */
    private void bindViews() {
        layoutProgress = findViewById(R.id.layoutProgress);
        cardResult = findViewById(R.id.cardResult);
        poseOverlay = findViewById(R.id.poseOverlay);
        scoreRing = findViewById(R.id.scoreRing);
        textScore = findViewById(R.id.textScore);
        textAngles = findViewById(R.id.textAngles);
        textFeedback = findViewById(R.id.textFeedback);
        findViewById(R.id.btnPickVideo).setOnClickListener(v -> requestVideoPermission());
        findViewById(R.id.btnRecordVideo).setOnClickListener(v -> openCamera());
        findViewById(R.id.btnHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    /** בודק הרשאה לקריאת סרטונים; ההרשאה הנדרשת שונה מאנדרואיד 13 (API 33) ומעלה. */
    private void requestVideoPermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_VIDEO
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        galleryLauncher.launch("video/*");
    }

    private void openCamera() {
        try {
            cameraLauncher.launch(new Intent(MediaStore.ACTION_VIDEO_CAPTURE));
        } catch (ActivityNotFoundException e) {
            showError(R.string.error_no_camera);
        }
    }

    /** מפעיל את הניתוח ב-thread ברקע. לעולם לא מריצים אותו על ה-thread הראשי! */
    private void startAnalysis(Uri videoUri) {
        if (videoUri == null) {
            return;
        }
        layoutProgress.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);
        executor.execute(() -> {
            try {
                ShotAnalyzer.Result result = analyzer.analyze(this, videoUri);
                if (result != null) {
                    saveShot(result);
                }
                mainHandler.post(() -> showResult(result));
            } catch (Exception e) {
                mainHandler.post(() -> showError(R.string.error_analysis));
            }
        });
    }

    /** שומר את הזריקה במסד (רץ עדיין ב-thread ברקע). */
    private void saveShot(ShotAnalyzer.Result r) {
        dbHelper.insertShot(new ShotData(session.getUserId(),
                r.elbowAngle, r.releaseAngle, r.score, r.feedback));
    }

    /** מציג את התוצאה על המסך (רץ ב-thread הראשי, דרך ה-Handler). */
    private void showResult(ShotAnalyzer.Result result) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        layoutProgress.setVisibility(View.GONE);
        if (result == null) {
            showError(R.string.error_no_pose);
            return;
        }
        poseOverlay.setResult(result);
        showScore(result.score);
        String angles = getString(R.string.result_angles, result.releaseAngle,
                ShotMathUtils.IDEAL_RELEASE_ANGLE, result.elbowAngle);
        String body = Double.isNaN(result.torsoLean)
                ? getString(R.string.result_follow_only, result.followThroughAngle)
                : getString(R.string.result_body, result.torsoLean, result.followThroughAngle);
        textAngles.setText(angles + "\n" + body);
        textFeedback.setText(result.feedback);
        cardResult.setVisibility(View.VISIBLE);
    }

    /** מציג את הציון בטבעת: אורך הטבעת = הציון, והצבע לפי רמת הציון. */
    private void showScore(int score) {
        textScore.setText(String.valueOf(score));
        scoreRing.setIndicatorColor(ScoreColors.forScore(this, score));
        scoreRing.setProgressCompat(score, true);
    }

    /** מציג הודעת שגיאה ידידותית בתחתית המסך. */
    private void showError(int messageRes) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        layoutProgress.setVisibility(View.GONE);
        Snackbar.make(findViewById(android.R.id.content), messageRes, Snackbar.LENGTH_LONG).show();
    }

    private void logout() {
        session.logout();
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    /** כשהמסך נסגר: עוצרים את ה-thread ברקע ומשחררים את ML Kit. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        analyzer.close();
    }
}

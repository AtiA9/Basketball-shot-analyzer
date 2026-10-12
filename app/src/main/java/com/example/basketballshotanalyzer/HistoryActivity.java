// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basketballshotanalyzer.db.DatabaseHelper;
import com.example.basketballshotanalyzer.model.MonthlyStat;
import com.example.basketballshotanalyzer.model.ShotData;
import com.example.basketballshotanalyzer.model.UserSummary;
import com.example.basketballshotanalyzer.ui.ShotAdapter;
import com.example.basketballshotanalyzer.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * מסך היסטוריה: סטטיסטיקה (JOIN + GROUP BY) ורשימת כל הזריקות ב-RecyclerView,
 * עם אפשרות לערוך הערה (Update) ולמחוק זריקה (Delete).
 */
public class HistoryActivity extends AppCompatActivity implements ShotAdapter.OnShotActionListener {

    private DatabaseHelper dbHelper;
    private long userId;
    private final List<ShotData> shots = new ArrayList<>();
    private ShotAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle(R.string.history_title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        dbHelper = new DatabaseHelper(this);
        userId = new SessionManager(this).getUserId();
        RecyclerView recycler = findViewById(R.id.recyclerShots);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShotAdapter(shots, this);
        recycler.setAdapter(adapter);
        refresh();
    }

    /** קורא מחדש את הזריקות והסטטיסטיקה מהמסד ומעדכן את המסך. */
    private void refresh() {
        shots.clear();
        shots.addAll(dbHelper.getShotsForUser(userId));
        adapter.notifyDataSetChanged();
        findViewById(R.id.textEmpty).setVisibility(shots.isEmpty() ? View.VISIBLE : View.GONE);
        showStatistics();
    }

    /** מציג סיכום כללי (מ-JOIN) ושורה לכל חודש (מ-GROUP BY). */
    private void showStatistics() {
        UserSummary summary = dbHelper.getUserSummary(userId);
        TextView textSummary = findViewById(R.id.textSummary);
        if (summary != null) {
            textSummary.setText(getString(R.string.stats_summary,
                    summary.getTotalShots(), summary.getAverageScore(), summary.getBestScore()));
        }
        StringBuilder monthly = new StringBuilder();
        for (MonthlyStat stat : dbHelper.getMonthlyStats(userId)) {
            monthly.append(getString(R.string.stats_month, stat.getMonth(),
                    stat.getAverageScore(), stat.getShotCount())).append('\n');
        }
        ((TextView) findViewById(R.id.textMonthly)).setText(monthly.toString().trim());
    }

    /** Update: דיאלוג עם שדה טקסט לעריכת ההערה של הזריקה. */
    @Override
    public void onEditNote(ShotData shot) {
        EditText input = new EditText(this);
        input.setText(shot.getNote());
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_note_title)
                .setView(input)
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    dbHelper.updateShotNote(shot.getId(), input.getText().toString().trim());
                    refresh();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    /** Delete: מבקש אישור ורק אז מוחק את הזריקה מהמסד. */
    @Override
    public void onDelete(ShotData shot) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    dbHelper.deleteShot(shot.getId());
                    refresh();
                    Snackbar.make(findViewById(android.R.id.content), R.string.shot_deleted,
                            Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    /** חץ "חזרה" בסרגל העליון. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

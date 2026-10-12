// AI-GENERATED (Claude Code) - reviewed and understood by the student
package com.example.basketballshotanalyzer.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basketballshotanalyzer.R;
import com.example.basketballshotanalyzer.model.ShotData;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Adapter של RecyclerView: "מתרגם" רשימת ShotData לשורות על המסך.
 * RecyclerView יוצר רק כמה שורות שנכנסות במסך וממחזר אותן בגלילה (לכן הוא יעיל).
 */
public class ShotAdapter extends RecyclerView.Adapter<ShotAdapter.ShotViewHolder> {

    /** ממשק שדרכו ה-Adapter מודיע ל-Activity על לחיצות (ה-Adapter עצמו לא ניגש למסד). */
    public interface OnShotActionListener {
        void onEditNote(ShotData shot);
        void onDelete(ShotData shot);
    }

    private final List<ShotData> shots;
    private final OnShotActionListener listener;

    public ShotAdapter(List<ShotData> shots, OnShotActionListener listener) {
        this.shots = shots;
        this.listener = listener;
    }

    /** יוצר שורה חדשה מקובץ item_shot.xml (נקרא רק כמה פעמים). */
    @NonNull
    @Override
    public ShotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shot, parent, false);
        return new ShotViewHolder(view);
    }

    /** ממלא שורה קיימת בנתונים של הזריקה במקום position (נקרא בכל גלילה). */
    @Override
    public void onBindViewHolder(@NonNull ShotViewHolder holder, int position) {
        ShotData shot = shots.get(position);
        View item = holder.itemView;
        holder.textScore.setText(item.getContext().getString(R.string.result_score, shot.getScore()));
        holder.textDate.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(new Date(shot.getTimestamp())));
        holder.textAngles.setText(item.getContext().getString(R.string.item_angles,
                shot.getReleaseAngle(), shot.getElbowAngle()));
        holder.textFeedback.setText(shot.getFeedback());
        boolean hasNote = shot.getNote() != null && !shot.getNote().isEmpty();
        holder.textNote.setVisibility(hasNote ? View.VISIBLE : View.GONE);
        holder.textNote.setText(item.getContext().getString(R.string.item_note, shot.getNote()));
        holder.btnEditNote.setOnClickListener(v -> listener.onEditNote(shot));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(shot));
    }

    @Override
    public int getItemCount() {
        return shots.size();
    }

    /** ViewHolder: שומר הפניות לרכיבי השורה כדי לא לחפש אותם (findViewById) בכל גלילה. */
    static class ShotViewHolder extends RecyclerView.ViewHolder {
        final TextView textScore, textDate, textAngles, textFeedback, textNote;
        final View btnEditNote, btnDelete;

        ShotViewHolder(@NonNull View itemView) {
            super(itemView);
            textScore = itemView.findViewById(R.id.textItemScore);
            textDate = itemView.findViewById(R.id.textItemDate);
            textAngles = itemView.findViewById(R.id.textItemAngles);
            textFeedback = itemView.findViewById(R.id.textItemFeedback);
            textNote = itemView.findViewById(R.id.textItemNote);
            btnEditNote = itemView.findViewById(R.id.btnEditNote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

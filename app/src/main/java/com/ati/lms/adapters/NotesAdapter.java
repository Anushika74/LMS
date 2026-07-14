package com.ati.lms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ati.lms.R;
import com.ati.lms.models.Note;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private final List<Note> notes;
    private final OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onViewClick(Note note);
        void onDownloadClick(Note note);
    }

    public NotesAdapter(List<Note> notes, OnNoteClickListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvDescription.setText(note.getDescription() != null ? note.getDescription() : "");
        holder.tvFileInfo.setText(note.getFileType().toUpperCase() + " • " + note.getFileSize());
        holder.tvDate.setText(note.getUploadedAt() != null ? note.getUploadedAt().substring(0, 10) : "");

        holder.btnView.setOnClickListener(v -> {
            if (listener != null) listener.onViewClick(note);
        });
        holder.btnDownload.setOnClickListener(v -> {
            if (listener != null) listener.onDownloadClick(note);
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvTitle, tvDescription, tvFileInfo, tvDate;
        ImageButton btnView, btnDownload;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardNote);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvDescription = itemView.findViewById(R.id.tvNoteDescription);
            tvFileInfo = itemView.findViewById(R.id.tvFileInfo);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnView = itemView.findViewById(R.id.btnView);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}
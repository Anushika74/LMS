package com.ati.lms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ati.lms.R;
import com.ati.lms.models.PastPaper;

import java.util.List;

public class PastPapersAdapter extends RecyclerView.Adapter<PastPapersAdapter.ViewHolder> {

    private final List<PastPaper> papers;
    private final OnPastPaperClickListener listener;

    public interface OnPastPaperClickListener {
        void onViewClick(PastPaper paper);
        void onDownloadClick(PastPaper paper);
    }

    public PastPapersAdapter(List<PastPaper> papers, OnPastPaperClickListener listener) {
        this.papers = papers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_past_paper, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PastPaper paper = papers.get(position);
        holder.tvTitle.setText(paper.getTitle());
        holder.tvYear.setText(paper.getYear() != null ? "Year: " + paper.getYear() : "");
        holder.tvFileInfo.setText(paper.getFileType().toUpperCase() + " • " + paper.getFileSize());

        holder.btnView.setOnClickListener(v -> listener.onViewClick(paper));
        holder.btnDownload.setOnClickListener(v -> listener.onDownloadClick(paper));
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvYear, tvFileInfo;
        ImageButton btnView, btnDownload;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPaperTitle);
            tvYear = itemView.findViewById(R.id.tvPaperYear);
            tvFileInfo = itemView.findViewById(R.id.tvFileInfo);
            btnView = itemView.findViewById(R.id.btnView);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}
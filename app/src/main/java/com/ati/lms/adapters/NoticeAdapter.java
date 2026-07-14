package com.ati.lms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ati.lms.R;
import com.ati.lms.models.Notice;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    private final List<Notice> notices;
    private final OnNoticeClickListener listener;
    private final boolean isLecturer;

    public interface OnNoticeClickListener {
        void onNoticeClick(Notice notice);
    }

    public NoticeAdapter(List<Notice> notices, OnNoticeClickListener listener, boolean isLecturer) {
        this.notices = notices;
        this.listener = listener;
        this.isLecturer = isLecturer;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notice notice = notices.get(position);
        holder.tvTitle.setText(notice.getTitle());
        holder.tvContent.setText(notice.getContent());
        holder.tvDate.setText(notice.getCreatedAt() != null ? notice.getCreatedAt().substring(0, 10) : "");

        // Priority badge
        if ("urgent".equals(notice.getPriority())) {
            holder.tvPriority.setVisibility(View.VISIBLE);
            holder.tvPriority.setText("URGENT");
            holder.tvPriority.setBackgroundColor(0xFFE53935);
        } else if ("important".equals(notice.getPriority())) {
            holder.tvPriority.setVisibility(View.VISIBLE);
            holder.tvPriority.setText("IMPORTANT");
            holder.tvPriority.setBackgroundColor(0xFFFF9800);
        } else {
            holder.tvPriority.setVisibility(View.GONE);
        }

        if (listener != null) {
            holder.cardView.setOnClickListener(v -> listener.onNoticeClick(notice));
        }
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvTitle, tvContent, tvDate, tvPriority;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardNotice);
            tvTitle = itemView.findViewById(R.id.tvNoticeTitle);
            tvContent = itemView.findViewById(R.id.tvNoticeContent);
            tvDate = itemView.findViewById(R.id.tvNoticeDate);
            tvPriority = itemView.findViewById(R.id.tvPriority);
        }
    }
}
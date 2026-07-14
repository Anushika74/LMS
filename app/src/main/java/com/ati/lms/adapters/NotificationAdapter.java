package com.ati.lms.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ati.lms.R;
import com.ati.lms.models.AppNotification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<AppNotification> notifications;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(AppNotification notification);
    }

    public NotificationAdapter(List<AppNotification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppNotification notification = notifications.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTime.setText(notification.getCreatedAt() != null ? notification.getCreatedAt() : "");

        // Bold for unread
        if (!notification.isRead()) {
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            holder.ivDot.setVisibility(View.VISIBLE);
        } else {
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            holder.ivDot.setVisibility(View.GONE);
        }

        // Set icon based on type
        switch (notification.getType()) {
            case "note": holder.ivIcon.setImageResource(R.drawable.ic_note); break;
            case "assignment": holder.ivIcon.setImageResource(R.drawable.ic_assignment); break;
            case "quiz": holder.ivIcon.setImageResource(R.drawable.ic_quiz); break;
            case "notice": holder.ivIcon.setImageResource(R.drawable.ic_notice); break;
            default: holder.ivIcon.setImageResource(R.drawable.ic_notification); break;
        }

        holder.itemView.setOnClickListener(v -> {
            notification.setRead(true);
            notifyItemChanged(position);
            if (listener != null) listener.onNotificationClick(notification);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivDot;
        TextView tvTitle, tvMessage, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivNotifIcon);
            ivDot = itemView.findViewById(R.id.ivUnreadDot);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
        }
    }
}
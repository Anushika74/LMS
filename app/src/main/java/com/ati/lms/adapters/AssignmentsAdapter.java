package com.ati.lms.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ati.lms.R;
import com.ati.lms.models.Assignment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentsAdapter extends RecyclerView.Adapter<AssignmentsAdapter.ViewHolder> {

    private final List<Assignment> assignments;
    private final OnAssignmentClickListener listener;

    public interface OnAssignmentClickListener {
        void onViewClick(Assignment assignment);
        void onDownloadClick(Assignment assignment);
    }

    public AssignmentsAdapter(List<Assignment> assignments, OnAssignmentClickListener listener) {
        this.assignments = assignments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Assignment assignment = assignments.get(position);
        holder.tvTitle.setText(assignment.getTitle());
        holder.tvDescription.setText(assignment.getDescription() != null ? assignment.getDescription() : "");
        holder.tvDueDate.setText("Due: " + assignment.getDueDate());

        // Check if overdue
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date dueDate = sdf.parse(assignment.getDueDate());
            if (dueDate != null && dueDate.before(new Date())) {
                holder.tvDueDate.setTextColor(Color.RED);
                holder.tvStatus.setText("OVERDUE");
                holder.tvStatus.setTextColor(Color.RED);
            } else {
                holder.tvDueDate.setTextColor(Color.parseColor("#1565C0"));
                holder.tvStatus.setText("PENDING");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            }
        } catch (ParseException e) {
            // ignore
        }

        holder.btnView.setOnClickListener(v -> listener.onViewClick(assignment));
        holder.btnDownload.setOnClickListener(v -> listener.onDownloadClick(assignment));
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDueDate, tvStatus;
        ImageButton btnView, btnDownload;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAssignmentTitle);
            tvDescription = itemView.findViewById(R.id.tvAssignmentDescription);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnView = itemView.findViewById(R.id.btnView);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}
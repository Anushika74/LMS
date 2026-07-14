package com.ati.lms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ati.lms.R;
import com.ati.lms.models.QuizResult;

import java.util.List;

public class QuizResultAdapter extends RecyclerView.Adapter<QuizResultAdapter.ViewHolder> {

    private final List<QuizResult> results;

    public QuizResultAdapter(List<QuizResult> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizResult result = results.get(position);
        holder.tvQuizTitle.setText(result.getQuizTitle());
        holder.tvScore.setText(result.getScore() + "/" + result.getTotalMarks());
        holder.tvPercentage.setText(String.format("%.0f%%", result.getPercentage()));
        holder.progressBar.setProgress((int) result.getPercentage());
        if (result.getAttemptedAt() != null && result.getAttemptedAt().length() >= 10) {
            holder.tvDate.setText(result.getAttemptedAt().substring(0, 10));
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuizTitle, tvScore, tvPercentage, tvDate;
        ProgressBar progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            tvQuizTitle = itemView.findViewById(R.id.tvResultQuizTitle);
            tvScore = itemView.findViewById(R.id.tvResultScore);
            tvPercentage = itemView.findViewById(R.id.tvResultPercentage);
            tvDate = itemView.findViewById(R.id.tvResultDate);
            progressBar = itemView.findViewById(R.id.progressResult);
        }
    }
}
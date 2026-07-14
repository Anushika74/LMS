package com.ati.lms.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ati.lms.R;
import com.ati.lms.databinding.ActivityQuizResultBinding;

/**
 * QuizResultActivity - Displays quiz result after submission
 */
public class QuizResultActivity extends AppCompatActivity {

    private ActivityQuizResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int score = getIntent().getIntExtra("score", 0);
        int totalMarks = getIntent().getIntExtra("total_marks", 0);
        double percentage = getIntent().getDoubleExtra("percentage", 0);
        String quizTitle = getIntent().getStringExtra("quiz_title");

        binding.tvQuizTitle.setText(quizTitle);
        binding.tvScore.setText(score + " / " + totalMarks);
        binding.tvPercentage.setText(String.format("%.1f%%", percentage));
        binding.progressScore.setProgress((int) percentage);

        // Set result message based on percentage
        if (percentage >= 75) {
            binding.tvResultMessage.setText("Excellent! Great job!");
            binding.tvResultMessage.setTextColor(getColor(R.color.success_green));
        } else if (percentage >= 50) {
            binding.tvResultMessage.setText("Good work! Keep it up!");
            binding.tvResultMessage.setTextColor(getColor(R.color.primary_blue));
        } else {
            binding.tvResultMessage.setText("Keep practicing! You can do better.");
            binding.tvResultMessage.setTextColor(getColor(R.color.warning_orange));
        }

        binding.btnDone.setOnClickListener(v -> finish());
    }
}
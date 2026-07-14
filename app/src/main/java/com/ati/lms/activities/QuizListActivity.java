package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.QuizAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityQuizListBinding;
import com.ati.lms.models.Quiz;
import com.ati.lms.utils.SessionManager;

import java.util.List;

/**
 * QuizListActivity - Shows available quizzes for a module
 */
public class QuizListActivity extends AppCompatActivity {

    private ActivityQuizListBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int moduleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        moduleId = getIntent().getIntExtra("module_id", 0);
        String moduleName = getIntent().getStringExtra("module_name");

        binding.tvTitle.setText(moduleName + " - Quizzes");
        binding.btnBack.setOnClickListener(v -> finish());

        loadQuizzes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuizzes();
    }

    private void loadQuizzes() {
        List<Quiz> quizzes = dbHelper.getQuizzesByModule(moduleId);

        if (quizzes.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvQuizzes.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvQuizzes.setVisibility(View.VISIBLE);
            binding.rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
            binding.rvQuizzes.setAdapter(new QuizAdapter(quizzes, quiz -> {
                int studentId = sessionManager.getUserId();
                if (dbHelper.hasAttemptedQuiz(quiz.getId(), studentId)) {
                    Toast.makeText(this, "You have already attempted this quiz", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, QuizAttemptActivity.class);
                    intent.putExtra("quiz_id", quiz.getId());
                    intent.putExtra("quiz_title", quiz.getTitle());
                    startActivity(intent);
                }
            }));
        }
    }
}
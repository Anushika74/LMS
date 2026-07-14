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
import com.ati.lms.databinding.ActivityLecturerQuizBinding;
import com.ati.lms.models.Quiz;
import com.ati.lms.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * LecturerQuizActivity - View and manage created quizzes
 */
public class LecturerQuizActivity extends AppCompatActivity {

    private ActivityLecturerQuizBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int filterModuleId = -1;
    private String filterModuleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLecturerQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        filterModuleId = getIntent().getIntExtra("module_id", -1);
        filterModuleName = getIntent().getStringExtra("module_name");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateQuizActivity.class);
            if (filterModuleId != -1) {
                intent.putExtra("module_id", filterModuleId);
                intent.putExtra("module_name", filterModuleName);
            }
            startActivity(intent);
        });

        if (filterModuleId != -1) {
            binding.tvTitle.setText("Quizzes: " + filterModuleName);
        }

        loadQuizzes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuizzes();
    }

    private void loadQuizzes() {
        List<Quiz> quizzes;
        if (filterModuleId != -1) {
            quizzes = dbHelper.getQuizzesByModule(filterModuleId);
        } else {
            quizzes = dbHelper.getQuizzesByLecturer(sessionManager.getUserId());
        }

        if (quizzes.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvQuizzes.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvQuizzes.setVisibility(View.VISIBLE);
            binding.rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
            binding.rvQuizzes.setAdapter(new QuizAdapter(quizzes, quiz -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(quiz.getTitle())
                        .setMessage("Total Marks: " + quiz.getTotalMarks())
                        .setPositiveButton("Delete", (d, w) -> {
                            dbHelper.deleteQuiz(quiz.getId());
                            loadQuizzes();
                            Toast.makeText(this, "Quiz deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Close", null)
                        .show();
            }));
        }
    }
}
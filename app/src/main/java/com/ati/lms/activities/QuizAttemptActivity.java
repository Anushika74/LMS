package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityQuizAttemptBinding;
import com.ati.lms.models.QuizQuestion;
import com.ati.lms.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuizAttemptActivity - Student takes a quiz with MCQ questions
 * Calculates score instantly and saves result
 */
public class QuizAttemptActivity extends AppCompatActivity {

    private ActivityQuizAttemptBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int quizId;
    private List<QuizQuestion> questions;
    private Map<Integer, String> selectedAnswers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizAttemptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        quizId = getIntent().getIntExtra("quiz_id", 0);
        String quizTitle = getIntent().getStringExtra("quiz_title");

        binding.tvTitle.setText(quizTitle);
        binding.btnBack.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Leave Quiz?")
                    .setMessage("Your progress will be lost.")
                    .setPositiveButton("Leave", (d, w) -> finish())
                    .setNegativeButton("Stay", null)
                    .show();
        });

        questions = dbHelper.getQuizQuestions(quizId);
        binding.tvQuestionCount.setText(questions.size() + " Questions");

        setupQuestions();

        binding.btnSubmit.setOnClickListener(v -> submitQuiz());
    }

    private void setupQuestions() {
        binding.llQuestions.removeAllViews();

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion q = questions.get(i);

            // Inflate question item
            android.view.View questionView = getLayoutInflater().inflate(R.layout.item_quiz_question, binding.llQuestions, false);

            android.widget.TextView tvQuestionNum = questionView.findViewById(R.id.tvQuestionNumber);
            android.widget.TextView tvQuestion = questionView.findViewById(R.id.tvQuestion);
            RadioGroup radioGroup = questionView.findViewById(R.id.radioGroup);
            RadioButton rbA = questionView.findViewById(R.id.rbOptionA);
            RadioButton rbB = questionView.findViewById(R.id.rbOptionB);
            RadioButton rbC = questionView.findViewById(R.id.rbOptionC);
            RadioButton rbD = questionView.findViewById(R.id.rbOptionD);

            tvQuestionNum.setText("Question " + (i + 1));
            tvQuestion.setText(q.getQuestion());
            rbA.setText("A. " + q.getOptionA());
            rbB.setText("B. " + q.getOptionB());
            rbC.setText("C. " + q.getOptionC());
            rbD.setText("D. " + q.getOptionD());

            final int questionIndex = i;
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                String answer = "";
                if (checkedId == R.id.rbOptionA) answer = "A";
                else if (checkedId == R.id.rbOptionB) answer = "B";
                else if (checkedId == R.id.rbOptionC) answer = "C";
                else if (checkedId == R.id.rbOptionD) answer = "D";
                selectedAnswers.put(questionIndex, answer);
            });

            binding.llQuestions.addView(questionView);
        }
    }

    private void submitQuiz() {
        if (selectedAnswers.size() < questions.size()) {
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
            return;
        }

        int score = 0;
        int totalMarks = 0;

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion q = questions.get(i);
            totalMarks += q.getMarks();
            String selected = selectedAnswers.get(i);
            if (selected != null && selected.equalsIgnoreCase(q.getCorrectAnswer())) {
                score += q.getMarks();
            }
        }

        double percentage = totalMarks > 0 ? (score * 100.0 / totalMarks) : 0;

        // Save result
        dbHelper.saveQuizResult(quizId, sessionManager.getUserId(), score, totalMarks, percentage);

        // Navigate to result screen
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("total_marks", totalMarks);
        intent.putExtra("percentage", percentage);
        intent.putExtra("quiz_title", binding.tvTitle.getText().toString());
        startActivity(intent);
        finish();
    }
}
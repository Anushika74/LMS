package com.ati.lms.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ati.lms.R;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityCreateQuizBinding;
import com.ati.lms.models.Module;
import com.ati.lms.models.QuizQuestion;
import com.ati.lms.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateQuizActivity - Create quiz with multiple MCQ questions
 */
public class CreateQuizActivity extends AppCompatActivity {

    private ActivityCreateQuizBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Module> modules;
    private List<QuizQuestion> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int filterModuleId = -1;
    private String filterModuleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        filterModuleId = getIntent().getIntExtra("module_id", -1);
        filterModuleName = getIntent().getStringExtra("module_name");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAddQuestion.setOnClickListener(v -> addQuestion());
        binding.btnSaveQuiz.setOnClickListener(v -> saveQuiz());

        if (filterModuleId != -1) {
            binding.spinnerModule.setVisibility(View.GONE);
        } else {
            setupModuleSpinner();
        }
        setupCorrectAnswerSpinner();
    }

    private void setupModuleSpinner() {
        modules = dbHelper.getAllModules();
        String[] moduleNames = new String[modules.size()];
        for (int i = 0; i < modules.size(); i++) {
            moduleNames[i] = modules.get(i).getModuleName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, moduleNames);
        binding.spinnerModule.setAdapter(adapter);
    }

    private void setupCorrectAnswerSpinner() {
        String[] answers = {"A", "B", "C", "D"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, answers);
        binding.spinnerCorrectAnswer.setAdapter(adapter);
    }

    private void addQuestion() {
        String question = binding.etQuestion.getText().toString().trim();
        String optA = binding.etOptionA.getText().toString().trim();
        String optB = binding.etOptionB.getText().toString().trim();
        String optC = binding.etOptionC.getText().toString().trim();
        String optD = binding.etOptionD.getText().toString().trim();
        int correctPos = binding.spinnerCorrectAnswer.getSelectedItemPosition();
        String marksStr = binding.etMarks.getText().toString().trim();

        if (question.isEmpty() || optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
            Toast.makeText(this, "Please fill all question fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int marks = 1;
        if (!marksStr.isEmpty()) {
            marks = Integer.parseInt(marksStr);
        }

        String[] answerOptions = {"A", "B", "C", "D"};
        String correctAnswer = answerOptions[correctPos];

        QuizQuestion q = new QuizQuestion();
        q.setQuestion(question);
        q.setOptionA(optA);
        q.setOptionB(optB);
        q.setOptionC(optC);
        q.setOptionD(optD);
        q.setCorrectAnswer(correctAnswer);
        q.setMarks(marks);
        questions.add(q);

        // Clear fields
        binding.etQuestion.setText("");
        binding.etOptionA.setText("");
        binding.etOptionB.setText("");
        binding.etOptionC.setText("");
        binding.etOptionD.setText("");
        binding.etMarks.setText("");

        binding.tvQuestionCount.setText(questions.size() + " question(s) added");
        Toast.makeText(this, "Question added!", Toast.LENGTH_SHORT).show();
    }

    private void saveQuiz() {
        String title = binding.etQuizTitle.getText().toString().trim();
        String description = binding.etQuizDescription.getText().toString().trim();
        int moduleId;

        if (filterModuleId != -1) {
            moduleId = filterModuleId;
        } else {
            int modulePosition = binding.spinnerModule.getSelectedItemPosition();
            if (modulePosition < 0) {
                Toast.makeText(this, "Please select a module", Toast.LENGTH_SHORT).show();
                return;
            }
            moduleId = modules.get(modulePosition).getId();
        }

        if (title.isEmpty()) {
            binding.tilQuizTitle.setError("Quiz title is required");
            return;
        }
        if (questions.isEmpty()) {
            Toast.makeText(this, "Please add at least one question", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalMarks = 0;
        for (QuizQuestion q : questions) {
            totalMarks += q.getMarks();
        }

        long quizId = dbHelper.addQuiz(moduleId, sessionManager.getUserId(), title, description, totalMarks);

        if (quizId > 0) {
            for (QuizQuestion q : questions) {
                dbHelper.addQuizQuestion((int) quizId, q.getQuestion(), q.getOptionA(),
                        q.getOptionB(), q.getOptionC(), q.getOptionD(), q.getCorrectAnswer(), q.getMarks());
            }
            Toast.makeText(this, "Quiz created successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to create quiz", Toast.LENGTH_SHORT).show();
        }
    }
}
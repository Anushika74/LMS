package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ati.lms.databinding.ActivityModuleResourcesBinding;

/**
 * ModuleResourcesActivity - Hub for module-specific resources (Notes, Past Papers, Quizzes)
 */
public class ModuleResourcesActivity extends AppCompatActivity {

    private ActivityModuleResourcesBinding binding;
    private int moduleId;
    private String moduleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModuleResourcesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        moduleId = getIntent().getIntExtra("module_id", 0);
        moduleName = getIntent().getStringExtra("module_name");

        binding.tvTitle.setText(moduleName);
        binding.btnBack.setOnClickListener(v -> finish());

        binding.cardNotes.setOnClickListener(v -> {
            Intent intent = new Intent(this, LecturerNotesActivity.class);
            intent.putExtra("module_id", moduleId);
            intent.putExtra("module_name", moduleName);
            startActivity(intent);
        });

        binding.cardPastPapers.setOnClickListener(v -> {
            Intent intent = new Intent(this, LecturerPastPapersActivity.class);
            intent.putExtra("module_id", moduleId);
            intent.putExtra("module_name", moduleName);
            startActivity(intent);
        });

        binding.cardQuizzes.setOnClickListener(v -> {
            Intent intent = new Intent(this, LecturerQuizActivity.class);
            intent.putExtra("module_id", moduleId);
            intent.putExtra("module_name", moduleName);
            startActivity(intent);
        });
    }
}
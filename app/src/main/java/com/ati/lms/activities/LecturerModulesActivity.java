package com.ati.lms.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.ModuleAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityLecturerModulesBinding;
import com.ati.lms.models.Module;
import com.ati.lms.models.Semester;
import com.ati.lms.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * LecturerModulesActivity - CRUD operations for modules
 */
public class LecturerModulesActivity extends AppCompatActivity {

    private ActivityLecturerModulesBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int editingId = -1;
    private List<Semester> allSemesters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLecturerModulesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveModule());
        binding.fabAdd.setOnClickListener(v -> showForm());

        setupSemesterSpinner();
        loadModules();
    }

    private void setupSemesterSpinner() {
        allSemesters.clear();
        // Get all semesters from all courses/years
        List<com.ati.lms.models.Course> courses = dbHelper.getAllCourses();
        List<String> semesterNames = new ArrayList<>();

        for (com.ati.lms.models.Course course : courses) {
            List<com.ati.lms.models.Year> years = dbHelper.getYearsByCourse(course.getId());
            for (com.ati.lms.models.Year year : years) {
                List<Semester> semesters = dbHelper.getSemestersByYear(year.getId());
                for (Semester sem : semesters) {
                    allSemesters.add(sem);
                    semesterNames.add(course.getName() + " - " + year.getYearName() + " - " + sem.getSemesterName());
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, semesterNames);
        binding.spinnerSemester.setAdapter(adapter);
    }

    private void showForm() {
        binding.layoutForm.setVisibility(View.VISIBLE);
        editingId = -1;
        binding.etModuleName.setText("");
        binding.etModuleCode.setText("");
    }

    private void saveModule() {
        String name = binding.etModuleName.getText().toString().trim();
        String code = binding.etModuleCode.getText().toString().trim();
        int semPosition = binding.spinnerSemester.getSelectedItemPosition();

        if (name.isEmpty()) {
            binding.tilModuleName.setError("Module name is required");
            return;
        }

        if (editingId > 0) {
            dbHelper.updateModule(editingId, name, code);
            Toast.makeText(this, "Module updated", Toast.LENGTH_SHORT).show();
        } else {
            if (semPosition < 0 || allSemesters.isEmpty()) {
                Toast.makeText(this, "Please select a semester", Toast.LENGTH_SHORT).show();
                return;
            }
            int semesterId = allSemesters.get(semPosition).getId();
            long result = dbHelper.addModule(semesterId, name, code, sessionManager.getUserId());
            if (result > 0) {
                Toast.makeText(this, "Module added", Toast.LENGTH_SHORT).show();
            }
        }

        binding.layoutForm.setVisibility(View.GONE);
        loadModules();
    }

    private void loadModules() {
        List<Module> modules = dbHelper.getAllModules();

        if (modules.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvModules.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvModules.setVisibility(View.VISIBLE);
            binding.rvModules.setLayoutManager(new LinearLayoutManager(this));
            binding.rvModules.setAdapter(new ModuleAdapter(modules, module -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(module.getModuleName())
                        .setItems(new String[]{"Edit", "Delete"}, (d, which) -> {
                            if (which == 0) {
                                editingId = module.getId();
                                binding.layoutForm.setVisibility(View.VISIBLE);
                                binding.etModuleName.setText(module.getModuleName());
                                binding.etModuleCode.setText(module.getModuleCode());
                            } else {
                                dbHelper.deleteModule(module.getId());
                                loadModules();
                                Toast.makeText(this, "Module deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }));
        }
    }
}
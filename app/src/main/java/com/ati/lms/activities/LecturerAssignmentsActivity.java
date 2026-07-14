package com.ati.lms.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.AssignmentsAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityLecturerAssignmentsBinding;
import com.ati.lms.models.Assignment;
import com.ati.lms.models.Course;
import com.ati.lms.models.Module;
import com.ati.lms.models.Semester;
import com.ati.lms.models.Year;
import com.ati.lms.utils.FileUtils;
import com.ati.lms.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * LecturerAssignmentsActivity - CRUD operations for assignments
 */
public class LecturerAssignmentsActivity extends AppCompatActivity {

    private ActivityLecturerAssignmentsBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private Uri selectedFileUri;
    private List<Course> coursesList;
    private List<Year> yearsList;
    private List<Semester> semestersList;
    private List<Module> modules;
    private int editingId = -1;
    private String selectedDueDate = "";
    private int filterModuleId = -1;
    private String filterModuleName;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    if (selectedFileUri != null) {
                        binding.tvSelectedFile.setText(FileUtils.getFileName(this, selectedFileUri));
                        binding.tvSelectedFile.setVisibility(View.VISIBLE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLecturerAssignmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        filterModuleId = getIntent().getIntExtra("module_id", -1);
        filterModuleName = getIntent().getStringExtra("module_name");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSelectFile.setOnClickListener(v -> selectFile());
        binding.btnSave.setOnClickListener(v -> saveAssignment());
        binding.btnDueDate.setOnClickListener(v -> showDatePicker());
        binding.fabAdd.setOnClickListener(v -> showForm());

        if (filterModuleId != -1) {
            binding.tvTitle.setText("Assignments: " + filterModuleName);
            binding.spinnerCourse.setVisibility(View.GONE);
            binding.spinnerYear.setVisibility(View.GONE);
            binding.spinnerSemester.setVisibility(View.GONE);
            binding.spinnerModule.setVisibility(View.GONE);
        } else {
            setupSpinners();
        }
        loadAssignments();
    }

    private void setupSpinners() {
        coursesList = dbHelper.getAllCourses();
        List<String> courseNames = new ArrayList<>();
        for (Course c : coursesList) courseNames.add(c.getName());

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, courseNames);
        binding.spinnerCourse.setAdapter(courseAdapter);

        binding.spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupYearSpinner(coursesList.get(position).getId());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupYearSpinner(int courseId) {
        yearsList = dbHelper.getYearsByCourse(courseId);
        List<String> yearNames = new ArrayList<>();
        for (Year y : yearsList) yearNames.add(y.getYearName());

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, yearNames);
        binding.spinnerYear.setAdapter(yearAdapter);

        binding.spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupSemesterSpinner(yearsList.get(position).getId());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSemesterSpinner(int yearId) {
        semestersList = dbHelper.getSemestersByYear(yearId);
        List<String> semesterNames = new ArrayList<>();
        for (Semester s : semestersList) semesterNames.add(s.getSemesterName());

        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, semesterNames);
        binding.spinnerSemester.setAdapter(semesterAdapter);

        binding.spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupModuleSpinner(semestersList.get(position).getId());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupModuleSpinner(int semesterId) {
        modules = dbHelper.getModulesBySemester(semesterId);
        List<String> moduleNames = new ArrayList<>();
        
        if (modules.isEmpty()) {
            moduleNames.add("No Modules Found - Add in Course tab");
        } else {
            for (Module m : modules) moduleNames.add(m.getModuleName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, moduleNames);
        binding.spinnerModule.setAdapter(adapter);
    }

    private void showForm() {
        if (binding.layoutForm.getVisibility() == View.VISIBLE) {
            binding.layoutForm.setVisibility(View.GONE);
            binding.layoutList.setVisibility(View.VISIBLE);
            binding.fabAdd.setImageResource(android.R.drawable.ic_input_add);
        } else {
            binding.layoutForm.setVisibility(View.VISIBLE);
            binding.layoutList.setVisibility(View.GONE);
            binding.fabAdd.setImageResource(android.R.drawable.ic_delete);
            editingId = -1;
            binding.etTitle.setText("");
            binding.etDescription.setText("");
            binding.tvDueDate.setText("");
            binding.tvSelectedFile.setVisibility(View.GONE);
            selectedFileUri = null;
            selectedDueDate = "";
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDueDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            binding.tvDueDate.setText(selectedDueDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        filePickerLauncher.launch(intent);
    }

    private void saveAssignment() {
        try {
            String title = binding.etTitle.getText().toString().trim();
            String description = binding.etDescription.getText().toString().trim();
            int moduleId;

            if (filterModuleId != -1) {
                moduleId = filterModuleId;
            } else {
                int modulePosition = binding.spinnerModule.getSelectedItemPosition();
                if (modulePosition < 0 || modules == null || modules.isEmpty()) {
                    Toast.makeText(this, "Please select a module", Toast.LENGTH_SHORT).show();
                    return;
                }
                moduleId = modules.get(modulePosition).getId();
            }

            if (title.isEmpty()) {
                binding.tilTitle.setError("Title is required");
                return;
            }
            if (selectedDueDate.isEmpty()) {
                Toast.makeText(this, "Please set a due date", Toast.LENGTH_SHORT).show();
                return;
            }

            int lecturerId = sessionManager.getUserId();
            if (lecturerId <= 0) {
                Toast.makeText(this, "Invalid session. Please login again.", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSave.setEnabled(false);

            String filePath = null;
            String fileType = null;
            if (selectedFileUri != null) {
                filePath = FileUtils.copyFileToAppStorage(this, selectedFileUri, FileUtils.getAssignmentsFolder());
                if (filePath != null) {
                    fileType = FileUtils.getFileExtension(filePath);
                }
            }

            boolean success;

            if (editingId > 0) {
                success = dbHelper.updateAssignment(editingId, title, description, selectedDueDate, filePath, fileType);
                if (success) {
                    Toast.makeText(this, "Assignment updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update assignment in database", Toast.LENGTH_SHORT).show();
                }
            } else {
                long result = dbHelper.addAssignment(moduleId, lecturerId, title,
                        description, selectedDueDate, filePath, fileType);
                success = result > 0;
                if (success) {
                    Toast.makeText(this, "Assignment created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save assignment. Check if module exists.", Toast.LENGTH_SHORT).show();
                }
            }

            binding.progressBar.setVisibility(View.GONE);
            binding.btnSave.setEnabled(true);

            if (success) {
                binding.layoutForm.setVisibility(View.GONE);
                binding.layoutList.setVisibility(View.VISIBLE);
                binding.fabAdd.setImageResource(android.R.drawable.ic_input_add);
                loadAssignments();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving assignment: " + e.getMessage(), Toast.LENGTH_LONG).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSave.setEnabled(true);
        }
    }

    private void loadAssignments() {
        List<Assignment> assignments;
        if (filterModuleId != -1) {
            assignments = dbHelper.getAssignmentsByModule(filterModuleId);
        } else {
            assignments = dbHelper.getAssignmentsByLecturer(sessionManager.getUserId());
        }

        if (assignments.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvAssignments.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvAssignments.setVisibility(View.VISIBLE);
            binding.rvAssignments.setLayoutManager(new LinearLayoutManager(this));
            binding.rvAssignments.setAdapter(new AssignmentsAdapter(assignments, new AssignmentsAdapter.OnAssignmentClickListener() {
                @Override
                public void onViewClick(Assignment a) {
                    editingId = a.getId();
                    binding.layoutForm.setVisibility(View.VISIBLE);
                    binding.etTitle.setText(a.getTitle());
                    binding.etDescription.setText(a.getDescription());
                    selectedDueDate = a.getDueDate();
                    binding.tvDueDate.setText(a.getDueDate());
                }

                @Override
                public void onDownloadClick(Assignment a) {
                    new MaterialAlertDialogBuilder(LecturerAssignmentsActivity.this)
                            .setTitle("Delete Assignment")
                            .setMessage("Delete '" + a.getTitle() + "'?")
                            .setPositiveButton("Delete", (d, w) -> {
                                if (a.getFilePath() != null) FileUtils.deleteFile(a.getFilePath());
                                dbHelper.deleteAssignment(a.getId());
                                loadAssignments();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }));
        }
    }
}
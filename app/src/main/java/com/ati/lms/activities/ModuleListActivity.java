package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityModuleListBinding;
import com.ati.lms.databinding.DialogAddModuleBinding;
import com.ati.lms.models.Module;
import com.ati.lms.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * ModuleListActivity - Shows modules for a semester and allows lecturer management
 */
public class ModuleListActivity extends AppCompatActivity {

    private ActivityModuleListBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int semesterId;
    private String semesterName;
    private String courseName;
    private boolean isLecturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModuleListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        semesterId = getIntent().getIntExtra("semester_id", 0);
        semesterName = getIntent().getStringExtra("semester_name");
        courseName = getIntent().getStringExtra("course_name");
        isLecturer = "lecturer".equals(sessionManager.getUserRole());

        binding.tvTitle.setText(courseName + " - " + semesterName);
        binding.btnBack.setOnClickListener(v -> finish());

        if (isLecturer) {
            binding.fabAddModule.setVisibility(View.VISIBLE);
            binding.fabAddModule.setOnClickListener(v -> showAddModuleDialog(null));
        }

        loadModules();
    }

    private void loadModules() {
        List<Module> modules = dbHelper.getModulesBySemester(semesterId);

        if (modules.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvModules.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvModules.setVisibility(View.VISIBLE);
        }

        binding.rvModules.setLayoutManager(new LinearLayoutManager(this));
        binding.rvModules.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            @Override
            public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
                android.view.View view = getLayoutInflater().inflate(R.layout.item_course, parent, false);
                return new androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder holder, int position) {
                Module module = modules.get(position);
                android.widget.TextView tvName = holder.itemView.findViewById(R.id.tvCourseName);
                android.widget.TextView tvDesc = holder.itemView.findViewById(R.id.tvCourseDescription);
                android.view.View llActions = holder.itemView.findViewById(R.id.llActions);
                android.widget.ImageButton btnEdit = holder.itemView.findViewById(R.id.btnEdit);
                android.widget.ImageButton btnDelete = holder.itemView.findViewById(R.id.btnDelete);

                tvName.setText(module.getModuleName());
                tvDesc.setText(module.getModuleCode());
                
                holder.itemView.setOnClickListener(v -> {
                    if (isLecturer) {
                        Intent intent = new Intent(ModuleListActivity.this, ModuleResourcesActivity.class);
                        intent.putExtra("module_id", module.getId());
                        intent.putExtra("module_name", module.getModuleName());
                        startActivity(intent);
                    } else {
                        showModuleOptions(module);
                    }
                });

                if (isLecturer) {
                    llActions.setVisibility(View.VISIBLE);
                    btnEdit.setOnClickListener(v -> showAddModuleDialog(module));
                    btnDelete.setOnClickListener(v -> {
                        new MaterialAlertDialogBuilder(ModuleListActivity.this)
                                .setTitle("Delete Module")
                                .setMessage("Are you sure you want to delete " + module.getModuleName() + "?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    dbHelper.deleteModule(module.getId());
                                    loadModules();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });
                } else {
                    llActions.setVisibility(View.GONE);
                }
            }

            @Override
            public int getItemCount() {
                return modules.size();
            }
        });
    }

    private void showAddModuleDialog(Module moduleToEdit) {
        DialogAddModuleBinding dialogBinding = DialogAddModuleBinding.inflate(getLayoutInflater());
        String title = moduleToEdit == null ? "Add Module" : "Edit Module";

        if (moduleToEdit != null) {
            dialogBinding.etModuleName.setText(moduleToEdit.getModuleName());
            dialogBinding.etModuleCode.setText(moduleToEdit.getModuleCode());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = dialogBinding.etModuleName.getText().toString().trim();
                    String code = dialogBinding.etModuleCode.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Module name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (moduleToEdit == null) {
                        dbHelper.addModule(semesterId, name, code, sessionManager.getUserId());
                    } else {
                        dbHelper.updateModule(moduleToEdit.getId(), name, code);
                    }
                    loadModules();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showModuleOptions(Module module) {
        String[] options = {"Notes", "Past Papers", "Quizzes", "Assignments"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(module.getModuleName())
                .setItems(options, (dialog, which) -> {
                    Intent intent;
                    switch (which) {
                        case 0:
                            intent = new Intent(this, NotesActivity.class);
                            break;
                        case 1:
                            intent = new Intent(this, PastPapersActivity.class);
                            break;
                        case 2:
                            intent = new Intent(this, QuizListActivity.class);
                            break;
                        case 3:
                            intent = new Intent(this, AssignmentsActivity.class);
                            break;
                        default:
                            return;
                    }
                    intent.putExtra("module_id", module.getId());
                    intent.putExtra("module_name", module.getModuleName());
                    startActivity(intent);
                })
                .show();
    }
}
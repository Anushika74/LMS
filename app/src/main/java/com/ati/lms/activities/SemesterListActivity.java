package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivitySemesterListBinding;
import com.ati.lms.models.Semester;
import com.ati.lms.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * SemesterListActivity - Shows Semesters for selected year and allows lecturer management
 */
public class SemesterListActivity extends AppCompatActivity {

    private ActivitySemesterListBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int yearId;
    private String yearName;
    private String courseName;
    private boolean isLecturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySemesterListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        yearId = getIntent().getIntExtra("year_id", 0);
        yearName = getIntent().getStringExtra("year_name");
        courseName = getIntent().getStringExtra("course_name");
        isLecturer = "lecturer".equals(sessionManager.getUserRole());

        binding.tvTitle.setText(courseName + " - " + yearName);
        binding.btnBack.setOnClickListener(v -> finish());

        if (isLecturer) {
            binding.fabAddSemester.setVisibility(View.VISIBLE);
            binding.fabAddSemester.setOnClickListener(v -> showAddSemesterDialog());
        }

        loadSemesters();
    }

    private void loadSemesters() {
        List<Semester> semesters = dbHelper.getSemestersByYear(yearId);
        if (semesters.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvSemesters.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvSemesters.setVisibility(View.VISIBLE);
        }

        binding.rvSemesters.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSemesters.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            @Override
            public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
                android.view.View view = getLayoutInflater().inflate(R.layout.item_course, parent, false);
                return new androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder holder, int position) {
                Semester semester = semesters.get(position);
                android.widget.TextView tvName = holder.itemView.findViewById(R.id.tvCourseName);
                android.widget.TextView tvDesc = holder.itemView.findViewById(R.id.tvCourseDescription);
                android.view.View llActions = holder.itemView.findViewById(R.id.llActions);
                android.widget.ImageButton btnDelete = holder.itemView.findViewById(R.id.btnDelete);
                holder.itemView.findViewById(R.id.btnEdit).setVisibility(View.GONE);

                tvName.setText(semester.getSemesterName());
                tvDesc.setText(yearName + " - " + semester.getSemesterName());
                
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(SemesterListActivity.this, ModuleListActivity.class);
                    intent.putExtra("semester_id", semester.getId());
                    intent.putExtra("semester_name", semester.getSemesterName());
                    intent.putExtra("course_name", courseName);
                    startActivity(intent);
                });

                if (isLecturer) {
                    llActions.setVisibility(View.VISIBLE);
                    btnDelete.setOnClickListener(v -> {
                        new MaterialAlertDialogBuilder(SemesterListActivity.this)
                                .setTitle("Delete Semester")
                                .setMessage("Are you sure you want to delete " + semester.getSemesterName() + "?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    dbHelper.deleteSemester(semester.getId());
                                    loadSemesters();
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
                return semesters.size();
            }
        });
    }

    private void showAddSemesterDialog() {
        EditText et = new EditText(this);
        et.setHint("e.g. Semester 1");
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Semester")
                .setView(et)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = et.getText().toString().trim();
                    if (!name.isEmpty()) {
                        dbHelper.addSemester(yearId, name);
                        loadSemesters();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
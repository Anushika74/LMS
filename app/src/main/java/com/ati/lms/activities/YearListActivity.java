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
import com.ati.lms.databinding.ActivityYearListBinding;
import com.ati.lms.models.Year;
import com.ati.lms.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * YearListActivity - Shows Years for selected course and allows lecturer management
 */
public class YearListActivity extends AppCompatActivity {

    private ActivityYearListBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int courseId;
    private String courseName;
    private boolean isLecturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYearListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        courseId = getIntent().getIntExtra("course_id", 0);
        courseName = getIntent().getStringExtra("course_name");
        isLecturer = "lecturer".equals(sessionManager.getUserRole());

        binding.tvTitle.setText(courseName);
        binding.btnBack.setOnClickListener(v -> finish());

        if (isLecturer) {
            binding.fabAddYear.setVisibility(View.VISIBLE);
            binding.fabAddYear.setOnClickListener(v -> showAddYearDialog());
        }

        loadYears();
    }

    private void loadYears() {
        List<Year> years = dbHelper.getYearsByCourse(courseId);
        if (years.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvYears.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvYears.setVisibility(View.VISIBLE);
        }

        binding.rvYears.setLayoutManager(new LinearLayoutManager(this));
        binding.rvYears.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            @Override
            public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
                android.view.View view = getLayoutInflater().inflate(R.layout.item_course, parent, false);
                return new androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder holder, int position) {
                Year year = years.get(position);
                android.widget.TextView tvName = holder.itemView.findViewById(R.id.tvCourseName);
                android.widget.TextView tvDesc = holder.itemView.findViewById(R.id.tvCourseDescription);
                android.view.View llActions = holder.itemView.findViewById(R.id.llActions);
                android.widget.ImageButton btnEdit = holder.itemView.findViewById(R.id.btnEdit);
                android.widget.ImageButton btnDelete = holder.itemView.findViewById(R.id.btnDelete);

                tvName.setText(year.getYearName());
                tvDesc.setText(courseName + " - " + year.getYearName());
                
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(YearListActivity.this, SemesterListActivity.class);
                    intent.putExtra("year_id", year.getId());
                    intent.putExtra("year_name", year.getYearName());
                    intent.putExtra("course_name", courseName);
                    startActivity(intent);
                });

                if (isLecturer) {
                    llActions.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.GONE); // Year edit not specifically requested but can add if needed
                    btnDelete.setOnClickListener(v -> {
                        new MaterialAlertDialogBuilder(YearListActivity.this)
                                .setTitle("Delete Year")
                                .setMessage("Are you sure you want to delete " + year.getYearName() + "?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    dbHelper.deleteYear(year.getId());
                                    loadYears();
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
                return years.size();
            }
        });
    }

    private void showAddYearDialog() {
        EditText et = new EditText(this);
        et.setHint("e.g. 1st Year");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Year")
                .setView(et)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = et.getText().toString().trim();
                    if (!name.isEmpty()) {
                        dbHelper.addYear(courseId, name);
                        loadYears();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
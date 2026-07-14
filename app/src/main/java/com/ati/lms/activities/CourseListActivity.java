package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.CourseAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityCourseListBinding;
import com.ati.lms.databinding.DialogAddCourseBinding;
import com.ati.lms.models.Course;
import com.ati.lms.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * CourseListActivity - Displays the courses and allows lecturer management
 */
public class CourseListActivity extends AppCompatActivity {

    private ActivityCourseListBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private CourseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCourseListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        
        boolean isLecturer = "lecturer".equals(sessionManager.getUserRole());
        if (isLecturer) {
            binding.fabAddCourse.setVisibility(View.VISIBLE);
            binding.fabAddCourse.setOnClickListener(v -> showAddCourseDialog(null));
        }

        loadCourses();
    }

    private void loadCourses() {
        List<Course> courses = dbHelper.getAllCourses();
        if (courses.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvCourses.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvCourses.setVisibility(View.VISIBLE);
        }

        adapter = new CourseAdapter(courses, new CourseAdapter.OnCourseClickListener() {
            @Override
            public void onCourseClick(Course course) {
                Intent intent = new Intent(CourseListActivity.this, YearListActivity.class);
                intent.putExtra("course_id", course.getId());
                intent.putExtra("course_name", course.getName());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Course course) {
                showAddCourseDialog(course);
            }

            @Override
            public void onDeleteClick(Course course) {
                new MaterialAlertDialogBuilder(CourseListActivity.this)
                        .setTitle("Delete Course")
                        .setMessage("Are you sure you want to delete " + course.getName() + "? All related years, semesters and modules will be deleted.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            dbHelper.deleteCourse(course.getId());
                            loadCourses();
                            Toast.makeText(CourseListActivity.this, "Course deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        
        adapter.setLecturer("lecturer".equals(sessionManager.getUserRole()));
        binding.rvCourses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCourses.setAdapter(adapter);
    }

    private void showAddCourseDialog(Course courseToEdit) {
        DialogAddCourseBinding dialogBinding = DialogAddCourseBinding.inflate(getLayoutInflater());
        String title = courseToEdit == null ? "Add Course" : "Edit Course";
        
        if (courseToEdit != null) {
            dialogBinding.etCourseName.setText(courseToEdit.getName());
            dialogBinding.etCourseFullName.setText(courseToEdit.getFullName());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = dialogBinding.etCourseName.getText().toString().trim();
                    String fullName = dialogBinding.etCourseFullName.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Course name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (courseToEdit == null) {
                        dbHelper.addCourse(name, fullName, "");
                    } else {
                        dbHelper.updateCourse(courseToEdit.getId(), name, fullName);
                    }
                    loadCourses();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
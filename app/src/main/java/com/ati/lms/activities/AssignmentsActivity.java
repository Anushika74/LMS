package com.ati.lms.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.AssignmentsAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityAssignmentsBinding;
import com.ati.lms.models.Assignment;
import com.ati.lms.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * AssignmentsActivity - Student view for browsing assignments (sorted by due date)
 */
public class AssignmentsActivity extends AppCompatActivity {

    private ActivityAssignmentsBinding binding;
    private DatabaseHelper dbHelper;
    private int moduleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAssignmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        moduleId = getIntent().getIntExtra("module_id", -1);
        String moduleName = getIntent().getStringExtra("module_name");

        if (moduleName != null) {
            binding.tvTitle.setText(moduleName + " - Assignments");
        } else {
            binding.tvTitle.setText("All Assignments");
        }
        binding.btnBack.setOnClickListener(v -> finish());

        loadAssignments();
    }

    private void loadAssignments() {
        List<Assignment> assignments;
        if (moduleId > 0) {
            assignments = dbHelper.getAssignmentsByModule(moduleId);
        } else {
            assignments = dbHelper.getAllAssignments();
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
                public void onViewClick(Assignment assignment) {
                    if (assignment.getFilePath() != null) {
                        viewFile(assignment.getFilePath());
                    } else {
                        Toast.makeText(AssignmentsActivity.this, "No attachment", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onDownloadClick(Assignment assignment) {
                    if (assignment.getFilePath() != null) {
                        String exported = FileUtils.exportToDownloads(AssignmentsActivity.this, assignment.getFilePath());
                        if (exported != null) {
                            Toast.makeText(AssignmentsActivity.this, "Downloaded to: " + exported, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AssignmentsActivity.this, "No attachment to download", Toast.LENGTH_SHORT).show();
                    }
                }
            }));
        }
    }

    private void viewFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, FileUtils.getMimeType(filePath));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app available to open this file", Toast.LENGTH_SHORT).show();
        }
    }
}
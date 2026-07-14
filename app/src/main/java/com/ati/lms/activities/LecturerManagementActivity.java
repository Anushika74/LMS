package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ati.lms.databinding.ActivityLecturerManagementBinding;

/**
 * LecturerManagementActivity - Hub for limited lecturer management options (Assignments and Notices)
 */
public class LecturerManagementActivity extends AppCompatActivity {

    private ActivityLecturerManagementBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLecturerManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.cardAssignments.setOnClickListener(v ->
                startActivity(new Intent(this, LecturerAssignmentsActivity.class)));
        binding.cardNotices.setOnClickListener(v ->
                startActivity(new Intent(this, LecturerNoticesActivity.class)));
    }
}
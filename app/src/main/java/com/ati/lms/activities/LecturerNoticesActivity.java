package com.ati.lms.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.NoticeAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityLecturerNoticesBinding;
import com.ati.lms.models.Notice;
import com.ati.lms.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * LecturerNoticesActivity - CRUD operations for notices
 */
public class LecturerNoticesActivity extends AppCompatActivity {

    private ActivityLecturerNoticesBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int editingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLecturerNoticesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveNotice());
        binding.fabAdd.setOnClickListener(v -> showForm());

        setupPrioritySpinner();
        loadNotices();
    }

    private void setupPrioritySpinner() {
        String[] priorities = {"Normal", "Important", "Urgent"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, priorities);
        binding.spinnerPriority.setAdapter(adapter);
    }

    private void showForm() {
        binding.layoutForm.setVisibility(View.VISIBLE);
        editingId = -1;
        binding.etTitle.setText("");
        binding.etContent.setText("");
    }

    private void saveNotice() {
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();
        String priority = binding.spinnerPriority.getSelectedItem().toString().toLowerCase();

        if (title.isEmpty()) {
            binding.tilTitle.setError("Title is required");
            return;
        }
        if (content.isEmpty()) {
            binding.tilContent.setError("Content is required");
            return;
        }

        if (editingId > 0) {
            dbHelper.updateNotice(editingId, title, content, priority);
            Toast.makeText(this, "Notice updated", Toast.LENGTH_SHORT).show();
        } else {
            long result = dbHelper.addNotice(sessionManager.getUserId(), title, content, priority);
            if (result > 0) {
                Toast.makeText(this, "Notice created", Toast.LENGTH_SHORT).show();
            }
        }

        binding.layoutForm.setVisibility(View.GONE);
        loadNotices();
    }

    private void loadNotices() {
        List<Notice> notices = dbHelper.getNoticesByLecturer(sessionManager.getUserId());

        if (notices.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvNotices.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvNotices.setVisibility(View.VISIBLE);
            binding.rvNotices.setLayoutManager(new LinearLayoutManager(this));
            binding.rvNotices.setAdapter(new NoticeAdapter(notices, notice -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(notice.getTitle())
                        .setItems(new String[]{"Edit", "Delete"}, (d, which) -> {
                            if (which == 0) {
                                editingId = notice.getId();
                                binding.layoutForm.setVisibility(View.VISIBLE);
                                binding.etTitle.setText(notice.getTitle());
                                binding.etContent.setText(notice.getContent());
                            } else {
                                dbHelper.deleteNotice(notice.getId());
                                loadNotices();
                                Toast.makeText(this, "Notice deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }, true));
        }
    }
}
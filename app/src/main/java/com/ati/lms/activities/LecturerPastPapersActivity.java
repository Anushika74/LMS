package com.ati.lms.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.PastPapersAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityLecturerPastPapersBinding;
import com.ati.lms.models.Module;
import com.ati.lms.models.PastPaper;
import com.ati.lms.utils.FileUtils;
import com.ati.lms.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * LecturerPastPapersActivity - CRUD operations for past papers
 */
public class LecturerPastPapersActivity extends AppCompatActivity {

    private ActivityLecturerPastPapersBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private Uri selectedFileUri;
    private List<Module> modules;
    private int editingId = -1;
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
        binding = ActivityLecturerPastPapersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        filterModuleId = getIntent().getIntExtra("module_id", -1);
        filterModuleName = getIntent().getStringExtra("module_name");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSelectFile.setOnClickListener(v -> selectFile());
        binding.btnUpload.setOnClickListener(v -> uploadPastPaper());
        binding.fabAdd.setOnClickListener(v -> showUploadForm());

        if (filterModuleId != -1) {
            binding.tvTitle.setText("Past Papers: " + filterModuleName);
            binding.spinnerModule.setVisibility(View.GONE);
        } else {
            setupModuleSpinner();
        }
        loadPastPapers();
    }

    private void setupModuleSpinner() {
        modules = dbHelper.getAllModules();
        String[] moduleNames = new String[modules.size()];
        for (int i = 0; i < modules.size(); i++) {
            moduleNames[i] = modules.get(i).getModuleName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, moduleNames);
        binding.spinnerModule.setAdapter(adapter);
    }

    private void showUploadForm() {
        binding.layoutUploadForm.setVisibility(View.VISIBLE);
        editingId = -1;
        binding.etTitle.setText("");
        binding.etDescription.setText("");
        binding.etYear.setText("");
        binding.tvSelectedFile.setVisibility(View.GONE);
        selectedFileUri = null;
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(intent);
    }

    private void uploadPastPaper() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String year = binding.etYear.getText().toString().trim();
        int moduleId;

        if (filterModuleId != -1) {
            moduleId = filterModuleId;
        } else {
            int modulePosition = binding.spinnerModule.getSelectedItemPosition();
            if (modulePosition < 0) {
                Toast.makeText(this, "Please select a module", Toast.LENGTH_SHORT).show();
                return;
            }
            moduleId = modules.get(modulePosition).getId();
        }

        if (title.isEmpty()) {
            binding.tilTitle.setError("Title is required");
            return;
        }

        boolean success;

        if (editingId > 0) {
            String filePath = null;
            String fileType = null;
            if (selectedFileUri != null) {
                filePath = FileUtils.copyFileToAppStorage(this, selectedFileUri, FileUtils.getPastPapersFolder());
                fileType = FileUtils.getFileExtension(filePath);
            }
            success = dbHelper.updatePastPaper(editingId, title, description, year, filePath, fileType);
            if (success) {
                Toast.makeText(this, "Past paper updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update past paper", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (selectedFileUri == null) {
                Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.progressBar.setVisibility(View.VISIBLE);
            String filePath = FileUtils.copyFileToAppStorage(this, selectedFileUri, FileUtils.getPastPapersFolder());
            binding.progressBar.setVisibility(View.GONE);

            if (filePath == null) {
                Toast.makeText(this, "Failed to copy file", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileType = FileUtils.getFileExtension(filePath);
            long fileSize = FileUtils.getFileSize(this, selectedFileUri);

            long result = dbHelper.addPastPaper(moduleId, sessionManager.getUserId(), title,
                    description, year, filePath, fileType, FileUtils.formatFileSize(fileSize));

            success = result > 0;
            if (success) {
                Toast.makeText(this, "Past paper uploaded", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }

        if (success) {
            binding.layoutUploadForm.setVisibility(View.GONE);
            loadPastPapers();
        }
    }

    private void loadPastPapers() {
        List<PastPaper> papers;
        if (filterModuleId != -1) {
            papers = dbHelper.getPastPapersByModule(filterModuleId);
        } else {
            papers = dbHelper.getPastPapersByLecturer(sessionManager.getUserId());
        }

        if (papers.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvPastPapers.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvPastPapers.setVisibility(View.VISIBLE);
            binding.rvPastPapers.setLayoutManager(new LinearLayoutManager(this));
            binding.rvPastPapers.setAdapter(new PastPapersAdapter(papers, new PastPapersAdapter.OnPastPaperClickListener() {
                @Override
                public void onViewClick(PastPaper paper) {
                    editingId = paper.getId();
                    binding.layoutUploadForm.setVisibility(View.VISIBLE);
                    binding.etTitle.setText(paper.getTitle());
                    binding.etDescription.setText(paper.getDescription());
                    binding.etYear.setText(paper.getYear());
                }

                @Override
                public void onDownloadClick(PastPaper paper) {
                    new MaterialAlertDialogBuilder(LecturerPastPapersActivity.this)
                            .setTitle("Delete Past Paper")
                            .setMessage("Delete '" + paper.getTitle() + "'?")
                            .setPositiveButton("Delete", (d, w) -> {
                                FileUtils.deleteFile(paper.getFilePath());
                                dbHelper.deletePastPaper(paper.getId());
                                loadPastPapers();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }));
        }
    }
}
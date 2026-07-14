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
import com.ati.lms.adapters.NotesAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityLecturerNotesBinding;
import com.ati.lms.models.Module;
import com.ati.lms.models.Note;
import com.ati.lms.utils.FileUtils;
import com.ati.lms.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * LecturerNotesActivity - CRUD operations for notes
 * Upload PDF/PPTX, edit, delete notes
 */
public class LecturerNotesActivity extends AppCompatActivity {

    private ActivityLecturerNotesBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private Uri selectedFileUri;
    private String selectedFilePath;
    private List<Module> modules;
    private int editingNoteId = -1;
    private int filterModuleId = -1;
    private String filterModuleName;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    if (selectedFileUri != null) {
                        String fileName = FileUtils.getFileName(this, selectedFileUri);
                        binding.tvSelectedFile.setText(fileName);
                        binding.tvSelectedFile.setVisibility(View.VISIBLE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLecturerNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        filterModuleId = getIntent().getIntExtra("module_id", -1);
        filterModuleName = getIntent().getStringExtra("module_name");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSelectFile.setOnClickListener(v -> selectFile());
        binding.btnUpload.setOnClickListener(v -> uploadNote());
        binding.fabAdd.setOnClickListener(v -> showUploadForm());

        if (filterModuleId != -1) {
            binding.tvTitle.setText("Notes: " + filterModuleName);
            binding.spinnerModule.setVisibility(View.GONE);
        } else {
            setupModuleSpinner();
        }
        loadNotes();
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
        editingNoteId = -1;
        binding.etTitle.setText("");
        binding.etDescription.setText("");
        binding.tvSelectedFile.setVisibility(View.GONE);
        selectedFileUri = null;
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-powerpoint"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(intent);
    }

    private void uploadNote() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        int moduleId;

        if (filterModuleId != -1) {
            moduleId = filterModuleId;
        } else {
            int modulePosition = binding.spinnerModule.getSelectedItemPosition();
            if (modulePosition < 0 || modules.isEmpty()) {
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

        if (editingNoteId > 0) {
            // Update existing note
            String filePath = null;
            String fileType = null;
            if (selectedFileUri != null) {
                filePath = FileUtils.copyFileToAppStorage(this, selectedFileUri, FileUtils.getNotesFolder());
                fileType = FileUtils.getFileExtension(filePath);
            }
            success = dbHelper.updateNote(editingNoteId, title, description, filePath, fileType);
            if (success) {
                Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show();
            }
        } else {
            // New upload
            if (selectedFileUri == null) {
                Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.progressBar.setVisibility(View.VISIBLE);
            String filePath = FileUtils.copyFileToAppStorage(this, selectedFileUri, FileUtils.getNotesFolder());
            binding.progressBar.setVisibility(View.GONE);

            if (filePath == null) {
                Toast.makeText(this, "Failed to copy file", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileType = FileUtils.getFileExtension(filePath);
            long fileSize = FileUtils.getFileSize(this, selectedFileUri);
            String fileSizeStr = FileUtils.formatFileSize(fileSize);

            long result = dbHelper.addNote(moduleId, sessionManager.getUserId(), title, description,
                    filePath, fileType, fileSizeStr);

            success = result > 0;
            if (success) {
                Toast.makeText(this, "Note uploaded successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }

        if (success) {
            binding.layoutUploadForm.setVisibility(View.GONE);
            loadNotes();
        }
    }

    private void loadNotes() {
        List<Note> notes;
        if (filterModuleId != -1) {
            notes = dbHelper.getNotesByModule(filterModuleId);
        } else {
            notes = dbHelper.getNotesByLecturer(sessionManager.getUserId());
        }

        if (notes.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvNotes.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvNotes.setVisibility(View.VISIBLE);
            binding.rvNotes.setLayoutManager(new LinearLayoutManager(this));
            binding.rvNotes.setAdapter(new NotesAdapter(notes, new NotesAdapter.OnNoteClickListener() {
                @Override
                public void onViewClick(Note note) {
                    // Edit mode
                    editingNoteId = note.getId();
                    binding.layoutUploadForm.setVisibility(View.VISIBLE);
                    binding.etTitle.setText(note.getTitle());
                    binding.etDescription.setText(note.getDescription());
                }

                @Override
                public void onDownloadClick(Note note) {
                    // Delete
                    new MaterialAlertDialogBuilder(LecturerNotesActivity.this)
                            .setTitle("Delete Note")
                            .setMessage("Are you sure you want to delete '" + note.getTitle() + "'?")
                            .setPositiveButton("Delete", (d, w) -> {
                                FileUtils.deleteFile(note.getFilePath());
                                dbHelper.deleteNote(note.getId());
                                loadNotes();
                                Toast.makeText(LecturerNotesActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }));
        }
    }
}
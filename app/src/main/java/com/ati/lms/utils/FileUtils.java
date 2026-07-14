package com.ati.lms.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

/**
 * FileUtils - Handles file operations (copy, export, size formatting)
 */
public class FileUtils {

    private static final String APP_FOLDER = "ATI_LMS";
    private static final String NOTES_FOLDER = "Notes";
    private static final String PAST_PAPERS_FOLDER = "PastPapers";
    private static final String ASSIGNMENTS_FOLDER = "Assignments";
    private static final String PROFILE_IMAGES_FOLDER = "ProfileImages";

    /**
     * Copy file from URI to app's internal storage
     */
    public static String copyFileToAppStorage(Context context, Uri uri, String subfolder) {
        try {
            String fileName = getFileName(context, uri);
            File dir = new File(context.getFilesDir(), APP_FOLDER + File.separator + subfolder);
            if (!dir.exists()) dir.mkdirs();

            // Add timestamp to avoid name conflicts
            String uniqueName = System.currentTimeMillis() + "_" + fileName;
            File destFile = new File(dir, uniqueName);

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            OutputStream outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            return destFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Export file from app storage to Downloads folder
     */
    public static String exportToDownloads(Context context, String sourcePath) {
        try {
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) return null;

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File atiDir = new File(downloadsDir, APP_FOLDER);
            if (!atiDir.exists()) atiDir.mkdirs();

            File destFile = new File(atiDir, sourceFile.getName());

            InputStream inputStream = new java.io.FileInputStream(sourceFile);
            OutputStream outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            return destFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get file name from URI
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (index >= 0) result = cursor.getString(index);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "unknown_file";
    }

    /**
     * Get file size from URI
     */
    public static long getFileSize(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        return cursor.getLong(sizeIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * Format file size to human readable string
     */
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, units.length - 1);
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Get file extension from path
     */
    public static String getFileExtension(String filePath) {
        if (filePath == null) return "";
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot >= 0) {
            return filePath.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Get MIME type from file extension
     */
    public static String getMimeType(String filePath) {
        String extension = getFileExtension(filePath);
        if (extension.isEmpty()) return "application/octet-stream";
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mime != null ? mime : "application/octet-stream";
    }

    /**
     * Check if file is a PDF
     */
    public static boolean isPdf(String filePath) {
        return "pdf".equalsIgnoreCase(getFileExtension(filePath));
    }

    /**
     * Check if file is a PPTX
     */
    public static boolean isPptx(String filePath) {
        String ext = getFileExtension(filePath);
        return "pptx".equalsIgnoreCase(ext) || "ppt".equalsIgnoreCase(ext);
    }

    /**
     * Delete file from storage
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null) return false;
        File file = new File(filePath);
        return file.exists() && file.delete();
    }

    /**
     * Get appropriate subfolder for storage type
     */
    public static String getNotesFolder() { return NOTES_FOLDER; }
    public static String getPastPapersFolder() { return PAST_PAPERS_FOLDER; }
    public static String getAssignmentsFolder() { return ASSIGNMENTS_FOLDER; }
    public static String getProfileImagesFolder() { return PROFILE_IMAGES_FOLDER; }
}
package com.ati.lms.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ati.lms.models.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper - Central SQLite database management for ATI LMS
 * Handles all CRUD operations for courses, users, notes, assignments, quizzes, etc.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ati_lms.db";
    private static final int DATABASE_VERSION = 2;
    private static DatabaseHelper instance;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_COURSES = "courses";
    private static final String TABLE_YEARS = "years";
    private static final String TABLE_SEMESTERS = "semesters";
    private static final String TABLE_MODULES = "modules";
    private static final String TABLE_NOTES = "notes";
    private static final String TABLE_PAST_PAPERS = "past_papers";
    private static final String TABLE_ASSIGNMENTS = "assignments";
    private static final String TABLE_QUIZZES = "quizzes";
    private static final String TABLE_QUIZ_QUESTIONS = "quiz_questions";
    private static final String TABLE_QUIZ_RESULTS = "quiz_results";
    private static final String TABLE_NOTICES = "notices";
    private static final String TABLE_NOTIFICATIONS = "notifications";

    // Singleton pattern
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "student_id TEXT, " +
                "designation TEXT, " +
                "profile_image TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

        // Courses table
        db.execSQL("CREATE TABLE " + TABLE_COURSES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "full_name TEXT, " +
                "description TEXT)");

        // Years table
        db.execSQL("CREATE TABLE " + TABLE_YEARS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "course_id INTEGER NOT NULL, " +
                "year_name TEXT NOT NULL, " +
                "FOREIGN KEY (course_id) REFERENCES " + TABLE_COURSES + "(id))");

        // Semesters table
        db.execSQL("CREATE TABLE " + TABLE_SEMESTERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "year_id INTEGER NOT NULL, " +
                "semester_name TEXT NOT NULL, " +
                "FOREIGN KEY (year_id) REFERENCES " + TABLE_YEARS + "(id) ON DELETE CASCADE)");

        // Modules table
        db.execSQL("CREATE TABLE " + TABLE_MODULES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "semester_id INTEGER NOT NULL, " +
                "module_name TEXT NOT NULL, " +
                "module_code TEXT, " +
                "lecturer_id INTEGER, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (semester_id) REFERENCES " + TABLE_SEMESTERS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (lecturer_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE SET NULL)");

        // Notes table
        db.execSQL("CREATE TABLE " + TABLE_NOTES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "module_id INTEGER NOT NULL, " +
                "lecturer_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "file_path TEXT NOT NULL, " +
                "file_type TEXT NOT NULL, " +
                "file_size TEXT, " +
                "uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (module_id) REFERENCES " + TABLE_MODULES + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (lecturer_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE)");

        // Past Papers table
        db.execSQL("CREATE TABLE " + TABLE_PAST_PAPERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "module_id INTEGER NOT NULL, " +
                "lecturer_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "year TEXT, " +
                "file_path TEXT NOT NULL, " +
                "file_type TEXT NOT NULL, " +
                "file_size TEXT, " +
                "uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (module_id) REFERENCES " + TABLE_MODULES + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (lecturer_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE)");

        // Assignments table
        db.execSQL("CREATE TABLE " + TABLE_ASSIGNMENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "module_id INTEGER NOT NULL, " +
                "lecturer_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "due_date TEXT NOT NULL, " +
                "file_path TEXT, " +
                "file_type TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (module_id) REFERENCES " + TABLE_MODULES + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (lecturer_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE)");

        // Quizzes table
        db.execSQL("CREATE TABLE " + TABLE_QUIZZES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "module_id INTEGER NOT NULL, " +
                "lecturer_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "total_marks INTEGER NOT NULL, " +
                "time_limit INTEGER DEFAULT 0, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (module_id) REFERENCES " + TABLE_MODULES + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (lecturer_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE)");

        // Quiz Questions table
        db.execSQL("CREATE TABLE " + TABLE_QUIZ_QUESTIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "quiz_id INTEGER NOT NULL, " +
                "question TEXT NOT NULL, " +
                "option_a TEXT NOT NULL, " +
                "option_b TEXT NOT NULL, " +
                "option_c TEXT NOT NULL, " +
                "option_d TEXT NOT NULL, " +
                "correct_answer TEXT NOT NULL, " +
                "marks INTEGER DEFAULT 1, " +
                "FOREIGN KEY (quiz_id) REFERENCES " + TABLE_QUIZZES + "(id) ON DELETE CASCADE)");

        // Quiz Results table
        db.execSQL("CREATE TABLE " + TABLE_QUIZ_RESULTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "quiz_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "score INTEGER NOT NULL, " +
                "total_marks INTEGER NOT NULL, " +
                "percentage REAL NOT NULL, " +
                "attempted_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (quiz_id) REFERENCES " + TABLE_QUIZZES + "(id), " +
                "FOREIGN KEY (student_id) REFERENCES " + TABLE_USERS + "(id))");

        // Notices table
        db.execSQL("CREATE TABLE " + TABLE_NOTICES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lecturer_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "priority TEXT DEFAULT 'normal', " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (lecturer_id) REFERENCES " + TABLE_USERS + "(id))");

        // Notifications table
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "title TEXT NOT NULL, " +
                "message TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "is_read INTEGER DEFAULT 0, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id))");

        // Insert default courses
        insertDefaultCourses(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_RESULTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZZES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSIGNMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAST_PAPERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEMESTERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YEARS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ==================== DEFAULT DATA ====================

    private void insertDefaultCourses(SQLiteDatabase db) {
        String[][] courses = {
                {"HNDIT", "Higher National Diploma in Information Technology"},
                {"HNDM", "Higher National Diploma in Management"},
                {"HNDE", "Higher National Diploma in Engineering"},
                {"HNDTHM", "Higher National Diploma in Tourism & Hospitality Management"},
                {"HNDA", "Higher National Diploma in Accountancy"}
        };

        for (String[] course : courses) {
            ContentValues cv = new ContentValues();
            cv.put("name", course[0]);
            cv.put("full_name", course[1]);
            long courseId = db.insert(TABLE_COURSES, null, cv);

            // Insert years for each course
            String[] years = {"First Year", "Second Year"};
            for (String year : years) {
                ContentValues ycv = new ContentValues();
                ycv.put("course_id", courseId);
                ycv.put("year_name", year);
                long yearId = db.insert(TABLE_YEARS, null, ycv);

                // Insert semesters for each year
                String[] semesters = {"Semester 1", "Semester 2"};
                for (String semester : semesters) {
                    ContentValues scv = new ContentValues();
                    scv.put("year_id", yearId);
                    scv.put("semester_name", semester);
                    db.insert(TABLE_SEMESTERS, null, scv);
                }
            }
        }

        // Insert sample modules for HNDIT First Year Semester 1
        insertSampleModules(db);
    }

    private void insertSampleModules(SQLiteDatabase db) {
        // Get HNDIT -> First Year -> Semester 1 ID
        Cursor cursor = db.rawQuery(
                "SELECT s.id FROM semesters s " +
                        "JOIN years y ON s.year_id = y.id " +
                        "JOIN courses c ON y.course_id = c.id " +
                        "WHERE c.name = 'HNDIT' AND y.year_name = 'First Year' AND s.semester_name = 'Semester 1'",
                null);

        if (cursor.moveToFirst()) {
            long semesterId = cursor.getLong(0);
            String[] modules = {"Programming Fundamentals", "Database Systems", "Computer Networks",
                    "Software Engineering", "Web Development", "Mathematics for IT"};
            for (String module : modules) {
                ContentValues mcv = new ContentValues();
                mcv.put("semester_id", semesterId);
                mcv.put("module_name", module);
                db.insert(TABLE_MODULES, null, mcv);
            }
        }
        cursor.close();
    }

    // ==================== USER OPERATIONS ====================

    public long registerUser(String fullName, String email, String password, String role,
                             String studentId, String designation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("full_name", fullName);
        cv.put("email", email);
        cv.put("password", password);
        cv.put("role", role);
        if (studentId != null) cv.put("student_id", studentId);
        if (designation != null) cv.put("designation", designation);
        return db.insert(TABLE_USERS, null, cv);
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE email = ? AND password = ?",
                new String[]{email, password});
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE email = ?",
                new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isStudentIdExists(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE student_id = ?",
                new String[]{studentId});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public User getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE id = ?",
                new String[]{String.valueOf(id)});
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public boolean updateUserProfile(int userId, String fullName, String email, String profileImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("full_name", fullName);
        cv.put("email", email);
        if (profileImage != null) cv.put("profile_image", profileImage);
        return db.update(TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        return db.update(TABLE_USERS, cv, "email = ?", new String[]{email}) > 0;
    }

    public boolean updateProfileImage(int userId, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("profile_image", imagePath);
        return db.update(TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        user.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow("student_id")));
        user.setDesignation(cursor.getString(cursor.getColumnIndexOrThrow("designation")));
        user.setProfileImage(cursor.getString(cursor.getColumnIndexOrThrow("profile_image")));
        return user;
    }

    // ==================== COURSE OPERATIONS ====================

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSES, null);
        while (cursor.moveToNext()) {
            Course course = new Course();
            course.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            course.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            course.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            courses.add(course);
        }
        cursor.close();
        return courses;
    }

    public long addCourse(String name, String fullName, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("full_name", fullName);
        cv.put("description", description);
        return db.insert(TABLE_COURSES, null, cv);
    }

    public boolean updateCourse(int id, String name, String fullName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("full_name", fullName);
        return db.update(TABLE_COURSES, cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteCourse(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Manual cascade for years, semesters, modules etc if not handled by foreign keys
        // Assuming foreign keys are enabled in onConfigure
        return db.delete(TABLE_COURSES, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // ==================== YEAR OPERATIONS ====================

    public List<Year> getYearsByCourse(int courseId) {
        List<Year> years = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_YEARS + " WHERE course_id = ?",
                new String[]{String.valueOf(courseId)});
        while (cursor.moveToNext()) {
            Year year = new Year();
            year.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            year.setCourseId(cursor.getInt(cursor.getColumnIndexOrThrow("course_id")));
            year.setYearName(cursor.getString(cursor.getColumnIndexOrThrow("year_name")));
            years.add(year);
        }
        cursor.close();
        return years;
    }

    public long addYear(int courseId, String yearName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("course_id", courseId);
        cv.put("year_name", yearName);
        return db.insert(TABLE_YEARS, null, cv);
    }

    public boolean deleteYear(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_YEARS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // ==================== SEMESTER OPERATIONS ====================

    public List<Semester> getSemestersByYear(int yearId) {
        List<Semester> semesters = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_SEMESTERS + " WHERE year_id = ?",
                new String[]{String.valueOf(yearId)});
        while (cursor.moveToNext()) {
            Semester semester = new Semester();
            semester.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            semester.setYearId(cursor.getInt(cursor.getColumnIndexOrThrow("year_id")));
            semester.setSemesterName(cursor.getString(cursor.getColumnIndexOrThrow("semester_name")));
            semesters.add(semester);
        }
        cursor.close();
        return semesters;
    }

    public long addSemester(int yearId, String semesterName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("year_id", yearId);
        cv.put("semester_name", semesterName);
        return db.insert(TABLE_SEMESTERS, null, cv);
    }

    public boolean deleteSemester(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SEMESTERS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // ==================== MODULE OPERATIONS ====================

    public List<Module> getModulesBySemester(int semesterId) {
        List<Module> modules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_MODULES + " WHERE semester_id = ?",
                new String[]{String.valueOf(semesterId)});
        while (cursor.moveToNext()) {
            modules.add(cursorToModule(cursor));
        }
        cursor.close();
        return modules;
    }

    public List<Module> getAllModules() {
        List<Module> modules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MODULES, null);
        while (cursor.moveToNext()) {
            modules.add(cursorToModule(cursor));
        }
        cursor.close();
        return modules;
    }

    public long addModule(int semesterId, String moduleName, String moduleCode, int lecturerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("semester_id", semesterId);
        cv.put("module_name", moduleName);
        cv.put("module_code", moduleCode);
        if (lecturerId > 0) cv.put("lecturer_id", lecturerId);
        long id = db.insert(TABLE_MODULES, null, cv);
        if (id > 0) addNotification(null, "New Module Added", moduleName + " has been added", "module");
        return id;
    }

    public boolean updateModule(int id, String moduleName, String moduleCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("module_name", moduleName);
        cv.put("module_code", moduleCode);
        return db.update(TABLE_MODULES, cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteModule(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_MODULES, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public List<Module> searchModules(String query) {
        List<Module> modules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_MODULES + " WHERE module_name LIKE ? OR module_code LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"});
        while (cursor.moveToNext()) {
            modules.add(cursorToModule(cursor));
        }
        cursor.close();
        return modules;
    }

    private Module cursorToModule(Cursor cursor) {
        Module module = new Module();
        module.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        module.setSemesterId(cursor.getInt(cursor.getColumnIndexOrThrow("semester_id")));
        module.setModuleName(cursor.getString(cursor.getColumnIndexOrThrow("module_name")));
        module.setModuleCode(cursor.getString(cursor.getColumnIndexOrThrow("module_code")));
        int lecturerIdx = cursor.getColumnIndex("lecturer_id");
        if (lecturerIdx >= 0 && !cursor.isNull(lecturerIdx)) {
            module.setLecturerId(cursor.getInt(lecturerIdx));
        }
        return module;
    }

    // ==================== NOTES OPERATIONS ====================

    public long addNote(int moduleId, int lecturerId, String title, String description,
                        String filePath, String fileType, String fileSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("module_id", moduleId);
        cv.put("lecturer_id", lecturerId);
        cv.put("title", title);
        cv.put("description", description);
        cv.put("file_path", filePath);
        cv.put("file_type", fileType);
        cv.put("file_size", fileSize);
        long id = db.insert(TABLE_NOTES, null, cv);
        if (id > 0) addNotification(null, "New Note Uploaded", title, "note");
        return id;
    }

    public List<Note> getNotesByModule(int moduleId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NOTES + " WHERE module_id = ? ORDER BY uploaded_at DESC",
                new String[]{String.valueOf(moduleId)});
        while (cursor.moveToNext()) {
            notes.add(cursorToNote(cursor));
        }
        cursor.close();
        return notes;
    }

    public List<Note> getNotesByLecturer(int lecturerId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NOTES + " WHERE lecturer_id = ? ORDER BY uploaded_at DESC",
                new String[]{String.valueOf(lecturerId)});
        while (cursor.moveToNext()) {
            notes.add(cursorToNote(cursor));
        }
        cursor.close();
        return notes;
    }

    public List<Note> searchNotes(String query) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NOTES + " WHERE title LIKE ? OR description LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"});
        while (cursor.moveToNext()) {
            notes.add(cursorToNote(cursor));
        }
        cursor.close();
        return notes;
    }

    public boolean updateNote(int id, String title, String description, String filePath, String fileType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("description", description);
        if (filePath != null) {
            cv.put("file_path", filePath);
            cv.put("file_type", fileType);
        }
        return db.update(TABLE_NOTES, cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NOTES, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    private Note cursorToNote(Cursor cursor) {
        Note note = new Note();
        note.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        note.setModuleId(cursor.getInt(cursor.getColumnIndexOrThrow("module_id")));
        note.setLecturerId(cursor.getInt(cursor.getColumnIndexOrThrow("lecturer_id")));
        note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        note.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        note.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow("file_path")));
        note.setFileType(cursor.getString(cursor.getColumnIndexOrThrow("file_type")));
        note.setFileSize(cursor.getString(cursor.getColumnIndexOrThrow("file_size")));
        note.setUploadedAt(cursor.getString(cursor.getColumnIndexOrThrow("uploaded_at")));
        return note;
    }

    // ==================== PAST PAPERS OPERATIONS ====================

    public long addPastPaper(int moduleId, int lecturerId, String title, String description,
                             String year, String filePath, String fileType, String fileSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("module_id", moduleId);
        cv.put("lecturer_id", lecturerId);
        cv.put("title", title);
        cv.put("description", description);
        cv.put("year", year);
        cv.put("file_path", filePath);
        cv.put("file_type", fileType);
        cv.put("file_size", fileSize);
        long id = db.insert(TABLE_PAST_PAPERS, null, cv);
        if (id > 0) addNotification(null, "New Past Paper Uploaded", title, "past_paper");
        return id;
    }

    public List<PastPaper> getPastPapersByModule(int moduleId) {
        List<PastPaper> papers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_PAST_PAPERS + " WHERE module_id = ? ORDER BY uploaded_at DESC",
                new String[]{String.valueOf(moduleId)});
        while (cursor.moveToNext()) {
            papers.add(cursorToPastPaper(cursor));
        }
        cursor.close();
        return papers;
    }

    public List<PastPaper> getPastPapersByLecturer(int lecturerId) {
        List<PastPaper> papers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_PAST_PAPERS + " WHERE lecturer_id = ? ORDER BY uploaded_at DESC",
                new String[]{String.valueOf(lecturerId)});
        while (cursor.moveToNext()) {
            papers.add(cursorToPastPaper(cursor));
        }
        cursor.close();
        return papers;
    }

    public List<PastPaper> searchPastPapers(String query) {
        List<PastPaper> papers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_PAST_PAPERS + " WHERE title LIKE ? OR description LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"});
        while (cursor.moveToNext()) {
            papers.add(cursorToPastPaper(cursor));
        }
        cursor.close();
        return papers;
    }

    public boolean updatePastPaper(int id, String title, String description, String year,
                                   String filePath, String fileType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("description", description);
        cv.put("year", year);
        if (filePath != null) {
            cv.put("file_path", filePath);
            cv.put("file_type", fileType);
        }
        return db.update(TABLE_PAST_PAPERS, cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deletePastPaper(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PAST_PAPERS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    private PastPaper cursorToPastPaper(Cursor cursor) {
        PastPaper paper = new PastPaper();
        paper.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        paper.setModuleId(cursor.getInt(cursor.getColumnIndexOrThrow("module_id")));
        paper.setLecturerId(cursor.getInt(cursor.getColumnIndexOrThrow("lecturer_id")));
        paper.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        paper.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        paper.setYear(cursor.getString(cursor.getColumnIndexOrThrow("year")));
        paper.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow("file_path")));
        paper.setFileType(cursor.getString(cursor.getColumnIndexOrThrow("file_type")));
        paper.setFileSize(cursor.getString(cursor.getColumnIndexOrThrow("file_size")));
        paper.setUploadedAt(cursor.getString(cursor.getColumnIndexOrThrow("uploaded_at")));
        return paper;
    }

    // ==================== ASSIGNMENT OPERATIONS ====================

    public long addAssignment(int moduleId, int lecturerId, String title, String description,
                              String dueDate, String filePath, String fileType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("module_id", moduleId);
        cv.put("lecturer_id", lecturerId);
        cv.put("title", title);
        cv.put("description", description);
        cv.put("due_date", dueDate);
        if (filePath != null) cv.put("file_path", filePath);
        if (fileType != null) cv.put("file_type", fileType);
        
        long id = -1;
        try {
            id = db.insert(TABLE_ASSIGNMENTS, null, cv);
            if (id > 0) {
                // Try catch for notification in case of schema issues
                try {
                    addNotification(null, "New Assignment", title + " - Due: " + dueDate, "assignment");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public List<Assignment> getAssignmentsByModule(int moduleId) {
        List<Assignment> assignments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ASSIGNMENTS + " WHERE module_id = ? ORDER BY due_date ASC",
                new String[]{String.valueOf(moduleId)});
        while (cursor.moveToNext()) {
            assignments.add(cursorToAssignment(cursor));
        }
        cursor.close();
        return assignments;
    }

    public List<Assignment> getAssignmentsByLecturer(int lecturerId) {
        List<Assignment> assignments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ASSIGNMENTS + " WHERE lecturer_id = ? ORDER BY due_date ASC",
                new String[]{String.valueOf(lecturerId)});
        while (cursor.moveToNext()) {
            assignments.add(cursorToAssignment(cursor));
        }
        cursor.close();
        return assignments;
    }

    public List<Assignment> getAllAssignments() {
        List<Assignment> assignments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ASSIGNMENTS + " ORDER BY due_date ASC", null);
        while (cursor.moveToNext()) {
            assignments.add(cursorToAssignment(cursor));
        }
        cursor.close();
        return assignments;
    }

    public List<Assignment> searchAssignments(String query) {
        List<Assignment> assignments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ASSIGNMENTS + " WHERE title LIKE ? OR description LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"});
        while (cursor.moveToNext()) {
            assignments.add(cursorToAssignment(cursor));
        }
        cursor.close();
        return assignments;
    }

    public boolean updateAssignment(int id, String title, String description, String dueDate,
                                    String filePath, String fileType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("description", description);
        cv.put("due_date", dueDate);
        if (filePath != null) cv.put("file_path", filePath);
        if (fileType != null) cv.put("file_type", fileType);
        return db.update(TABLE_ASSIGNMENTS, cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteAssignment(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ASSIGNMENTS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    private Assignment cursorToAssignment(Cursor cursor) {
        Assignment a = new Assignment();
        a.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        a.setModuleId(cursor.getInt(cursor.getColumnIndexOrThrow("module_id")));
        a.setLecturerId(cursor.getInt(cursor.getColumnIndexOrThrow("lecturer_id")));
        a.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        a.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        a.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow("due_date")));
        a.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow("file_path")));
        a.setFileType(cursor.getString(cursor.getColumnIndexOrThrow("file_type")));
        a.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return a;
    }

    // ==================== QUIZ OPERATIONS ====================

    public long addQuiz(int moduleId, int lecturerId, String title, String description, int totalMarks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("module_id", moduleId);
        cv.put("lecturer_id", lecturerId);
        cv.put("title", title);
        cv.put("description", description);
        cv.put("total_marks", totalMarks);
        long id = db.insert(TABLE_QUIZZES, null, cv);
        if (id > 0) addNotification(null, "New Quiz Available", title, "quiz");
        return id;
    }

    public long addQuizQuestion(int quizId, String question, String optA, String optB,
                                String optC, String optD, String correctAnswer, int marks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("quiz_id", quizId);
        cv.put("question", question);
        cv.put("option_a", optA);
        cv.put("option_b", optB);
        cv.put("option_c", optC);
        cv.put("option_d", optD);
        cv.put("correct_answer", correctAnswer);
        cv.put("marks", marks);
        return db.insert(TABLE_QUIZ_QUESTIONS, null, cv);
    }

    public List<Quiz> getQuizzesByModule(int moduleId) {
        List<Quiz> quizzes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_QUIZZES + " WHERE module_id = ? ORDER BY created_at DESC",
                new String[]{String.valueOf(moduleId)});
        while (cursor.moveToNext()) {
            quizzes.add(cursorToQuiz(cursor));
        }
        cursor.close();
        return quizzes;
    }

    public List<Quiz> getQuizzesByLecturer(int lecturerId) {
        List<Quiz> quizzes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_QUIZZES + " WHERE lecturer_id = ? ORDER BY created_at DESC",
                new String[]{String.valueOf(lecturerId)});
        while (cursor.moveToNext()) {
            quizzes.add(cursorToQuiz(cursor));
        }
        cursor.close();
        return quizzes;
    }

    public List<Quiz> getAllQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_QUIZZES + " ORDER BY created_at DESC", null);
        while (cursor.moveToNext()) {
            quizzes.add(cursorToQuiz(cursor));
        }
        cursor.close();
        return quizzes;
    }

    public List<QuizQuestion> getQuizQuestions(int quizId) {
        List<QuizQuestion> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_QUIZ_QUESTIONS + " WHERE quiz_id = ?",
                new String[]{String.valueOf(quizId)});
        while (cursor.moveToNext()) {
            QuizQuestion q = new QuizQuestion();
            q.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            q.setQuizId(cursor.getInt(cursor.getColumnIndexOrThrow("quiz_id")));
            q.setQuestion(cursor.getString(cursor.getColumnIndexOrThrow("question")));
            q.setOptionA(cursor.getString(cursor.getColumnIndexOrThrow("option_a")));
            q.setOptionB(cursor.getString(cursor.getColumnIndexOrThrow("option_b")));
            q.setOptionC(cursor.getString(cursor.getColumnIndexOrThrow("option_c")));
            q.setOptionD(cursor.getString(cursor.getColumnIndexOrThrow("option_d")));
            q.setCorrectAnswer(cursor.getString(cursor.getColumnIndexOrThrow("correct_answer")));
            q.setMarks(cursor.getInt(cursor.getColumnIndexOrThrow("marks")));
            questions.add(q);
        }
        cursor.close();
        return questions;
    }

    public boolean deleteQuiz(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_QUIZ_QUESTIONS, "quiz_id = ?", new String[]{String.valueOf(id)});
        return db.delete(TABLE_QUIZZES, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateQuiz(int id, String title, String description, int totalMarks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("description", description);
        cv.put("total_marks", totalMarks);
        return db.update(TABLE_QUIZZES, cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    private Quiz cursorToQuiz(Cursor cursor) {
        Quiz quiz = new Quiz();
        quiz.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        quiz.setModuleId(cursor.getInt(cursor.getColumnIndexOrThrow("module_id")));
        quiz.setLecturerId(cursor.getInt(cursor.getColumnIndexOrThrow("lecturer_id")));
        quiz.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        quiz.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        quiz.setTotalMarks(cursor.getInt(cursor.getColumnIndexOrThrow("total_marks")));
        quiz.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return quiz;
    }

    // ==================== QUIZ RESULT OPERATIONS ====================

    public long saveQuizResult(int quizId, int studentId, int score, int totalMarks, double percentage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("quiz_id", quizId);
        cv.put("student_id", studentId);
        cv.put("score", score);
        cv.put("total_marks", totalMarks);
        cv.put("percentage", percentage);
        return db.insert(TABLE_QUIZ_RESULTS, null, cv);
    }

    public List<QuizResult> getQuizResultsByStudent(int studentId) {
        List<QuizResult> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT qr.*, q.title as quiz_title FROM " + TABLE_QUIZ_RESULTS + " qr " +
                        "JOIN " + TABLE_QUIZZES + " q ON qr.quiz_id = q.id " +
                        "WHERE qr.student_id = ? ORDER BY qr.attempted_at DESC",
                new String[]{String.valueOf(studentId)});
        while (cursor.moveToNext()) {
            QuizResult r = new QuizResult();
            r.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            r.setQuizId(cursor.getInt(cursor.getColumnIndexOrThrow("quiz_id")));
            r.setStudentId(cursor.getInt(cursor.getColumnIndexOrThrow("student_id")));
            r.setScore(cursor.getInt(cursor.getColumnIndexOrThrow("score")));
            r.setTotalMarks(cursor.getInt(cursor.getColumnIndexOrThrow("total_marks")));
            r.setPercentage(cursor.getDouble(cursor.getColumnIndexOrThrow("percentage")));
            r.setAttemptedAt(cursor.getString(cursor.getColumnIndexOrThrow("attempted_at")));
            r.setQuizTitle(cursor.getString(cursor.getColumnIndexOrThrow("quiz_title")));
            results.add(r);
        }
        cursor.close();
        return results;
    }

    public boolean hasAttemptedQuiz(int quizId, int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_QUIZ_RESULTS + " WHERE quiz_id = ? AND student_id = ?",
                new String[]{String.valueOf(quizId), String.valueOf(studentId)});
        boolean attempted = cursor.getCount() > 0;
        cursor.close();
        return attempted;
    }

    public double getStudentAverageScore(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT AVG(percentage) FROM " + TABLE_QUIZ_RESULTS + " WHERE student_id = ?",
                new String[]{String.valueOf(studentId)});
        double avg = 0;
        if (cursor.moveToFirst()) {
            avg = cursor.getDouble(0);
        }
        cursor.close();
        return avg;
    }

    // ==================== NOTICE OPERATIONS ====================

    public long addNotice(int lecturerId, String title, String content, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("lecturer_id", lecturerId);
        cv.put("title", title);
        cv.put("content", content);
        cv.put("priority", priority);
        long id = db.insert(TABLE_NOTICES, null, cv);
        if (id > 0) addNotification(null, "New Notice", title, "notice");
        return id;
    }

    public List<Notice> getAllNotices() {
        List<Notice> notices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NOTICES + " ORDER BY created_at DESC", null);
        while (cursor.moveToNext()) {
            notices.add(cursorToNotice(cursor));
        }
        cursor.close();
        return notices;
    }

    public List<Notice> getNoticesByLecturer(int lecturerId) {
        List<Notice> notices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NOTICES + " WHERE lecturer_id = ? ORDER BY created_at DESC",
                new String[]{String.valueOf(lecturerId)});
        while (cursor.moveToNext()) {
            notices.add(cursorToNotice(cursor));
        }
        cursor.close();
        return notices;
    }

    public List<Notice> getRecentNotices(int limit) {
        List<Notice> notices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NOTICES + " ORDER BY created_at DESC LIMIT ?",
                new String[]{String.valueOf(limit)});
        while (cursor.moveToNext()) {
            notices.add(cursorToNotice(cursor));
        }
        cursor.close();
        return notices;
    }

    public boolean updateNotice(int id, String title, String content, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("content", content);
        cv.put("priority", priority);
        return db.update(TABLE_NOTICES, cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteNotice(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NOTICES, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    private Notice cursorToNotice(Cursor cursor) {
        Notice notice = new Notice();
        notice.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        notice.setLecturerId(cursor.getInt(cursor.getColumnIndexOrThrow("lecturer_id")));
        notice.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        notice.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
        notice.setPriority(cursor.getString(cursor.getColumnIndexOrThrow("priority")));
        notice.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return notice;
    }

    // ==================== NOTIFICATION OPERATIONS ====================

    public long addNotification(Integer userId, String title, String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (userId != null && userId > 0) {
            cv.put("user_id", userId);
        } else {
            // Check if column exists or use explicit NULL
            cv.putNull("user_id");
        }
        cv.put("title", title);
        cv.put("message", message);
        cv.put("type", type);
        return db.insert(TABLE_NOTIFICATIONS, null, cv);
    }

    public List<AppNotification> getNotifications(int limit) {
        List<AppNotification> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NOTIFICATIONS + " ORDER BY created_at DESC LIMIT ?",
                new String[]{String.valueOf(limit)});
        while (cursor.moveToNext()) {
            AppNotification n = new AppNotification();
            n.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            n.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            n.setMessage(cursor.getString(cursor.getColumnIndexOrThrow("message")));
            n.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
            n.setRead(cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1);
            n.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            notifications.add(n);
        }
        cursor.close();
        return notifications;
    }

    public int getUnreadNotificationCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS + " WHERE is_read = 0", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public void markNotificationRead(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_read", 1);
        db.update(TABLE_NOTIFICATIONS, cv, "id = ?", new String[]{String.valueOf(id)});
    }

    // ==================== STATISTICS (LECTURER DASHBOARD) ====================

    public int getCountByLecturer(String table, int lecturerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + table + " WHERE lecturer_id = ?",
                new String[]{String.valueOf(lecturerId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getTotalModulesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MODULES, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getTotalCoursesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_COURSES, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
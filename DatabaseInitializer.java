package com.fams.database;

import com.fams.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages sample dataset seeding and provides fast thread-safe memory storage fallback
 * if MySQL server is not connected.
 */
public class DatabaseInitializer {

    // Memory Store collections for fallback mode
    public static final Map<String, User> USERS_MAP = new ConcurrentHashMap<>();
    public static final List<AttendanceRecord> ATTENDANCE_RECORDS = new CopyOnWriteArrayList<>();
    public static final List<MedicalRecord> MEDICAL_RECORDS = new CopyOnWriteArrayList<>();
    public static final List<Notice> NOTICES = new CopyOnWriteArrayList<>();
    public static final List<TimetableEntry> TIMETABLE_ENTRIES = new CopyOnWriteArrayList<>();
    public static final Map<String, String> COURSES_MAP = new ConcurrentHashMap<>();

    private static boolean initialized = false;

    public static synchronized void initializeSampleData() {
        if (initialized) return;

        COURSES_MAP.put("ICT2132", "Object Oriented Programming Practicum");
        COURSES_MAP.put("ICT2113", "Database Systems Practicum");
        COURSES_MAP.put("ICT2122", "Data Structures & Algorithms");
        COURSES_MAP.put("ENG2102", "Technical Communication");

        // 1. Seed Admin (1)
        AdminUser admin = new AdminUser("ADM001", "admin", "admin123", "I M H B B Ilukkumbura (Admin)", "admin@fot.ruh.ac.lk", "0412223344", "Information & Communication Technology");
        USERS_MAP.put(admin.getUsername(), admin);

        // 2. Seed Lecturers (5)
        LecturerUser lec1 = new LecturerUser("LEC001", "dr_samarasinghe", "lec123", "Dr. A. Samarasinghe", "samarasinghe@fot.ruh.ac.lk", "0711112233", "ICT Department");
        LecturerUser lec2 = new LecturerUser("LEC002", "prof_perera", "lec123", "Prof. K. L. Perera", "perera@fot.ruh.ac.lk", "0712223344", "ICT Department");
        LecturerUser lec3 = new LecturerUser("LEC003", "dr_gunawardena", "lec123", "Dr. N. Gunawardena", "gunawardena@fot.ruh.ac.lk", "0713334455", "ET Department");
        LecturerUser lec4 = new LecturerUser("LEC004", "ms_fernando", "lec123", "Ms. T. Fernando", "fernando@fot.ruh.ac.lk", "0714445566", "BST Department");
        LecturerUser lec5 = new LecturerUser("LEC005", "mr_jayasinghe", "lec123", "Mr. R. Jayasinghe", "jayasinghe@fot.ruh.ac.lk", "0715556677", "ICT Department");

        USERS_MAP.put(lec1.getUsername(), lec1);
        USERS_MAP.put(lec2.getUsername(), lec2);
        USERS_MAP.put(lec3.getUsername(), lec3);
        USERS_MAP.put(lec4.getUsername(), lec4);
        USERS_MAP.put(lec5.getUsername(), lec5);

        // 3. Seed Technical Officers (4)
        TechnicalOfficerUser to1 = new TechnicalOfficerUser("TO001", "to_chamikara", "to123", "C H G Chamikara (TO)", "chamikara@fot.ruh.ac.lk", "0771112233", "ICT Department");
        TechnicalOfficerUser to2 = new TechnicalOfficerUser("TO002", "to_bandara", "to123", "Mr. S. Bandara (TO)", "bandara@fot.ruh.ac.lk", "0772223344", "ET Department");
        TechnicalOfficerUser to3 = new TechnicalOfficerUser("TO003", "to_silva", "to123", "Ms. M. Silva (TO)", "silva@fot.ruh.ac.lk", "0773334455", "BST Department");
        TechnicalOfficerUser to4 = new TechnicalOfficerUser("TO004", "to_kumara", "to123", "Mr. D. Kumara (TO)", "kumara@fot.ruh.ac.lk", "0774445566", "ICT Department");

        USERS_MAP.put(to1.getUsername(), to1);
        USERS_MAP.put(to2.getUsername(), to2);
        USERS_MAP.put(to3.getUsername(), to3);
        USERS_MAP.put(to4.getUsername(), to4);

        // 4. Seed Undergraduates (20) including repeat and batch missed
        String[] studentNames = {
            "Kasun Rajapaksha", "Nimali Wickramasinghe", "Pathum Nissanka", "Dilshan Madushanka", "Kusal Mendis",
            "Charith Asalanka", "Wanindu Hasaranga", "Maheesh Theekshana", "Matheesha Pathirana", "Dunith Wellalage",
            "Kamindu Mendis", "Asitha Fernando", "Avishka Fernando", "Bhanuka Rajapaksa", "Dushmantha Chameera",
            "Lahiru Kumara", "Nuwan Thushara", "Pawan Rathnayake", "Janith Liyanage", "Sadeera Samarawickrama"
        };

        for (int i = 1; i <= 20; i++) {
            String regNo = String.format("TG/2024/%04d", 2100 + i);
            String username = "student" + i;
            String pass = "student123";
            String name = studentNames[i - 1];
            String email = "student" + i + "@fot.ruh.ac.lk";
            String phone = "078000" + String.format("%04d", i);
            String dept = "ICT Department";
            String batch = (i > 16) ? "Batch 15" : "Batch 16";
            boolean isRepeat = (i == 17 || i == 18);
            boolean isBatchMissed = (i == 19 || i == 20);

            UndergraduateUser ug = new UndergraduateUser("UG" + i, username, pass, name, email, phone, dept, regNo, batch, isRepeat, isBatchMissed);
            USERS_MAP.put(username, ug);
        }

        // 5. Seed Attendance scenarios (15 Theory + 15 Practical sessions)
        // Scenario 1: UG 1 (Kasun) -> >80% Attendance (14/15 Theory, 14/15 Practical = 28/30 = 93.3%)
        seedStudentAttendance("TG/2024/2101", "Kasun Rajapaksha", "ICT2132", 14, 14, false, false);

        // Scenario 2: UG 2 (Nimali) -> Exactly 80% Attendance (12/15 Theory, 12/15 Practical = 24/30 = 80.0%)
        seedStudentAttendance("TG/2024/2102", "Nimali Wickramasinghe", "ICT2132", 12, 12, false, false);

        // Scenario 3: UG 3 (Pathum) -> <80% Attendance WITHOUT medicals (9/15 Theory, 10/15 Practical = 19/30 = 63.3%, 0 Meds)
        seedStudentAttendance("TG/2024/2103", "Pathum Nissanka", "ICT2132", 9, 10, false, false);

        // Scenario 4: UG 4 (Dilshan) -> >80% Attendance WITH medicals (13/15 Theory, 12/15 Practical = 25/30 = 83.3%, 2 approved medicals)
        seedStudentAttendance("TG/2024/2104", "Dilshan Madushanka", "ICT2132", 13, 12, true, true);

        // Scenario 5: UG 5 (Kusal) -> <80% Attendance WITH medicals (10/15 Theory, 9/15 Practical = 19/30 = 63.3%, 4 medicals [3 Approved, 1 Pending])
        seedStudentAttendance("TG/2024/2105", "Kusal Mendis", "ICT2132", 10, 9, true, true);

        // Seed default 85% attendance for remaining students 6 to 20
        for (int i = 6; i <= 20; i++) {
            String regNo = String.format("TG/2024/%04d", 2100 + i);
            String name = studentNames[i - 1];
            seedStudentAttendance(regNo, name, "ICT2132", 13, 13, false, false);
        }

        // 6. Seed Medical Records for Scenario 4 & 5 and TO verification
        int medId = 1;
        // Dilshan (Scenario 4)
        MEDICAL_RECORDS.add(new MedicalRecord(medId++, "TG/2024/2104", "Dilshan Madushanka", "ICT2132", ComponentType.PRACTICAL, 14, LocalDate.now().minusDays(5), "MED/2026/041", "Fever & Viral Infection", LocalDate.now().minusDays(3), MedicalStatus.APPROVED, "to_chamikara"));
        MEDICAL_RECORDS.add(new MedicalRecord(medId++, "TG/2024/2104", "Dilshan Madushanka", "ICT2132", ComponentType.PRACTICAL, 15, LocalDate.now().minusDays(4), "MED/2026/042", "Fever & Medical Rest", LocalDate.now().minusDays(3), MedicalStatus.APPROVED, "to_chamikara"));

        // Kusal (Scenario 5)
        MEDICAL_RECORDS.add(new MedicalRecord(medId++, "TG/2024/2105", "Kusal Mendis", "ICT2132", ComponentType.THEORY, 11, LocalDate.now().minusDays(15), "MED/2026/051", "Hospital Admission - Dengue", LocalDate.now().minusDays(10), MedicalStatus.APPROVED, "to_chamikara"));
        MEDICAL_RECORDS.add(new MedicalRecord(medId++, "TG/2024/2105", "Kusal Mendis", "ICT2132", ComponentType.THEORY, 12, LocalDate.now().minusDays(14), "MED/2026/052", "Hospital Admission - Dengue", LocalDate.now().minusDays(10), MedicalStatus.APPROVED, "to_chamikara"));
        MEDICAL_RECORDS.add(new MedicalRecord(medId++, "TG/2024/2105", "Kusal Mendis", "ICT2132", ComponentType.PRACTICAL, 11, LocalDate.now().minusDays(12), "MED/2026/053", "Recovery Rest", LocalDate.now().minusDays(8), MedicalStatus.APPROVED, "to_chamikara"));
        MEDICAL_RECORDS.add(new MedicalRecord(medId++, "TG/2024/2105", "Kusal Mendis", "ICT2132", ComponentType.PRACTICAL, 12, LocalDate.now().minusDays(2), "MED/2026/054", "Migraine Specialist Note", LocalDate.now().minusDays(1), MedicalStatus.PENDING, null));

        // 7. Seed Notices (Admin Created)
        int noticeId = 1;
        NOTICES.add(new Notice(noticeId++, "Welcome to Semester 1 Academic Portal", "Faculty Academic Management System (FAMS) is live. All students are advised to verify attendance and timetable schedules.", "Admin (I M H B B Ilukkumbura)", LocalDateTime.now().minusDays(10), null, true));
        NOTICES.add(new Notice(noticeId++, "ICT2132 Mid-Semester Practical Assessment", "The practical assessment for ICT2132 will take place on Friday 8.00 AM in Computer Lab 02. Attendance is compulsory.", "Dr. A. Samarasinghe", LocalDateTime.now().minusDays(3), Role.UNDERGRADUATE, true));
        NOTICES.add(new Notice(noticeId++, "Medical Certificate Submission Deadline", "Technical Officers remind all undergraduates to submit medical certificates within 14 days of absence for approval.", "C H G Chamikara (TO)", LocalDateTime.now().minusDays(1), null, false));

        // 8. Seed Timetables (Admin & Department view)
        int ttId = 1;
        TIMETABLE_ENTRIES.add(new TimetableEntry(ttId++, "ICT2132", "Object Oriented Programming Practicum", ComponentType.THEORY, "Monday", "08:00 AM", "10:00 AM", "Auditorium A", "Dr. A. Samarasinghe", "ICT Department", "Batch 16"));
        TIMETABLE_ENTRIES.add(new TimetableEntry(ttId++, "ICT2132", "Object Oriented Programming Practicum", ComponentType.PRACTICAL, "Wednesday", "01:00 PM", "03:00 PM", "Computer Lab 02", "Dr. A. Samarasinghe", "ICT Department", "Batch 16"));
        TIMETABLE_ENTRIES.add(new TimetableEntry(ttId++, "ICT2113", "Database Systems Practicum", ComponentType.THEORY, "Tuesday", "10:00 AM", "12:00 PM", "Lecture Hall 03", "Prof. K. L. Perera", "ICT Department", "Batch 16"));
        TIMETABLE_ENTRIES.add(new TimetableEntry(ttId++, "ICT2113", "Database Systems Practicum", ComponentType.PRACTICAL, "Thursday", "08:00 AM", "10:00 AM", "Database Lab 01", "Prof. K. L. Perera", "ICT Department", "Batch 16"));
        TIMETABLE_ENTRIES.add(new TimetableEntry(ttId++, "ICT2122", "Data Structures & Algorithms", ComponentType.THEORY, "Friday", "08:00 AM", "10:00 AM", "Lecture Hall 01", "Mr. R. Jayasinghe", "ICT Department", "Batch 16"));

        initialized = true;
    }

    private static void seedStudentAttendance(String regNo, String name, String courseCode, int theoryAttendedCount, int practicalAttendedCount, boolean generateMedicalForAbsence, boolean isApprovedMedical) {
        int recId = ATTENDANCE_RECORDS.size() + 1;
        LocalDate startDate = LocalDate.now().minusWeeks(15);

        // 15 Theory Sessions
        for (int session = 1; session <= 15; session++) {
            boolean present = (session <= theoryAttendedCount);
            boolean hasMed = !present && generateMedicalForAbsence;
            boolean medApp = hasMed && isApprovedMedical;

            ATTENDANCE_RECORDS.add(new AttendanceRecord(
                recId++, regNo, name, courseCode, "Object Oriented Programming Practicum",
                ComponentType.THEORY, session, startDate.plusDays(session * 7),
                present, hasMed, medApp
            ));
        }

        // 15 Practical Sessions
        for (int session = 1; session <= 15; session++) {
            boolean present = (session <= practicalAttendedCount);
            boolean hasMed = !present && generateMedicalForAbsence;
            boolean medApp = hasMed && isApprovedMedical;

            ATTENDANCE_RECORDS.add(new AttendanceRecord(
                recId++, regNo, name, courseCode, "Object Oriented Programming Practicum",
                ComponentType.PRACTICAL, session, startDate.plusDays(session * 7 + 2),
                present, hasMed, medApp
            ));
        }
    }
}

-- FAMS COMPLETE DATABASE SETUP
-- Combined from database_schema.sql, marks_eligibility_v2_fixed.sql,
-- and sync_medical_attendance.sql.
-- Run this file in MySQL Workbench / phpMyAdmin as one script.
-- Recommended order: base schema -> academic marks upgrade -> medical sync.


-- 1) DATABASE SCHEMA
-- Source file: database_schema.sql

-- ============================================================================
-- FAMS database setup. Safe to re-run: this script does NOT delete the database.
-- Back up your database before applying schema changes to an existing installation.
-- Existing tables are preserved because all table creation uses IF NOT EXISTS.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS fams_db;
USE fams_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
                                     user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    department VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    student_reg_no VARCHAR(50) UNIQUE,
    batch VARCHAR(50),
    is_repeat BOOLEAN DEFAULT FALSE,
    is_batch_missed BOOLEAN DEFAULT FALSE,
    profile_picture_url VARCHAR(500) DEFAULT NULL
    );

-- 2. Courses Table
CREATE TABLE IF NOT EXISTS courses (
                                       course_code VARCHAR(20) PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    theory_credits INT DEFAULT 2,
    practical_credits INT DEFAULT 1,
    department VARCHAR(100) NOT NULL
    );

-- 3. Attendance Records Table
--    NOTE: uq_attendance_session added so re-running this script does not
--          duplicate rows in the ICT2132 seed further down.
CREATE TABLE IF NOT EXISTS attendance_records (
                                                  record_id INT AUTO_INCREMENT PRIMARY KEY,
                                                  student_reg_no VARCHAR(50) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    component_type VARCHAR(20) NOT NULL, -- THEORY or PRACTICAL
    session_number INT NOT NULL, -- 1 to 15
    session_date DATE NOT NULL,
    is_present BOOLEAN NOT NULL DEFAULT TRUE,
    has_medical_submitted BOOLEAN DEFAULT FALSE,
    is_medical_approved BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (student_reg_no) REFERENCES users(student_reg_no) ON DELETE CASCADE,
    FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    UNIQUE KEY uq_attendance_session (student_reg_no, course_code, component_type, session_number)
    );

-- 4. Medical Records Table
CREATE TABLE IF NOT EXISTS medical_records (
                                               medical_id INT AUTO_INCREMENT PRIMARY KEY,
                                               student_reg_no VARCHAR(50) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    component_type VARCHAR(20) NOT NULL,
    session_number INT NOT NULL,
    absent_date DATE NOT NULL,
    reference_no VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    submission_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    reviewed_by VARCHAR(100),
    image_path VARCHAR(255),
    FOREIGN KEY (student_reg_no) REFERENCES users(student_reg_no) ON DELETE CASCADE
    );

-- 5. Notices Table
CREATE TABLE IF NOT EXISTS notices (
                                       notice_id INT AUTO_INCREMENT PRIMARY KEY,
                                       title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    published_by VARCHAR(100) NOT NULL,
    published_date DATETIME NOT NULL,
    target_role VARCHAR(30), -- NULL for ALL, or LECTURER, TECHNICAL_OFFICER, UNDERGRADUATE
    is_important BOOLEAN DEFAULT FALSE
    );

-- 6. Timetables Table
CREATE TABLE IF NOT EXISTS timetables (
                                          timetable_id INT AUTO_INCREMENT PRIMARY KEY,
                                          course_code VARCHAR(20) NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    component_type VARCHAR(20) NOT NULL, -- THEORY or PRACTICAL
    day_of_week VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    venue VARCHAR(100) NOT NULL,
    lecturer_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    batch VARCHAR(50) NOT NULL,
    academic_period VARCHAR(100) NOT NULL DEFAULT 'Semester 1 - 2026'
    );

-- 6b. Sample weekly timetable (ICT Department, Batch 16, Semester 1 - 2026).
-- Inserted only when the timetables table is empty, so re-importing this file will not duplicate rows.
INSERT INTO timetables (course_code, course_name, component_type, day_of_week, start_time, end_time, venue, lecturer_name, department, batch, academic_period)
SELECT * FROM (
                  SELECT 'ICT2132','Object Oriented Programming Practicum','THEORY','Monday','08:00:00','10:00:00','Auditorium A','Dr. A. Samarasinghe','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2142','E-Business Systems','THEORY','Monday','10:00:00','12:00:00','Lecture Hall 02','Ms. N. Fernando','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ENG2102','Technical Communication','THEORY','Monday','13:00:00','15:00:00','Lecture Hall 01','Ms. S. Wijesinghe','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2122','Data Structures & Algorithms','PRACTICAL','Tuesday','08:00:00','10:00:00','Computer Lab 01','Mr. R. Jayasinghe','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2113','Database Systems Practicum','THEORY','Tuesday','10:00:00','12:00:00','Lecture Hall 03','Prof. K. L. Perera','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2142','E-Business Systems','PRACTICAL','Tuesday','13:00:00','15:00:00','Computer Lab 03','Ms. N. Fernando','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ENG2102','Technical Communication','THEORY','Wednesday','10:00:00','12:00:00','Lecture Hall 01','Ms. S. Wijesinghe','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2132','Object Oriented Programming Practicum','PRACTICAL','Wednesday','13:00:00','15:00:00','Computer Lab 02','Dr. A. Samarasinghe','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2113','Database Systems Practicum','PRACTICAL','Thursday','08:00:00','10:00:00','Database Lab 01','Prof. K. L. Perera','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2142','E-Business Systems','THEORY','Thursday','10:00:00','12:00:00','Lecture Hall 02','Ms. N. Fernando','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2132','Object Oriented Programming Practicum','THEORY','Thursday','13:00:00','15:00:00','Auditorium A','Dr. A. Samarasinghe','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2122','Data Structures & Algorithms','THEORY','Friday','08:00:00','10:00:00','Lecture Hall 01','Mr. R. Jayasinghe','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2122','Data Structures & Algorithms','PRACTICAL','Friday','10:00:00','12:00:00','Computer Lab 01','Mr. R. Jayasinghe','ICT Department','Batch 16','Semester 1 - 2026'
                  UNION ALL
                  SELECT 'ICT2113','Database Systems Practicum','THEORY','Friday','13:00:00','15:00:00','Lecture Hall 03','Prof. K. L. Perera','ICT Department','Batch 16','Semester 1 - 2026'
              ) AS sample_timetable
WHERE NOT EXISTS (SELECT 1 FROM timetables);

-- 7. Course materials (lecturer uploads a title and local path/URL)
CREATE TABLE IF NOT EXISTS course_materials (
                                                material_id INT AUTO_INCREMENT PRIMARY KEY,
                                                course_code VARCHAR(20) NOT NULL,
    lecturer_user_id VARCHAR(50) NULL,
    title VARCHAR(150) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    uploaded_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_material_course FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    CONSTRAINT fk_material_lecturer FOREIGN KEY (lecturer_user_id) REFERENCES users(user_id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS course_enrollments (
                                                  enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
                                                  student_user_id VARCHAR(50) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    enrolled_on DATE NOT NULL DEFAULT (CURRENT_DATE),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_enrollment_student_user FOREIGN KEY (student_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course_code FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    UNIQUE KEY uq_student_course (student_user_id, course_code)
    );

-- 8. Assessments and marks integrated with the shared users/courses tables.
--    FIX: quiz_number was declared as TINYNULL which is not a MySQL type; changed to TINYINT.
CREATE TABLE IF NOT EXISTS assessments (
                                           assessment_id INT AUTO_INCREMENT PRIMARY KEY,
                                           course_code VARCHAR(20) NOT NULL,
    assessment_name VARCHAR(120) NOT NULL,
    assessment_type ENUM('QUIZ','ASSIGNMENT','MID_TERM','NORMAL_ASSIGN','CA','FINAL') NOT NULL DEFAULT 'CA',
    weight_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    quiz_number TINYINT DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_assessment_weight CHECK (weight_percentage BETWEEN 0 AND 100),
    CONSTRAINT chk_assessment_quiz CHECK (quiz_number IS NULL OR quiz_number BETWEEN 1 AND 3),
    CONSTRAINT fk_assessment_course_code FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    UNIQUE KEY uq_course_assessment_name (course_code, assessment_name)
    );

CREATE TABLE IF NOT EXISTS marks (
                                     mark_id INT AUTO_INCREMENT PRIMARY KEY,
                                     assessment_id INT NOT NULL,
                                     student_user_id VARCHAR(50) NOT NULL,
    mark DECIMAL(5,2) NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_mark_range CHECK (mark BETWEEN 0 AND 100),
    CONSTRAINT fk_mark_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE,
    CONSTRAINT fk_mark_student_user FOREIGN KEY (student_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uq_assessment_student_user (assessment_id, student_user_id)
    );

-- Demo seed passwords are intentionally disabled. Create users through the application and set secure passwords.
-- The dataset below uses placeholder credentials; replace them with salted hashes before real deployment.
INSERT IGNORE INTO users (user_id, username, password, full_name, email, phone, department, role, student_reg_no, batch, is_repeat, is_batch_missed) VALUES
('ADM001','admin','admin123','System Administrator','admin@fot.ruh.ac.lk','0412223344','Faculty of Technology','ADMIN',NULL,NULL,FALSE,FALSE),
('LEC001','dr_samarasinghe','lec123','Dr. A. Samarasinghe','samarasinghe@fot.ruh.ac.lk','0711112233','ICT Department','LECTURER',NULL,NULL,FALSE,FALSE),
('LEC002','prof_perera','lec123','Prof. K. L. Perera','perera@fot.ruh.ac.lk','0712223344','ICT Department','LECTURER',NULL,NULL,FALSE,FALSE),
('LEC003','dr_gunawardena','lec123','Dr. N. Gunawardena','gunawardena@fot.ruh.ac.lk','0713334455','ET Department','LECTURER',NULL,NULL,FALSE,FALSE),
('LEC004','ms_fernando','lec123','Ms. T. Fernando','fernando@fot.ruh.ac.lk','0714445566','BST Department','LECTURER',NULL,NULL,FALSE,FALSE),
('LEC005','mr_jayasinghe','lec123','Mr. R. Jayasinghe','jayasinghe@fot.ruh.ac.lk','0715556677','ICT Department','LECTURER',NULL,NULL,FALSE,FALSE),
('TO001','to_chamikara','to123','C H G Chamikara (TO)','chamikara@fot.ruh.ac.lk','0771112233','ICT Department','TECHNICAL_OFFICER',NULL,NULL,FALSE,FALSE),
('TO002','to_bandara','to123','Mr. S. Bandara (TO)','bandara@fot.ruh.ac.lk','0772223344','ET Department','TECHNICAL_OFFICER',NULL,NULL,FALSE,FALSE),
('TO003','to_silva','to123','Ms. M. Silva (TO)','silva@fot.ruh.ac.lk','0773334455','BST Department','TECHNICAL_OFFICER',NULL,NULL,FALSE,FALSE),
('TO004','to_kumara','to123','Mr. D. Kumara (TO)','kumara@fot.ruh.ac.lk','0774445566','ICT Department','TECHNICAL_OFFICER',NULL,NULL,FALSE,FALSE);

-- Demo-only login passwords. Re-running this script updates existing sample accounts too.
UPDATE users SET password='admin123' WHERE username='admin';
UPDATE users SET password='lec123' WHERE username IN ('dr_samarasinghe','prof_perera','dr_gunawardena','ms_fernando','mr_jayasinghe');
UPDATE users SET password='to123' WHERE username IN ('to_chamikara','to_bandara','to_silva','to_kumara');
UPDATE users SET password='student123' WHERE username LIKE 'student%';

-- Core current-semester sample courses. Confirm credit values against the official timetable before assessment use.
INSERT IGNORE INTO courses(course_code,course_name,theory_credits,practical_credits,department) VALUES
('ICT2132','Object Oriented Programming Practicum',1,2,'ICT Department'),
('ICT2113','Database Systems Practicum',1,2,'ICT Department'),
('ICT2122','Data Structures and Algorithms',2,1,'ICT Department'),
('ENG2102','Technical Communication',2,0,'ICT Department');

INSERT IGNORE INTO users(user_id,username,password,full_name,email,phone,department,role,student_reg_no,batch,is_repeat,is_batch_missed) VALUES
('UG1','student1','student123','Kasun Rajapaksha','student1@fot.ruh.ac.lk','0780000001','ICT Department','UNDERGRADUATE','TG/2024/2101','Batch 16',FALSE,FALSE),
('UG2','student2','student123','Nimali Wickramasinghe','student2@fot.ruh.ac.lk','0780000002','ICT Department','UNDERGRADUATE','TG/2024/2102','Batch 16',FALSE,FALSE),
('UG3','student3','student123','Pathum Nissanka','student3@fot.ruh.ac.lk','0780000003','ICT Department','UNDERGRADUATE','TG/2024/2103','Batch 16',FALSE,FALSE),
('UG4','student4','student123','Dilshan Madushanka','student4@fot.ruh.ac.lk','0780000004','ICT Department','UNDERGRADUATE','TG/2024/2104','Batch 16',FALSE,FALSE),
('UG5','student5','student123','Kusal Mendis','student5@fot.ruh.ac.lk','0780000005','ICT Department','UNDERGRADUATE','TG/2024/2105','Batch 16',FALSE,FALSE),
('UG6','student6','student123','Charith Asalanka','student6@fot.ruh.ac.lk','0780000006','ICT Department','UNDERGRADUATE','TG/2024/2106','Batch 16',FALSE,FALSE),
('UG7','student7','student123','Wanindu Hasaranga','student7@fot.ruh.ac.lk','0780000007','ICT Department','UNDERGRADUATE','TG/2024/2107','Batch 16',FALSE,FALSE),
('UG8','student8','student123','Maheesh Theekshana','student8@fot.ruh.ac.lk','0780000008','ICT Department','UNDERGRADUATE','TG/2024/2108','Batch 16',FALSE,FALSE),
('UG9','student9','student123','Matheesha Pathirana','student9@fot.ruh.ac.lk','0780000009','ICT Department','UNDERGRADUATE','TG/2024/2109','Batch 16',FALSE,FALSE),
('UG10','student10','student123','Dunith Wellalage','student10@fot.ruh.ac.lk','0780000010','ICT Department','UNDERGRADUATE','TG/2024/2110','Batch 16',FALSE,FALSE),
('UG11','student11','student123','Kamindu Mendis','student11@fot.ruh.ac.lk','0780000011','ICT Department','UNDERGRADUATE','TG/2024/2111','Batch 16',FALSE,FALSE),
('UG12','student12','student123','Asitha Fernando','student12@fot.ruh.ac.lk','0780000012','ICT Department','UNDERGRADUATE','TG/2024/2112','Batch 16',FALSE,FALSE),
('UG13','student13','student123','Avishka Fernando','student13@fot.ruh.ac.lk','0780000013','ICT Department','UNDERGRADUATE','TG/2024/2113','Batch 16',FALSE,FALSE),
('UG14','student14','student123','Bhanuka Rajapaksa','student14@fot.ruh.ac.lk','0780000014','ICT Department','UNDERGRADUATE','TG/2024/2114','Batch 16',FALSE,FALSE),
('UG15','student15','student123','Dushmantha Chameera','student15@fot.ruh.ac.lk','0780000015','ICT Department','UNDERGRADUATE','TG/2024/2115','Batch 16',FALSE,FALSE),
('UG16','student16','student123','Lahiru Kumara','student16@fot.ruh.ac.lk','0780000016','ICT Department','UNDERGRADUATE','TG/2024/2116','Batch 16',FALSE,FALSE),
('UG17','student17','student123','Nuwan Thushara','student17@fot.ruh.ac.lk','0780000017','ICT Department','UNDERGRADUATE','TG/2024/2117','Batch 15',TRUE,FALSE),
('UG18','student18','student123','Pawan Rathnayake','student18@fot.ruh.ac.lk','0780000018','ICT Department','UNDERGRADUATE','TG/2024/2118','Batch 15',TRUE,FALSE),
('UG19','student19','student123','Janith Liyanage','student19@fot.ruh.ac.lk','0780000019','ICT Department','UNDERGRADUATE','TG/2024/2119','Batch 15',FALSE,TRUE),
('UG20','student20','student123','Sadeera Samarawickrama','student20@fot.ruh.ac.lk','0780000020','ICT Department','UNDERGRADUATE','TG/2024/2120','Batch 15',FALSE,TRUE);

-- ICT2132 attendance sample for all 20 students.
-- With uq_attendance_session on attendance_records this block is now safe to re-run.
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2101','Kasun Rajapaksha','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2102','Nimali Wickramasinghe','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2103','Pathum Nissanka','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2104','Dilshan Madushanka','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2105','Kusal Mendis','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2106','Charith Asalanka','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2107','Wanindu Hasaranga','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2108','Maheesh Theekshana','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2109','Matheesha Pathirana','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2110','Dunith Wellalage','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2111','Kamindu Mendis','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2112','Asitha Fernando','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2113','Avishka Fernando','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2114','Bhanuka Rajapaksa','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2115','Dushmantha Chameera','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2116','Lahiru Kumara','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2117','Nuwan Thushara','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2118','Pawan Rathnayake','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2119','Janith Liyanage','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','THEORY',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',1,DATE_SUB(CURDATE(), INTERVAL 113 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',2,DATE_SUB(CURDATE(), INTERVAL 106 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',3,DATE_SUB(CURDATE(), INTERVAL 99 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',4,DATE_SUB(CURDATE(), INTERVAL 92 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',5,DATE_SUB(CURDATE(), INTERVAL 85 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',6,DATE_SUB(CURDATE(), INTERVAL 78 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',7,DATE_SUB(CURDATE(), INTERVAL 71 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',8,DATE_SUB(CURDATE(), INTERVAL 64 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',9,DATE_SUB(CURDATE(), INTERVAL 57 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',10,DATE_SUB(CURDATE(), INTERVAL 50 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',11,DATE_SUB(CURDATE(), INTERVAL 43 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',12,DATE_SUB(CURDATE(), INTERVAL 36 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',13,DATE_SUB(CURDATE(), INTERVAL 29 DAY),TRUE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',14,DATE_SUB(CURDATE(), INTERVAL 22 DAY),FALSE,FALSE,FALSE);
INSERT IGNORE INTO attendance_records(student_reg_no,student_name,course_code,component_type,session_number,session_date,is_present,has_medical_submitted,is_medical_approved) VALUES ('TG/2024/2120','Sadeera Samarawickrama','ICT2132','PRACTICAL',15,DATE_SUB(CURDATE(), INTERVAL 15 DAY),FALSE,FALSE,FALSE);

-- Assessment criteria templates (each assessment is marked out of 100).
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ICT2132','CA 1','CA',20);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ICT2132','CA 2','CA',30);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ICT2132','Final Examination','FINAL',100);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ICT2113','CA 1','CA',20);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ICT2113','CA 2','CA',30);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ICT2113','Final Examination','FINAL',100);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ICT2122','CA 1','CA',20);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ICT2122','CA 2','CA',30);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ICT2122','Final Examination','FINAL',100);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ENG2102','CA 1','CA',20);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ENG2102','CA 2','CA',30);
INSERT IGNORE INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES ('ENG2102','Final Examination','FINAL',100);

-- Complete attendance coverage for the current-semester sample modules and all seeded undergraduates.
-- FIX: MOD 3 & 4 students are marked absent for sessions 14 & 15 so that the demo medical records
--      (which target sessions 14 & 15) attach to genuinely missed sessions in every course.
INSERT INTO attendance_records
(student_reg_no, student_name, course_code, component_type, session_number, session_date, is_present, has_medical_submitted, is_medical_approved)
SELECT u.student_reg_no, u.full_name, c.course_code, cp.component_type, sn.session_number,
       DATE_SUB(CURDATE(), INTERVAL (120 - sn.session_number * 7) DAY),
       CASE
           WHEN MOD(CAST(SUBSTRING(u.user_id, 3) AS UNSIGNED), 5) = 0 AND sn.session_number > 10 THEN FALSE
           WHEN MOD(CAST(SUBSTRING(u.user_id, 3) AS UNSIGNED), 5) = 1 AND sn.session_number > 12 THEN FALSE
           WHEN MOD(CAST(SUBSTRING(u.user_id, 3) AS UNSIGNED), 5) = 2 AND sn.session_number > 3 THEN FALSE
           WHEN MOD(CAST(SUBSTRING(u.user_id, 3) AS UNSIGNED), 5) = 3 AND sn.session_number IN (14, 15) THEN FALSE
           WHEN MOD(CAST(SUBSTRING(u.user_id, 3) AS UNSIGNED), 5) = 4 AND sn.session_number IN (14, 15) THEN FALSE
           ELSE TRUE
           END,
       FALSE, -- has_medical_submitted: kept in step with medical_records by the sync statement at the end of this file
       FALSE  -- is_medical_approved
FROM users u
         JOIN courses c ON c.department = u.department
         JOIN (SELECT 'THEORY' component_type UNION ALL SELECT 'PRACTICAL') cp
         JOIN (SELECT 1 session_number UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15) sn
WHERE u.role='UNDERGRADUATE'
  AND u.student_reg_no IS NOT NULL
  AND ((cp.component_type='THEORY' AND c.theory_credits > 0) OR (cp.component_type='PRACTICAL' AND c.practical_credits > 0))
  AND NOT EXISTS (
    SELECT 1 FROM attendance_records ar
    WHERE ar.student_reg_no=u.student_reg_no AND ar.course_code=c.course_code
      AND ar.component_type=cp.component_type AND ar.session_number=sn.session_number
);

-- Ensure every current-semester module has a complete evaluation template; each component mark is out of 100.
INSERT IGNORE INTO assessments(course_code, assessment_name, assessment_type, weight_percentage)
SELECT c.course_code, seed.assessment_name, seed.assessment_type, seed.weight_percentage
FROM courses c
         CROSS JOIN (
    SELECT 'Assignment / Practical CA' assessment_name, 'CA' assessment_type, 20.00 weight_percentage
    UNION ALL SELECT 'Mid-semester Test', 'CA', 20.00
    UNION ALL SELECT 'Continuous Assessment Total', 'CA', 60.00
    UNION ALL SELECT 'Final Examination', 'FINAL', 100.00
) seed;

-- Sample medical records spanning theory and practical components, with mixed review states.
-- FIX: target sessions 14 & 15 (which are genuinely missed in the demo data) rather than 4 & 9
--      (which every demo student attended), so the sync UPDATE below actually sets attendance flags.
INSERT INTO medical_records
(student_reg_no, student_name, course_code, component_type, session_number, absent_date, reference_no, reason, submission_date, status, reviewed_by, image_path)
SELECT u.student_reg_no, u.full_name, c.course_code, cp.component_type, sn.session_number,
       DATE_SUB(CURDATE(), INTERVAL (120 - sn.session_number * 7) DAY),
       CONCAT('MED-', u.user_id, '-', c.course_code, '-', cp.component_type, '-', sn.session_number),
       'Demonstration medical record for attendance scenario testing', CURDATE(),
       CASE WHEN MOD(CAST(SUBSTRING(u.user_id, 3) AS UNSIGNED), 2)=0 THEN 'APPROVED' ELSE 'PENDING' END,
       CASE WHEN MOD(CAST(SUBSTRING(u.user_id, 3) AS UNSIGNED), 2)=0 THEN 'Demo Reviewer' ELSE NULL END,
       NULL
FROM users u JOIN courses c ON c.department=u.department
             JOIN (SELECT 'THEORY' component_type UNION ALL SELECT 'PRACTICAL') cp
             JOIN (SELECT 14 session_number UNION ALL SELECT 15 session_number) sn
WHERE u.role='UNDERGRADUATE' AND u.student_reg_no IS NOT NULL
  AND MOD(CAST(SUBSTRING(u.user_id, 3) AS UNSIGNED), 5) IN (3,4)
  AND ((cp.component_type='THEORY' AND c.theory_credits>0) OR (cp.component_type='PRACTICAL' AND c.practical_credits>0))
  AND (cp.component_type='THEORY' OR sn.session_number=14) -- max 3 medicals per student per subject
  AND NOT EXISTS (SELECT 1 FROM medical_records mr WHERE mr.reference_no=CONCAT('MED-',u.user_id,'-',c.course_code,'-',cp.component_type,'-',sn.session_number));

-- ---------------------------------------------------------------------------------------------
-- Keep attendance in step with medical records
--   PENDING  medical -> has_medical_submitted = TRUE,  is_medical_approved = FALSE
--   APPROVED medical -> has_medical_submitted = TRUE,  is_medical_approved = TRUE
--   REJECTED / no medical -> both FALSE
-- (The application does this automatically when a medical is uploaded / approved / rejected.)
-- ---------------------------------------------------------------------------------------------
UPDATE attendance_records SET has_medical_submitted = FALSE, is_medical_approved = FALSE WHERE record_id > 0;

UPDATE attendance_records ar
    JOIN medical_records mr
ON mr.student_reg_no = ar.student_reg_no AND mr.course_code = ar.course_code
    AND mr.component_type = ar.component_type AND mr.session_number = ar.session_number
    SET ar.has_medical_submitted = TRUE,
        ar.is_medical_approved   = (mr.status = 'APPROVED')
WHERE mr.status IN ('PENDING', 'APPROVED') AND ar.is_present = FALSE AND ar.record_id > 0;

-- ============================================================================
-- MARKS & GPA SUBSYSTEM ADDITIONS
-- Module: Marks & GPA Subsystem | UGC Commission Circular No.12-2024
-- ============================================================================

-- 9. Lecturer–course assignment table (admin grants a lecturer access to enter marks for a course)
CREATE TABLE IF NOT EXISTS lecturer_course_assignments (
                                                           assignment_id     INT AUTO_INCREMENT PRIMARY KEY,
                                                           lecturer_user_id  VARCHAR(50) NOT NULL,
    course_code       VARCHAR(20) NOT NULL,
    assigned_by       VARCHAR(50) NOT NULL DEFAULT 'ADM001',
    assigned_on       DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT fk_lca_lecturer FOREIGN KEY (lecturer_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_lca_course   FOREIGN KEY (course_code)      REFERENCES courses(course_code) ON DELETE CASCADE,
    UNIQUE KEY uq_lec_course (lecturer_user_id, course_code)
    );
-- Assign lecturers to courses (admin setup)
INSERT IGNORE INTO lecturer_course_assignments (lecturer_user_id, course_code, assigned_by) VALUES
('LEC001', 'ICT2132', 'ADM001'),
('LEC002', 'ICT2113', 'ADM001'),
('LEC005', 'ICT2122', 'ADM001'),
('LEC004', 'ENG2102', 'ADM001');

-- 10. The is_published column has been added directly to the marks table definition above.

-- 11. Course Enrollments — enrol all 20 students in all 4 courses
INSERT IGNORE INTO course_enrollments (student_user_id, course_code, enrolled_on, active) VALUES
-- Student UG1–UG20 in ICT2132
('UG1','ICT2132','2026-02-01',TRUE),('UG2','ICT2132','2026-02-01',TRUE),('UG3','ICT2132','2026-02-01',TRUE),
('UG4','ICT2132','2026-02-01',TRUE),('UG5','ICT2132','2026-02-01',TRUE),('UG6','ICT2132','2026-02-01',TRUE),
('UG7','ICT2132','2026-02-01',TRUE),('UG8','ICT2132','2026-02-01',TRUE),('UG9','ICT2132','2026-02-01',TRUE),
('UG10','ICT2132','2026-02-01',TRUE),('UG11','ICT2132','2026-02-01',TRUE),('UG12','ICT2132','2026-02-01',TRUE),
('UG13','ICT2132','2026-02-01',TRUE),('UG14','ICT2132','2026-02-01',TRUE),('UG15','ICT2132','2026-02-01',TRUE),
('UG16','ICT2132','2026-02-01',TRUE),('UG17','ICT2132','2026-02-01',TRUE),('UG18','ICT2132','2026-02-01',TRUE),
('UG19','ICT2132','2026-02-01',TRUE),('UG20','ICT2132','2026-02-01',TRUE),
-- Student UG1–UG20 in ICT2113
('UG1','ICT2113','2026-02-01',TRUE),('UG2','ICT2113','2026-02-01',TRUE),('UG3','ICT2113','2026-02-01',TRUE),
('UG4','ICT2113','2026-02-01',TRUE),('UG5','ICT2113','2026-02-01',TRUE),('UG6','ICT2113','2026-02-01',TRUE),
('UG7','ICT2113','2026-02-01',TRUE),('UG8','ICT2113','2026-02-01',TRUE),('UG9','ICT2113','2026-02-01',TRUE),
('UG10','ICT2113','2026-02-01',TRUE),('UG11','ICT2113','2026-02-01',TRUE),('UG12','ICT2113','2026-02-01',TRUE),
('UG13','ICT2113','2026-02-01',TRUE),('UG14','ICT2113','2026-02-01',TRUE),('UG15','ICT2113','2026-02-01',TRUE),
('UG16','ICT2113','2026-02-01',TRUE),('UG17','ICT2113','2026-02-01',TRUE),('UG18','ICT2113','2026-02-01',TRUE),
('UG19','ICT2113','2026-02-01',TRUE),('UG20','ICT2113','2026-02-01',TRUE),
-- Student UG1–UG20 in ICT2122
('UG1','ICT2122','2026-02-01',TRUE),('UG2','ICT2122','2026-02-01',TRUE),('UG3','ICT2122','2026-02-01',TRUE),
('UG4','ICT2122','2026-02-01',TRUE),('UG5','ICT2122','2026-02-01',TRUE),('UG6','ICT2122','2026-02-01',TRUE),
('UG7','ICT2122','2026-02-01',TRUE),('UG8','ICT2122','2026-02-01',TRUE),('UG9','ICT2122','2026-02-01',TRUE),
('UG10','ICT2122','2026-02-01',TRUE),('UG11','ICT2122','2026-02-01',TRUE),('UG12','ICT2122','2026-02-01',TRUE),
('UG13','ICT2122','2026-02-01',TRUE),('UG14','ICT2122','2026-02-01',TRUE),('UG15','ICT2122','2026-02-01',TRUE),
('UG16','ICT2122','2026-02-01',TRUE),('UG17','ICT2122','2026-02-01',TRUE),('UG18','ICT2122','2026-02-01',TRUE),
('UG19','ICT2122','2026-02-01',TRUE),('UG20','ICT2122','2026-02-01',TRUE),
-- Student UG1–UG20 in ENG2102
('UG1','ENG2102','2026-02-01',TRUE),('UG2','ENG2102','2026-02-01',TRUE),('UG3','ENG2102','2026-02-01',TRUE),
('UG4','ENG2102','2026-02-01',TRUE),('UG5','ENG2102','2026-02-01',TRUE),('UG6','ENG2102','2026-02-01',TRUE),
('UG7','ENG2102','2026-02-01',TRUE),('UG8','ENG2102','2026-02-01',TRUE),('UG9','ENG2102','2026-02-01',TRUE),
('UG10','ENG2102','2026-02-01',TRUE),('UG11','ENG2102','2026-02-01',TRUE),('UG12','ENG2102','2026-02-01',TRUE),
('UG13','ENG2102','2026-02-01',TRUE),('UG14','ENG2102','2026-02-01',TRUE),('UG15','ENG2102','2026-02-01',TRUE),
('UG16','ENG2102','2026-02-01',TRUE),('UG17','ENG2102','2026-02-01',TRUE),('UG18','ENG2102','2026-02-01',TRUE),
('UG19','ENG2102','2026-02-01',TRUE),('UG20','ENG2102','2026-02-01',TRUE);

-- ============================================================================
-- 12. MARKS SEED DATA
-- All marks out of 100. CA assessments have weight_percentage; final exams = 100 weight.
-- CA eligibility: CA weighted total >= 40%. Attendance eligibility: >= 80%.
-- Grades per UGC Commission Circular No. 12-2024:
--   A+(90+,4.0) A(80+,4.0) A-(75+,3.7) B+(70+,3.3) B(65+,3.0) B-(60+,2.7)
--   C+(55+,2.3) C(50+,2.0) C-(45+,1.7) D+(40+,1.3) D(35+,1.0) E(<35,0.0)
-- ============================================================================

-- ICT2132 – CA 1 (weight 20%) marks for all 20 students
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 85 WHEN 'UG2'  THEN 72 WHEN 'UG3'  THEN 35 WHEN 'UG4'  THEN 90
           WHEN 'UG5'  THEN 42 WHEN 'UG6'  THEN 78 WHEN 'UG7'  THEN 65 WHEN 'UG8'  THEN 55
           WHEN 'UG9'  THEN 88 WHEN 'UG10' THEN 70 WHEN 'UG11' THEN 48 WHEN 'UG12' THEN 92
           WHEN 'UG13' THEN 60 WHEN 'UG14' THEN 75 WHEN 'UG15' THEN 38 WHEN 'UG16' THEN 82
           WHEN 'UG17' THEN 55 WHEN 'UG18' THEN 68 WHEN 'UG19' THEN 45 WHEN 'UG20' THEN 77
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ICT2132' AND a.assessment_name='CA 1' AND a.assessment_type='CA'
  AND u.role='UNDERGRADUATE';

-- ICT2132 – CA 2 (weight 30%) marks
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 80 WHEN 'UG2'  THEN 68 WHEN 'UG3'  THEN 28 WHEN 'UG4'  THEN 88
           WHEN 'UG5'  THEN 45 WHEN 'UG6'  THEN 76 WHEN 'UG7'  THEN 62 WHEN 'UG8'  THEN 50
           WHEN 'UG9'  THEN 84 WHEN 'UG10' THEN 72 WHEN 'UG11' THEN 55 WHEN 'UG12' THEN 89
           WHEN 'UG13' THEN 65 WHEN 'UG14' THEN 78 WHEN 'UG15' THEN 32 WHEN 'UG16' THEN 79
           WHEN 'UG17' THEN 58 WHEN 'UG18' THEN 71 WHEN 'UG19' THEN 48 WHEN 'UG20' THEN 73
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ICT2132' AND a.assessment_name='CA 2' AND a.assessment_type='CA'
  AND u.role='UNDERGRADUATE';

-- ICT2132 – Final Examination (weight 100%)
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 78 WHEN 'UG2'  THEN 65 WHEN 'UG3'  THEN 30 WHEN 'UG4'  THEN 85
           WHEN 'UG5'  THEN 50 WHEN 'UG6'  THEN 72 WHEN 'UG7'  THEN 60 WHEN 'UG8'  THEN 55
           WHEN 'UG9'  THEN 82 WHEN 'UG10' THEN 67 WHEN 'UG11' THEN 48 WHEN 'UG12' THEN 90
           WHEN 'UG13' THEN 61 WHEN 'UG14' THEN 74 WHEN 'UG15' THEN 33 WHEN 'UG16' THEN 76
           WHEN 'UG17' THEN 53 WHEN 'UG18' THEN 69 WHEN 'UG19' THEN 44 WHEN 'UG20' THEN 71
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ICT2132' AND a.assessment_name='Final Examination' AND a.assessment_type='FINAL'
  AND u.role='UNDERGRADUATE';

-- ICT2113 – CA 1 (weight 20%)
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 88 WHEN 'UG2'  THEN 74 WHEN 'UG3'  THEN 55 WHEN 'UG4'  THEN 92
           WHEN 'UG5'  THEN 48 WHEN 'UG6'  THEN 80 WHEN 'UG7'  THEN 67 WHEN 'UG8'  THEN 58
           WHEN 'UG9'  THEN 86 WHEN 'UG10' THEN 73 WHEN 'UG11' THEN 50 WHEN 'UG12' THEN 95
           WHEN 'UG13' THEN 62 WHEN 'UG14' THEN 77 WHEN 'UG15' THEN 40 WHEN 'UG16' THEN 84
           WHEN 'UG17' THEN 57 WHEN 'UG18' THEN 70 WHEN 'UG19' THEN 46 WHEN 'UG20' THEN 79
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ICT2113' AND a.assessment_name='CA 1' AND a.assessment_type='CA'
  AND u.role='UNDERGRADUATE';

-- ICT2113 – CA 2 (weight 30%)
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 83 WHEN 'UG2'  THEN 70 WHEN 'UG3'  THEN 50 WHEN 'UG4'  THEN 89
           WHEN 'UG5'  THEN 44 WHEN 'UG6'  THEN 77 WHEN 'UG7'  THEN 63 WHEN 'UG8'  THEN 52
           WHEN 'UG9'  THEN 81 WHEN 'UG10' THEN 69 WHEN 'UG11' THEN 53 WHEN 'UG12' THEN 91
           WHEN 'UG13' THEN 64 WHEN 'UG14' THEN 75 WHEN 'UG15' THEN 38 WHEN 'UG16' THEN 80
           WHEN 'UG17' THEN 55 WHEN 'UG18' THEN 68 WHEN 'UG19' THEN 47 WHEN 'UG20' THEN 74
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ICT2113' AND a.assessment_name='CA 2' AND a.assessment_type='CA'
  AND u.role='UNDERGRADUATE';

-- ICT2113 – Final Examination
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 80 WHEN 'UG2'  THEN 67 WHEN 'UG3'  THEN 48 WHEN 'UG4'  THEN 86
           WHEN 'UG5'  THEN 46 WHEN 'UG6'  THEN 73 WHEN 'UG7'  THEN 61 WHEN 'UG8'  THEN 54
           WHEN 'UG9'  THEN 79 WHEN 'UG10' THEN 66 WHEN 'UG11' THEN 50 WHEN 'UG12' THEN 88
           WHEN 'UG13' THEN 62 WHEN 'UG14' THEN 72 WHEN 'UG15' THEN 35 WHEN 'UG16' THEN 77
           WHEN 'UG17' THEN 54 WHEN 'UG18' THEN 67 WHEN 'UG19' THEN 44 WHEN 'UG20' THEN 70
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ICT2113' AND a.assessment_name='Final Examination' AND a.assessment_type='FINAL'
  AND u.role='UNDERGRADUATE';

-- ICT2122 – CA 1 (weight 20%)
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 76 WHEN 'UG2'  THEN 62 WHEN 'UG3'  THEN 45 WHEN 'UG4'  THEN 87
           WHEN 'UG5'  THEN 50 WHEN 'UG6'  THEN 74 WHEN 'UG7'  THEN 59 WHEN 'UG8'  THEN 48
           WHEN 'UG9'  THEN 83 WHEN 'UG10' THEN 65 WHEN 'UG11' THEN 52 WHEN 'UG12' THEN 90
           WHEN 'UG13' THEN 58 WHEN 'UG14' THEN 71 WHEN 'UG15' THEN 38 WHEN 'UG16' THEN 78
           WHEN 'UG17' THEN 54 WHEN 'UG18' THEN 66 WHEN 'UG19' THEN 43 WHEN 'UG20' THEN 72
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ICT2122' AND a.assessment_name='CA 1' AND a.assessment_type='CA'
  AND u.role='UNDERGRADUATE';

-- ICT2122 – CA 2 (weight 30%)
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 79 WHEN 'UG2'  THEN 64 WHEN 'UG3'  THEN 42 WHEN 'UG4'  THEN 85
           WHEN 'UG5'  THEN 48 WHEN 'UG6'  THEN 72 WHEN 'UG7'  THEN 57 WHEN 'UG8'  THEN 46
           WHEN 'UG9'  THEN 80 WHEN 'UG10' THEN 63 WHEN 'UG11' THEN 55 WHEN 'UG12' THEN 88
           WHEN 'UG13' THEN 60 WHEN 'UG14' THEN 73 WHEN 'UG15' THEN 36 WHEN 'UG16' THEN 76
           WHEN 'UG17' THEN 52 WHEN 'UG18' THEN 64 WHEN 'UG19' THEN 45 WHEN 'UG20' THEN 70
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ICT2122' AND a.assessment_name='CA 2' AND a.assessment_type='CA'
  AND u.role='UNDERGRADUATE';

-- ICT2122 – Final Examination
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 74 WHEN 'UG2'  THEN 60 WHEN 'UG3'  THEN 39 WHEN 'UG4'  THEN 83
           WHEN 'UG5'  THEN 46 WHEN 'UG6'  THEN 70 WHEN 'UG7'  THEN 55 WHEN 'UG8'  THEN 44
           WHEN 'UG9'  THEN 77 WHEN 'UG10' THEN 61 WHEN 'UG11' THEN 50 WHEN 'UG12' THEN 86
           WHEN 'UG13' THEN 58 WHEN 'UG14' THEN 70 WHEN 'UG15' THEN 33 WHEN 'UG16' THEN 73
           WHEN 'UG17' THEN 50 WHEN 'UG18' THEN 62 WHEN 'UG19' THEN 42 WHEN 'UG20' THEN 68
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ICT2122' AND a.assessment_name='Final Examination' AND a.assessment_type='FINAL'
  AND u.role='UNDERGRADUATE';

-- ENG2102 – CA 1 (weight 20%)
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 82 WHEN 'UG2'  THEN 69 WHEN 'UG3'  THEN 48 WHEN 'UG4'  THEN 91
           WHEN 'UG5'  THEN 53 WHEN 'UG6'  THEN 78 WHEN 'UG7'  THEN 64 WHEN 'UG8'  THEN 56
           WHEN 'UG9'  THEN 85 WHEN 'UG10' THEN 68 WHEN 'UG11' THEN 54 WHEN 'UG12' THEN 93
           WHEN 'UG13' THEN 63 WHEN 'UG14' THEN 76 WHEN 'UG15' THEN 41 WHEN 'UG16' THEN 81
           WHEN 'UG17' THEN 59 WHEN 'UG18' THEN 72 WHEN 'UG19' THEN 47 WHEN 'UG20' THEN 75
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ENG2102' AND a.assessment_name='CA 1' AND a.assessment_type='CA'
  AND u.role='UNDERGRADUATE';

-- ENG2102 – CA 2 (weight 30%)
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 84 WHEN 'UG2'  THEN 71 WHEN 'UG3'  THEN 44 WHEN 'UG4'  THEN 88
           WHEN 'UG5'  THEN 50 WHEN 'UG6'  THEN 75 WHEN 'UG7'  THEN 62 WHEN 'UG8'  THEN 53
           WHEN 'UG9'  THEN 83 WHEN 'UG10' THEN 70 WHEN 'UG11' THEN 56 WHEN 'UG12' THEN 90
           WHEN 'UG13' THEN 65 WHEN 'UG14' THEN 74 WHEN 'UG15' THEN 37 WHEN 'UG16' THEN 82
           WHEN 'UG17' THEN 56 WHEN 'UG18' THEN 69 WHEN 'UG19' THEN 46 WHEN 'UG20' THEN 76
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ENG2102' AND a.assessment_name='CA 2' AND a.assessment_type='CA'
  AND u.role='UNDERGRADUATE';

-- ENG2102 – Final Examination
INSERT IGNORE INTO marks (assessment_id, student_user_id, mark, is_published)
SELECT a.assessment_id, u.user_id,
       CASE u.user_id
           WHEN 'UG1'  THEN 81 WHEN 'UG2'  THEN 68 WHEN 'UG3'  THEN 42 WHEN 'UG4'  THEN 87
           WHEN 'UG5'  THEN 49 WHEN 'UG6'  THEN 73 WHEN 'UG7'  THEN 60 WHEN 'UG8'  THEN 52
           WHEN 'UG9'  THEN 80 WHEN 'UG10' THEN 66 WHEN 'UG11' THEN 54 WHEN 'UG12' THEN 89
           WHEN 'UG13' THEN 63 WHEN 'UG14' THEN 71 WHEN 'UG15' THEN 34 WHEN 'UG16' THEN 79
           WHEN 'UG17' THEN 53 WHEN 'UG18' THEN 66 WHEN 'UG19' THEN 43 WHEN 'UG20' THEN 72
           ELSE 60
           END AS mark,
       TRUE AS is_published
FROM assessments a
         CROSS JOIN users u
WHERE a.course_code='ENG2102' AND a.assessment_name='Final Examination' AND a.assessment_type='FINAL'
  AND u.role='UNDERGRADUATE';

-- ============================================================================
-- END OF MARKS & GPA SUBSYSTEM ADDITIONS
-- Demo grade summary (for reference):
--   UG3 (Pathum Nissanka) has low CA scores (ICT2132 CA1=35, CA2=28 → weighted ~26%) → NOT ELIGIBLE (CA<40%)
--   UG15 (Dushmantha Chameera) has CA1=38,CA2=32 → weighted ~27.6% → NOT ELIGIBLE (CA<40%)
--   Most students have attendance patterns from above attendance seed → check eligibility tab.
-- ============================================================================


-- 2) MARKS, GPA & EXAM ELIGIBILITY
-- Source file: marks_eligibility_v2_fixed.sql

-- FAMS academic marks, GPA and exam-eligibility upgrade.
-- Run AFTER database_schema.sql. Existing GUI layouts are intentionally unchanged.
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS academic_semesters (
                                                  semester_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  semester_code VARCHAR(20) NOT NULL UNIQUE,
    academic_year VARCHAR(20) NOT NULL,
    semester_no TINYINT NOT NULL,
    starts_on DATE NULL, ends_on DATE NULL
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS module_result_config (
                                                    course_code VARCHAR(20) PRIMARY KEY,
    ca_weight DECIMAL(5,2) NOT NULL DEFAULT 30.00,
    esa_weight DECIMAL(5,2) NOT NULL DEFAULT 70.00,
    ca_pass DECIMAL(5,2) NOT NULL DEFAULT 35.00,
    esa_pass DECIMAL(5,2) NOT NULL DEFAULT 40.00,
    gpa_module BOOLEAN NOT NULL DEFAULT TRUE,
    english_option VARCHAR(20) NULL,
    CHECK (ca_weight BETWEEN 0 AND 100),
    CHECK (esa_weight BETWEEN 0 AND 100),
    CHECK (ca_weight + esa_weight = 100),
    CHECK (ca_pass BETWEEN 0 AND 100),
    CHECK (esa_pass BETWEEN 0 AND 100),
    CONSTRAINT fk_mrc_course FOREIGN KEY(course_code) REFERENCES courses(course_code) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS grade_boundary_config (
                                                     boundary_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     course_code VARCHAR(20) NULL,
    grade_code VARCHAR(3) NOT NULL,
    minimum_mark DECIMAL(5,2) NOT NULL,
    grade_point DECIMAL(3,2) NOT NULL,
    UNIQUE KEY uq_grade_boundary_module(course_code,grade_code),
    CONSTRAINT fk_gbc_course FOREIGN KEY(course_code) REFERENCES courses(course_code) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS academic_system_config (
                                                      config_key VARCHAR(60) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL,
    updated_by VARCHAR(50) NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS academic_assessments (
                                                    assessment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                    course_code VARCHAR(20) NOT NULL,
    semester_id BIGINT NOT NULL,
    assessment_name VARCHAR(120) NOT NULL,
    component ENUM('QUIZ','ASSIGNMENT','MID_TERM','NORMAL_ASSIGN','CA','ESA') NOT NULL,
    quiz_no TINYINT NULL,
    weight_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aa_course FOREIGN KEY(course_code) REFERENCES courses(course_code),
    CONSTRAINT fk_aa_sem FOREIGN KEY(semester_id) REFERENCES academic_semesters(semester_id),
    CONSTRAINT chk_aa_weight CHECK(weight_percentage BETWEEN 0 AND 100),
    CONSTRAINT chk_aa_quiz CHECK(quiz_no IS NULL OR quiz_no BETWEEN 1 AND 3),
    CONSTRAINT chk_aa_component CHECK(component IN ('QUIZ','ASSIGNMENT','MID_TERM','NORMAL_ASSIGN','CA','ESA')),
    CONSTRAINT chk_aa_quiz_name CHECK((component IN ('QUIZ','MID_TERM') AND quiz_no IS NOT NULL AND quiz_no BETWEEN 1 AND 3) OR (component IN ('ASSIGNMENT','NORMAL_ASSIGN','CA','ESA') AND quiz_no IS NULL)),
    UNIQUE KEY uq_assessment_quiz(course_code,semester_id,component,quiz_no),
    UNIQUE KEY uq_assessment_name(course_code,semester_id,assessment_name)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS academic_marks (
                                              mark_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              assessment_id BIGINT NOT NULL,
                                              student_user_id VARCHAR(50) NOT NULL,
    attempt_no INT NOT NULL DEFAULT 1,
    mark DECIMAL(5,2) NOT NULL,
    entered_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_am_assessment FOREIGN KEY(assessment_id) REFERENCES academic_assessments(assessment_id) ON DELETE CASCADE,
    CONSTRAINT fk_am_student FOREIGN KEY(student_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_am_mark CHECK(mark BETWEEN 0 AND 100),
    CONSTRAINT chk_am_attempt CHECK(attempt_no >= 1),
    UNIQUE KEY uq_academic_mark_attempt(assessment_id,student_user_id,attempt_no)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS result_publish_requests (
                                                       request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                       course_code VARCHAR(20) NOT NULL,
    semester_id BIGINT NOT NULL,
    lecturer_user_id VARCHAR(50) NOT NULL,
    status ENUM('DRAFT','SUBMITTED','APPROVED','REJECTED') NOT NULL DEFAULT 'DRAFT',
    submitted_at DATETIME NULL,
    reviewed_by VARCHAR(50) NULL,
    reviewed_at DATETIME NULL,
    review_reason VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rpr_course FOREIGN KEY(course_code) REFERENCES courses(course_code),
    CONSTRAINT fk_rpr_sem FOREIGN KEY(semester_id) REFERENCES academic_semesters(semester_id),
    CONSTRAINT fk_rpr_lecturer FOREIGN KEY(lecturer_user_id) REFERENCES users(user_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS academic_results (
                                                result_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                student_user_id VARCHAR(50) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    semester_id BIGINT NOT NULL,
    attempt_no INT NOT NULL DEFAULT 1,
    ca_component_grade VARCHAR(12) NULL,
    esa_component_grade VARCHAR(12) NULL,
    final_mark DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    grade_code VARCHAR(12) NOT NULL,
    grade_point DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    credits INT NOT NULL,
    gpa_module BOOLEAN NOT NULL DEFAULT TRUE,
    is_best BOOLEAN NOT NULL DEFAULT TRUE,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    approved_request_id BIGINT NULL,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ar_student FOREIGN KEY(student_user_id) REFERENCES users(user_id),
    CONSTRAINT fk_ar_course FOREIGN KEY(course_code) REFERENCES courses(course_code),
    CONSTRAINT fk_ar_sem FOREIGN KEY(semester_id) REFERENCES academic_semesters(semester_id),
    CONSTRAINT chk_ar_final CHECK(final_mark BETWEEN 0 AND 100),
    CONSTRAINT chk_ar_credits CHECK(credits > 0),
    UNIQUE KEY uq_ar_attempt(student_user_id,course_code,semester_id,attempt_no)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS eligibility_snapshots (
                                                     snapshot_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     student_user_id VARCHAR(50) NOT NULL,
    semester_id BIGINT NOT NULL,
    gpa_used DECIMAL(4,2) NOT NULL,
    sgpa DECIMAL(4,2) NOT NULL,
    cgpa DECIMAL(4,2) NOT NULL,
    status ENUM('ELIGIBLE','NOT_ELIGIBLE') NOT NULL,
    checked_by VARCHAR(50) NOT NULL,
    checked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    override_reason VARCHAR(1000) NULL,
    override_by VARCHAR(50) NULL,
    CONSTRAINT fk_es_student FOREIGN KEY(student_user_id) REFERENCES users(user_id),
    CONSTRAINT fk_es_sem FOREIGN KEY(semester_id) REFERENCES academic_semesters(semester_id),
    KEY idx_eligibility_lookup(student_user_id,semester_id,checked_at)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS progression_snapshots (
                                                     progression_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     student_user_id VARCHAR(50) NOT NULL,
    target_semester_id BIGINT NOT NULL,
    first_three_cgpa DECIMAL(4,2) NOT NULL,
    can_register BOOLEAN NOT NULL,
    checked_by VARCHAR(50) NOT NULL,
    checked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ps_student FOREIGN KEY(student_user_id) REFERENCES users(user_id),
    CONSTRAINT fk_ps_sem FOREIGN KEY(target_semester_id) REFERENCES academic_semesters(semester_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS academic_audit_log (
                                                  audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  actor_user_id VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    old_value TEXT NULL,
    new_value TEXT NULL,
    reason VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_entity(entity_type,entity_id,created_at)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_notifications (
                                                  notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  user_id VARCHAR(50) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_un_user FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS email_queue (
                                           email_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status ENUM('PENDING','SENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at DATETIME NULL
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS grade_improvement_advisories (
                                                            advisory_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                            student_user_id VARCHAR(50) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    current_result_id BIGINT NOT NULL,
    recommended_action VARCHAR(500) NOT NULL,
    is_notified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gia_student FOREIGN KEY(student_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_gia_course FOREIGN KEY(course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    CONSTRAINT fk_gia_result FOREIGN KEY(current_result_id) REFERENCES academic_results(result_id) ON DELETE CASCADE
    ) ENGINE=InnoDB;


-- =========================================================================
-- IMMUTABILITY TRIGGERS FOR ELIGIBILITY SNAPSHOTS
-- =========================================================================

DROP TRIGGER IF EXISTS trg_eligibility_snapshot_no_delete;
DROP TRIGGER IF EXISTS trg_eligibility_snapshot_no_update;

DELIMITER //

-- Prevent deletions from the snapshot table
CREATE TRIGGER trg_eligibility_snapshot_no_delete
    BEFORE DELETE ON eligibility_snapshots
    FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Eligibility snapshots cannot be deleted.';
END//

-- Prevent modifications to historical snapshot values
CREATE TRIGGER trg_eligibility_snapshot_no_update
    BEFORE UPDATE ON eligibility_snapshots
    FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Eligibility snapshots cannot be updated.';
END//

DELIMITER ;


-- ============================================================================
-- ACADEMIC MARKS DEMO DATA (idempotent)
-- Adds complete marks, approved results and eligibility snapshots for the
-- 20 sample undergraduates across the 4 sample courses.
-- Lecturer/admin views use academic_assessments + academic_marks;
-- student views are unlocked only for Admin-approved academic_results.
-- ============================================================================
INSERT IGNORE INTO academic_semesters (semester_code, academic_year, semester_no, starts_on, ends_on)
VALUES ('Y1S1', '2026/2027', 1, '2026-02-01', '2026-06-30');

-- The original check omitted CA and ESA from valid NULL-quiz components.
SET @drop_chk = IF(
  EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
          WHERE CONSTRAINT_SCHEMA = DATABASE()
            AND TABLE_NAME = 'academic_assessments'
            AND CONSTRAINT_NAME = 'chk_aa_quiz_name'
            AND CONSTRAINT_TYPE = 'CHECK'),
  IF(VERSION() LIKE '%MariaDB%',
     'ALTER TABLE academic_assessments DROP CONSTRAINT chk_aa_quiz_name',
     'ALTER TABLE academic_assessments DROP CHECK chk_aa_quiz_name'),
  'SELECT 1');
PREPARE stmt_drop_chk FROM @drop_chk;
EXECUTE stmt_drop_chk;
DEALLOCATE PREPARE stmt_drop_chk;
ALTER TABLE academic_assessments ADD CONSTRAINT chk_aa_quiz_name CHECK (
  (component IN ('QUIZ','MID_TERM') AND quiz_no IS NOT NULL AND quiz_no BETWEEN 1 AND 3)
  OR (component IN ('ASSIGNMENT','NORMAL_ASSIGN','CA','ESA') AND quiz_no IS NULL)
);

INSERT IGNORE INTO module_result_config (course_code, ca_weight, esa_weight, ca_pass, esa_pass, gpa_module)
SELECT course_code, 30.00, 70.00, 40.00, 40.00, TRUE FROM courses
WHERE course_code IN ('ICT2132','ICT2113','ICT2122','ENG2102');

INSERT IGNORE INTO academic_system_config (config_key, config_value, updated_by) VALUES
('eligibility.threshold','2.00','ADM001'),
('progression.semester5_min_cgpa','2.00','ADM001');

INSERT IGNORE INTO grade_boundary_config (course_code, grade_code, minimum_mark, grade_point) VALUES
(NULL,'A+',90.00,4.00),(NULL,'A',80.00,4.00),(NULL,'A-',75.00,3.70),
(NULL,'B+',70.00,3.30),(NULL,'B',65.00,3.00),(NULL,'B-',60.00,2.70),
(NULL,'C+',55.00,2.30),(NULL,'C',50.00,2.00),(NULL,'C-',45.00,1.70),
(NULL,'D+',40.00,1.30),(NULL,'D',35.00,1.00),(NULL,'E',0.00,0.00);

-- Three assessments per course: Quiz 1 (10%), Assignment 1 (20%), ESA (70%).
INSERT IGNORE INTO academic_assessments
(course_code, semester_id, assessment_name, component, quiz_no, weight_percentage, created_by)
SELECT c.course_code, s.semester_id, seed.assessment_name, seed.component, seed.quiz_no, seed.weight_pct, seed.created_by
FROM courses c CROSS JOIN academic_semesters s CROSS JOIN (
  SELECT 'Quiz 1' assessment_name, 'QUIZ' component, 1 quiz_no, 10.00 weight_pct, 'LEC001' created_by
  UNION ALL SELECT 'Assignment 1','ASSIGNMENT',NULL,20.00,'LEC001'
  UNION ALL SELECT 'End Semester Examination','ESA',NULL,70.00,'LEC001'
) seed
WHERE s.semester_code='Y1S1' AND c.course_code IN ('ICT2132','ICT2113','ICT2122','ENG2102');

-- ICT2132 / Quiz 1
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 56 WHEN 'UG2' THEN 67 WHEN 'UG3' THEN 30 WHEN 'UG4' THEN 89 WHEN 'UG5' THEN 50 WHEN 'UG6' THEN 61 WHEN 'UG7' THEN 72 WHEN 'UG8' THEN 83 WHEN 'UG9' THEN 94 WHEN 'UG10' THEN 55 WHEN 'UG11' THEN 66 WHEN 'UG12' THEN 77 WHEN 'UG13' THEN 88 WHEN 'UG14' THEN 49 WHEN 'UG15' THEN 35 WHEN 'UG16' THEN 71 WHEN 'UG17' THEN 82 WHEN 'UG18' THEN 93 WHEN 'UG19' THEN 54 WHEN 'UG20' THEN 65 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ICT2132' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='Quiz 1' AND u.role='UNDERGRADUATE';

-- ICT2132 / Assignment 1
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 53 WHEN 'UG2' THEN 66 WHEN 'UG3' THEN 25 WHEN 'UG4' THEN 92 WHEN 'UG5' THEN 49 WHEN 'UG6' THEN 62 WHEN 'UG7' THEN 75 WHEN 'UG8' THEN 88 WHEN 'UG9' THEN 45 WHEN 'UG10' THEN 58 WHEN 'UG11' THEN 71 WHEN 'UG12' THEN 84 WHEN 'UG13' THEN 41 WHEN 'UG14' THEN 54 WHEN 'UG15' THEN 32 WHEN 'UG16' THEN 80 WHEN 'UG17' THEN 93 WHEN 'UG18' THEN 50 WHEN 'UG19' THEN 63 WHEN 'UG20' THEN 76 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ICT2132' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='Assignment 1' AND u.role='UNDERGRADUATE';

-- ICT2132 / End Semester Examination
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 59 WHEN 'UG2' THEN 76 WHEN 'UG3' THEN 38 WHEN 'UG4' THEN 55 WHEN 'UG5' THEN 72 WHEN 'UG6' THEN 89 WHEN 'UG7' THEN 51 WHEN 'UG8' THEN 68 WHEN 'UG9' THEN 85 WHEN 'UG10' THEN 47 WHEN 'UG11' THEN 64 WHEN 'UG12' THEN 81 WHEN 'UG13' THEN 43 WHEN 'UG14' THEN 60 WHEN 'UG15' THEN 35 WHEN 'UG16' THEN 94 WHEN 'UG17' THEN 56 WHEN 'UG18' THEN 73 WHEN 'UG19' THEN 90 WHEN 'UG20' THEN 52 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ICT2132' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='End Semester Examination' AND u.role='UNDERGRADUATE';

-- ICT2113 / Quiz 1
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 63 WHEN 'UG2' THEN 74 WHEN 'UG3' THEN 30 WHEN 'UG4' THEN 46 WHEN 'UG5' THEN 57 WHEN 'UG6' THEN 68 WHEN 'UG7' THEN 79 WHEN 'UG8' THEN 90 WHEN 'UG9' THEN 51 WHEN 'UG10' THEN 62 WHEN 'UG11' THEN 73 WHEN 'UG12' THEN 84 WHEN 'UG13' THEN 45 WHEN 'UG14' THEN 56 WHEN 'UG15' THEN 35 WHEN 'UG16' THEN 78 WHEN 'UG17' THEN 89 WHEN 'UG18' THEN 50 WHEN 'UG19' THEN 61 WHEN 'UG20' THEN 72 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ICT2113' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='Quiz 1' AND u.role='UNDERGRADUATE';

-- ICT2113 / Assignment 1
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 62 WHEN 'UG2' THEN 75 WHEN 'UG3' THEN 25 WHEN 'UG4' THEN 45 WHEN 'UG5' THEN 58 WHEN 'UG6' THEN 71 WHEN 'UG7' THEN 84 WHEN 'UG8' THEN 41 WHEN 'UG9' THEN 54 WHEN 'UG10' THEN 67 WHEN 'UG11' THEN 80 WHEN 'UG12' THEN 93 WHEN 'UG13' THEN 50 WHEN 'UG14' THEN 63 WHEN 'UG15' THEN 32 WHEN 'UG16' THEN 89 WHEN 'UG17' THEN 46 WHEN 'UG18' THEN 59 WHEN 'UG19' THEN 72 WHEN 'UG20' THEN 85 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ICT2113' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='Assignment 1' AND u.role='UNDERGRADUATE';

-- ICT2113 / End Semester Examination
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 64 WHEN 'UG2' THEN 81 WHEN 'UG3' THEN 38 WHEN 'UG4' THEN 60 WHEN 'UG5' THEN 77 WHEN 'UG6' THEN 94 WHEN 'UG7' THEN 56 WHEN 'UG8' THEN 73 WHEN 'UG9' THEN 90 WHEN 'UG10' THEN 52 WHEN 'UG11' THEN 69 WHEN 'UG12' THEN 86 WHEN 'UG13' THEN 48 WHEN 'UG14' THEN 65 WHEN 'UG15' THEN 35 WHEN 'UG16' THEN 44 WHEN 'UG17' THEN 61 WHEN 'UG18' THEN 78 WHEN 'UG19' THEN 95 WHEN 'UG20' THEN 57 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ICT2113' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='End Semester Examination' AND u.role='UNDERGRADUATE';

-- ICT2122 / Quiz 1
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 70 WHEN 'UG2' THEN 81 WHEN 'UG3' THEN 30 WHEN 'UG4' THEN 53 WHEN 'UG5' THEN 64 WHEN 'UG6' THEN 75 WHEN 'UG7' THEN 86 WHEN 'UG8' THEN 47 WHEN 'UG9' THEN 58 WHEN 'UG10' THEN 69 WHEN 'UG11' THEN 80 WHEN 'UG12' THEN 91 WHEN 'UG13' THEN 52 WHEN 'UG14' THEN 63 WHEN 'UG15' THEN 35 WHEN 'UG16' THEN 85 WHEN 'UG17' THEN 46 WHEN 'UG18' THEN 57 WHEN 'UG19' THEN 68 WHEN 'UG20' THEN 79 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ICT2122' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='Quiz 1' AND u.role='UNDERGRADUATE';

-- ICT2122 / Assignment 1
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 71 WHEN 'UG2' THEN 84 WHEN 'UG3' THEN 25 WHEN 'UG4' THEN 54 WHEN 'UG5' THEN 67 WHEN 'UG6' THEN 80 WHEN 'UG7' THEN 93 WHEN 'UG8' THEN 50 WHEN 'UG9' THEN 63 WHEN 'UG10' THEN 76 WHEN 'UG11' THEN 89 WHEN 'UG12' THEN 46 WHEN 'UG13' THEN 59 WHEN 'UG14' THEN 72 WHEN 'UG15' THEN 32 WHEN 'UG16' THEN 42 WHEN 'UG17' THEN 55 WHEN 'UG18' THEN 68 WHEN 'UG19' THEN 81 WHEN 'UG20' THEN 94 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ICT2122' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='Assignment 1' AND u.role='UNDERGRADUATE';

-- ICT2122 / End Semester Examination
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 69 WHEN 'UG2' THEN 86 WHEN 'UG3' THEN 38 WHEN 'UG4' THEN 65 WHEN 'UG5' THEN 82 WHEN 'UG6' THEN 44 WHEN 'UG7' THEN 61 WHEN 'UG8' THEN 78 WHEN 'UG9' THEN 95 WHEN 'UG10' THEN 57 WHEN 'UG11' THEN 74 WHEN 'UG12' THEN 91 WHEN 'UG13' THEN 53 WHEN 'UG14' THEN 70 WHEN 'UG15' THEN 35 WHEN 'UG16' THEN 49 WHEN 'UG17' THEN 66 WHEN 'UG18' THEN 83 WHEN 'UG19' THEN 45 WHEN 'UG20' THEN 62 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ICT2122' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='End Semester Examination' AND u.role='UNDERGRADUATE';

-- ENG2102 / Quiz 1
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 77 WHEN 'UG2' THEN 88 WHEN 'UG3' THEN 30 WHEN 'UG4' THEN 60 WHEN 'UG5' THEN 71 WHEN 'UG6' THEN 82 WHEN 'UG7' THEN 93 WHEN 'UG8' THEN 54 WHEN 'UG9' THEN 65 WHEN 'UG10' THEN 76 WHEN 'UG11' THEN 87 WHEN 'UG12' THEN 48 WHEN 'UG13' THEN 59 WHEN 'UG14' THEN 70 WHEN 'UG15' THEN 35 WHEN 'UG16' THEN 92 WHEN 'UG17' THEN 53 WHEN 'UG18' THEN 64 WHEN 'UG19' THEN 75 WHEN 'UG20' THEN 86 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ENG2102' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='Quiz 1' AND u.role='UNDERGRADUATE';

-- ENG2102 / Assignment 1
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 80 WHEN 'UG2' THEN 93 WHEN 'UG3' THEN 25 WHEN 'UG4' THEN 63 WHEN 'UG5' THEN 76 WHEN 'UG6' THEN 89 WHEN 'UG7' THEN 46 WHEN 'UG8' THEN 59 WHEN 'UG9' THEN 72 WHEN 'UG10' THEN 85 WHEN 'UG11' THEN 42 WHEN 'UG12' THEN 55 WHEN 'UG13' THEN 68 WHEN 'UG14' THEN 81 WHEN 'UG15' THEN 32 WHEN 'UG16' THEN 51 WHEN 'UG17' THEN 64 WHEN 'UG18' THEN 77 WHEN 'UG19' THEN 90 WHEN 'UG20' THEN 47 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ENG2102' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='Assignment 1' AND u.role='UNDERGRADUATE';

-- ENG2102 / End Semester Examination
INSERT IGNORE INTO academic_marks (assessment_id, student_user_id, attempt_no, mark, entered_by)
SELECT a.assessment_id, u.user_id, 1, CASE u.user_id WHEN 'UG1' THEN 74 WHEN 'UG2' THEN 91 WHEN 'UG3' THEN 38 WHEN 'UG4' THEN 70 WHEN 'UG5' THEN 87 WHEN 'UG6' THEN 49 WHEN 'UG7' THEN 66 WHEN 'UG8' THEN 83 WHEN 'UG9' THEN 45 WHEN 'UG10' THEN 62 WHEN 'UG11' THEN 79 WHEN 'UG12' THEN 96 WHEN 'UG13' THEN 58 WHEN 'UG14' THEN 75 WHEN 'UG15' THEN 35 WHEN 'UG16' THEN 54 WHEN 'UG17' THEN 71 WHEN 'UG18' THEN 88 WHEN 'UG19' THEN 50 WHEN 'UG20' THEN 67 ELSE 60 END, 'LEC001'
FROM academic_assessments a CROSS JOIN users u
WHERE a.course_code='ENG2102' AND a.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND a.assessment_name='End Semester Examination' AND u.role='UNDERGRADUATE';

-- Calculate and insert final results from the seeded CA and ESA marks.
INSERT INTO academic_results
(student_user_id, course_code, semester_id, attempt_no, ca_component_grade, esa_component_grade,
 final_mark, grade_code, grade_point, credits, gpa_module, is_best, is_approved)
SELECT calc.student_user_id, calc.course_code, calc.semester_id, 1, NULL, NULL, calc.final_mark,
  CASE WHEN calc.final_mark>=90 THEN 'A+' WHEN calc.final_mark>=80 THEN 'A'
       WHEN calc.final_mark>=75 THEN 'A-' WHEN calc.final_mark>=70 THEN 'B+'
       WHEN calc.final_mark>=65 THEN 'B' WHEN calc.final_mark>=60 THEN 'B-'
       WHEN calc.final_mark>=55 THEN 'C+' WHEN calc.final_mark>=50 THEN 'C'
       WHEN calc.final_mark>=45 THEN 'C-' WHEN calc.final_mark>=40 THEN 'D+'
       WHEN calc.final_mark>=35 THEN 'D' ELSE 'E' END,
  CASE WHEN calc.final_mark>=90 THEN 4.00 WHEN calc.final_mark>=80 THEN 4.00
       WHEN calc.final_mark>=75 THEN 3.70 WHEN calc.final_mark>=70 THEN 3.30
       WHEN calc.final_mark>=65 THEN 3.00 WHEN calc.final_mark>=60 THEN 2.70
       WHEN calc.final_mark>=55 THEN 2.30 WHEN calc.final_mark>=50 THEN 2.00
       WHEN calc.final_mark>=45 THEN 1.70 WHEN calc.final_mark>=40 THEN 1.30
       WHEN calc.final_mark>=35 THEN 1.00 ELSE 0.00 END,
  COALESCE(c.theory_credits,0)+COALESCE(c.practical_credits,0), TRUE, TRUE, TRUE
FROM (
  SELECT q.student_user_id, aq.course_code, aq.semester_id,
    ROUND((((q.mark*aq.weight_percentage)+(assm.mark*aa.weight_percentage)) /
      NULLIF(aq.weight_percentage+aa.weight_percentage,0))*0.30 + esa.mark*0.70, 2) AS final_mark
  FROM academic_assessments aq
  JOIN academic_marks q ON q.assessment_id=aq.assessment_id AND q.attempt_no=1
  JOIN academic_assessments aa ON aa.course_code=aq.course_code AND aa.semester_id=aq.semester_id AND aa.component='ASSIGNMENT'
  JOIN academic_marks assm ON assm.assessment_id=aa.assessment_id AND assm.student_user_id=q.student_user_id AND assm.attempt_no=1
  JOIN academic_assessments ae ON ae.course_code=aq.course_code AND ae.semester_id=aq.semester_id AND ae.component='ESA'
  JOIN academic_marks esa ON esa.assessment_id=ae.assessment_id AND esa.student_user_id=q.student_user_id AND esa.attempt_no=1
  WHERE aq.component='QUIZ' AND aq.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
) calc JOIN courses c ON c.course_code=calc.course_code
ON DUPLICATE KEY UPDATE final_mark=VALUES(final_mark), grade_code=VALUES(grade_code),
  grade_point=VALUES(grade_point), is_best=TRUE, is_approved=TRUE;

-- One immutable GPA eligibility snapshot per sample student.
INSERT INTO eligibility_snapshots (student_user_id, semester_id, gpa_used, sgpa, cgpa, status, checked_by)
SELECT r.student_user_id, r.semester_id, ROUND(AVG(r.grade_point),2), ROUND(AVG(r.grade_point),2),
       ROUND(AVG(r.grade_point),2),
       CASE WHEN AVG(r.grade_point)>=2.00 THEN 'ELIGIBLE' ELSE 'NOT_ELIGIBLE' END, 'ADM001'
FROM academic_results r
WHERE r.semester_id=(SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1')
  AND r.is_approved=TRUE AND r.is_best=TRUE
  AND NOT EXISTS (SELECT 1 FROM eligibility_snapshots e WHERE e.student_user_id=r.student_user_id AND e.semester_id=r.semester_id)
GROUP BY r.student_user_id, r.semester_id;

-- Demo data is intentionally marked Admin-approved so each undergraduate
-- can see their own marks, final results, SGPA/CGPA and exam eligibility.
-- Admin and assigned lecturers can inspect/edit the same assessment records.


-- 3) MEDICAL & ATTENDANCE SYNC
-- Source file: sync_medical_attendance.sql

-- Run this ONCE on an existing fams_db if medicals were approved before this fix.
-- It makes attendance_records match medical_records (pending / approved / rejected).
USE fams_db;

UPDATE attendance_records SET has_medical_submitted = FALSE, is_medical_approved = FALSE WHERE record_id > 0;

UPDATE attendance_records ar
JOIN medical_records mr
  ON mr.student_reg_no = ar.student_reg_no AND mr.course_code = ar.course_code
 AND mr.component_type = ar.component_type AND mr.session_number = ar.session_number
SET ar.has_medical_submitted = TRUE,
    ar.is_medical_approved   = (mr.status = 'APPROVED')
WHERE mr.status IN ('PENDING', 'APPROVED') AND ar.is_present = FALSE AND ar.record_id > 0;

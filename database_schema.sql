-- Faculty Academic Management System (FAMS) MySQL Database Schema
-- ICT2132 - Object Oriented Programming Practicum Mini Project
-- Group B16 - Member 04 (C H G Chamikara - TG_2024_2108)

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
    student_reg_no VARCHAR(50),
    batch VARCHAR(50),
    is_repeat BOOLEAN DEFAULT FALSE,
    is_batch_missed BOOLEAN DEFAULT FALSE
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
    FOREIGN KEY (student_reg_no) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE
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
    FOREIGN KEY (student_reg_no) REFERENCES users(user_id) ON DELETE CASCADE
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
    start_time VARCHAR(20) NOT NULL,
    end_time VARCHAR(20) NOT NULL,
    venue VARCHAR(100) NOT NULL,
    lecturer_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    batch VARCHAR(50) NOT NULL,
    academic_period VARCHAR(100) NOT NULL DEFAULT 'Semester 1 - 2026'
);


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
CREATE TABLE IF NOT EXISTS assessments (
    assessment_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL,
    assessment_name VARCHAR(120) NOT NULL,
    assessment_type ENUM('CA','FINAL') NOT NULL DEFAULT 'CA',
    weight_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_assessment_weight CHECK (weight_percentage BETWEEN 0 AND 100),
    CONSTRAINT fk_assessment_course_code FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    UNIQUE KEY uq_course_assessment_name (course_code, assessment_name)
);

CREATE TABLE IF NOT EXISTS marks (
    mark_id INT AUTO_INCREMENT PRIMARY KEY,
    assessment_id INT NOT NULL,
    student_user_id VARCHAR(50) NOT NULL,
    mark DECIMAL(5,2) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_mark_range CHECK (mark BETWEEN 0 AND 100),
    CONSTRAINT fk_mark_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE,
    CONSTRAINT fk_mark_student_user FOREIGN KEY (student_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uq_assessment_student_user (assessment_id, student_user_id)
);

-- Seed Initial Admin User
INSERT IGNORE INTO users (user_id, username, password, full_name, email, phone, department, role)
VALUES ('ADM001', 'admin', 'admin123', 'System Administrator', 'admin@fot.ruh.ac.lk', '0412223344', 'Faculty of Technology', 'ADMIN');

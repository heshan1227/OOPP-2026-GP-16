# Marks & Eligibility Demo Data

The academic upgrade SQL now seeds the Marks & Eligibility subsystem with sample records.

## Setup

1. Start MySQL and open the `fams_db` database.
2. Run `database_schema.sql` first if the base FAMS database has not been created.
3. Run `marks_eligibility_v2_fixed.sql` after the base schema. The final section of that script creates the sample academic semester, assessment definitions, marks, approved results, and eligibility snapshots.
4. Restart FAMS and open **Marks & Eligibility**.

The demo seed is designed to be re-runnable: assessment and mark inserts use `INSERT IGNORE`, result rows use the unique student/course/semester/attempt key, and eligibility snapshots are only added when one does not already exist.

## What is included

- 20 sample undergraduate accounts (UG1–UG20).
- Four sample courses: ICT2132, ICT2113, ICT2122, ENG2102.
- Quiz 1 (10%), Assignment 1 (20%), and End Semester Examination (70%) marks for each student/course.
- Calculated final marks, letter grades, grade points, approved result rows, SGPA/CGPA source data, and eligibility snapshots.
- Lecturer assignments already defined by the base database script are retained.

## Role views

- **Admin:** all courses, marks, CA summaries, end-exam marks, eligibility, grades/GPA, and publication administration.
- **Lecturer:** only assigned courses; can inspect marks and eligibility and follow the lecturer publication workflow.
- **Undergraduate:** only their own Admin-approved CA marks, end-exam marks, final grades, SGPA/CGPA, and exam eligibility. Unapproved results remain hidden.

## Sample logins

- Admin: `admin` / `admin123`
- Lecturer for ICT2132: `dr_samarasinghe` / `lec123`
- Undergraduate sample: `student1` / `student123` (UG1); `student3` / `student123` (low-mark eligibility example)

All marks are demonstration data, not real academic records.

package fams.TG_2024_2113;

import fams.TG_2024_2133.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Transactional persistence for the upgraded marks/eligibility workflow. */
public class AcademicResultsDAO {
    private Connection connection() throws SQLException {
        Connection c=DBConnection.getConnection();
        if(c==null) throw new SQLException("MySQL connection is not available.");
        return c;
    }
    private void requireAssigned(Connection c,String lecturer,String course) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM lecturer_course_assignments WHERE lecturer_user_id=? AND course_code=?")) {
            p.setString(1,lecturer);p.setString(2,course);
            try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Lecturer is not assigned to this course.");}
        }
    }
    public long createAssessment(String actor,String course,long semester,String name,String component,
                                 Integer quizNo,BigDecimal weight) throws SQLException {
        // FIX: added "FINAL" so the UI's FINAL assessment type is accepted
        if(!Set.of("QUIZ","ASSIGNMENT","MID_TERM","NORMAL_ASSIGN","CA","ESA","FINAL").contains(component))
            throw new IllegalArgumentException("Invalid assessment component.");
        // Quiz assessments must be numbered 1, 2 or 3 when component is QUIZ
        if("QUIZ".equals(component) && quizNo==null)
            throw new IllegalArgumentException("Quiz assessments must be numbered 1, 2 or 3.");
        if(quizNo!=null && (quizNo<1||quizNo>3)) throw new IllegalArgumentException("Quiz number must be 1, 2 or 3.");
        // Assignment component validation
        if("ASSIGNMENT".equals(component) && name!=null && !name.toLowerCase(Locale.ROOT).matches("(?i)assignment\\s*[1-2].*"))
            throw new IllegalArgumentException("Assignment assessment name must follow pattern: 'Assignment 1' or 'Assignment 2'.");
        // FIX: removed stray quote — the original ended with ...|"mid term"))
        if("MID_TERM".equals(component) && name!=null && !name.toLowerCase(Locale.ROOT).matches("(?i)(mid.*[Ee]xam|mid term)"))
            throw new IllegalArgumentException("Mid term assessment name must contain 'Mid Term' or 'Mid Exam'.");
        // Set default weights based on assessment type if not specified (weight=0 means use default)
        if(weight==null||weight.signum()==0) {
            switch(component) {
                case "QUIZ": weight = new BigDecimal("10"); break;    // 10% for best 2 quizzes contribution to CA
                case "ASSIGNMENT": weight = new BigDecimal("5"); break;  // 5% for best 1 assignment contribution to CA
                case "MID_TERM": weight = new BigDecimal("10"); break; // 10% for mid term contribution to CA
                case "NORMAL_ASSIGN": weight = new BigDecimal("0"); break; // Not counted in CA calculations
                case "CA": weight = new BigDecimal("30"); break; // CA total weight
                case "ESA": weight = new BigDecimal("70"); break; // ESA (End Semester Assessment) weight
                case "FINAL": weight = new BigDecimal("70"); break; // FIX: added FINAL
            }
        }
        // Quiz-count validation is performed inside the transaction below.

        if(weight==null||weight.signum()<0||weight.compareTo(new BigDecimal("100"))>0) throw new IllegalArgumentException("Invalid assessment weight.");
        try(Connection c=connection()) {
            c.setAutoCommit(false);
            try {
                requireAnyRole(c,actor,"ADMIN","LECTURER");
                if("LECTURER".equals(role(c,actor))) requireAssigned(c,actor,course);
                // FIX: was `"CA".equals(component)&&quizNo!=null`; the query was for component='CA' which contradicted the check
                if("QUIZ".equals(component)) {
                    try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM academic_assessments WHERE course_code=? AND semester_id=? AND component='QUIZ' AND quiz_no IS NOT NULL")) {
                        p.setString(1,course);p.setLong(2,semester);
                        try(ResultSet r=p.executeQuery()){r.next();if(r.getInt(1)>=3)throw new SQLException("Maximum of 3 CA quizzes is allowed for this course and semester.");}
                    }
                    if(quizNo<1||quizNo>3) throw new IllegalArgumentException("Quiz number must be 1, 2 or 3.");
                }
                // Validate quiz/assignment/mid term specific rules
                switch(component) {
                    case "QUIZ":
                        if(name==null || !name.toLowerCase(Locale.ROOT).matches("(?i)quiz\\s*[1-3].*"))
                            throw new IllegalArgumentException("Quiz assessment name must follow pattern: 'Quiz 1', 'Quiz 2', or 'Quiz 3'.");
                        break;
                    case "ASSIGNMENT":
                        if(name==null || !name.toLowerCase(Locale.ROOT).matches("(?i)assignment\\s*[1-2].*"))
                            throw new IllegalArgumentException("Assignment assessment name must follow pattern: 'Assignment 1' or 'Assignment 2'.");
                        break;
                    case "MID_TERM":
                        // FIX: removed stray quote — was ...|"mid term"))
                        if(name==null || !name.toLowerCase(Locale.ROOT).matches("(?i)(mid.*[Ee]xam|mid term)"))
                            throw new IllegalArgumentException("Mid term assessment name must contain 'Mid Term' or 'Mid Exam'.");
                        break;
                    case "NORMAL_ASSIGN":
                        // No limit, no specific pattern required
                        break;
                    case "FINAL":
                        // FIX: removed stray quote — was ...|"final exam"))
                        if(name==null || !name.toLowerCase(Locale.ROOT).matches("(?i)(final.*[Ee]xam|final exam)"))
                            throw new IllegalArgumentException("Final assessment name must contain 'Final Exam'.");
                        break;
                }
                // Set default weights based on assessment type if not specified
                if(weight.signum()==0) {
                    switch(component) {
                        case "QUIZ": weight = new BigDecimal("10"); break;    // 10% for best 2 quizzes
                        case "ASSIGNMENT": weight = new BigDecimal("5"); break;  // 5% for best 1 assignment
                        case "MID_TERM": weight = new BigDecimal("10"); break; // 10% for mid term
                        case "NORMAL_ASSIGN": weight = new BigDecimal("0"); break; // Not counted in CA
                        case "CA": weight = new BigDecimal("30"); break; // CA total weight
                        case "ESA": weight = new BigDecimal("70"); break; // FIX: added ESA (was missing)
                        case "FINAL": weight = new BigDecimal("70"); break;
                    }
                }

                try(PreparedStatement p=c.prepareStatement("INSERT INTO academic_assessments(course_code,semester_id,assessment_name,component,quiz_no,weight_percentage,created_by) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1,course);p.setLong(2,semester);p.setString(3,name);p.setString(4,component);
                    if(quizNo==null)p.setNull(5,Types.TINYINT);else p.setInt(5,quizNo);
                    p.setBigDecimal(6,weight);p.setString(7,actor);p.executeUpdate();
                    try(ResultSet r=p.getGeneratedKeys()){r.next();long id=r.getLong(1);audit(c,actor,"ASSESSMENT_CREATE","academic_assessments",String.valueOf(id),null,name,null);c.commit();return id;}
                }
            } catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;if(e instanceof RuntimeException re)throw re;throw new SQLException(e);}
            finally{c.setAutoCommit(true);}
        }
    }
    /** Saves a whole class grid atomically. Map key = user_id, value = mark. */
    public void saveBulkMarks(String actor,long assessmentId,int attemptNo,Map<String,BigDecimal> marks) throws SQLException {
        if(marks==null||marks.isEmpty())throw new IllegalArgumentException("No marks supplied.");
        try(Connection c=connection()) {
            c.setAutoCommit(false);
            try {
                requireAnyRole(c,actor,"ADMIN","LECTURER");
                String course;String component;
                try(PreparedStatement p=c.prepareStatement("SELECT course_code,component FROM academic_assessments WHERE assessment_id=? FOR UPDATE")) {
                    p.setLong(1,assessmentId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Assessment not found.");course=r.getString(1);component=r.getString(2);}
                }
                if("LECTURER".equals(role(c,actor)))requireAssigned(c,actor,course);
                if(attemptNo<1)throw new IllegalArgumentException("Attempt number must be positive.");
                for(var entry:marks.entrySet()) {
                    AcademicResultEngine.validateMark(entry.getValue());
                    try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM course_enrollments WHERE student_user_id=? AND course_code=? AND active=TRUE")) {
                        p.setString(1,entry.getKey());p.setString(2,course);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Student "+entry.getKey()+" is not enrolled in "+course);}
                    }
                    if("ESA".equals(component)) {
                        try(PreparedStatement p=c.prepareStatement("SELECT status,override_reason FROM eligibility_snapshots WHERE student_user_id=? AND semester_id=(SELECT semester_id FROM academic_assessments WHERE assessment_id=?) ORDER BY checked_at DESC,snapshot_id DESC LIMIT 1")) {
                            p.setString(1,entry.getKey());p.setLong(2,assessmentId);try(ResultSet r=p.executeQuery()){
                                if(r.next()&&"NOT_ELIGIBLE".equals(r.getString("status"))&&r.getString("override_reason")==null)
                                    throw new SQLException("End Exam entry blocked: student "+entry.getKey()+" is NOT ELIGIBLE for this semester. Admin override with reason is required.");
                            }
                        }
                    }
                    BigDecimal old=null;
                    try(PreparedStatement p=c.prepareStatement("SELECT mark FROM academic_marks WHERE assessment_id=? AND student_user_id=? AND attempt_no=? FOR UPDATE")) {
                        p.setLong(1,assessmentId);p.setString(2,entry.getKey());p.setInt(3,attemptNo);try(ResultSet r=p.executeQuery()){if(r.next())old=r.getBigDecimal(1);}
                    }
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO academic_marks(assessment_id,student_user_id,attempt_no,mark,entered_by) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE mark=VALUES(mark),entered_by=VALUES(entered_by)")) {
                        p.setLong(1,assessmentId);p.setString(2,entry.getKey());p.setInt(3,attemptNo);p.setBigDecimal(4,entry.getValue().setScale(2,java.math.RoundingMode.HALF_UP));p.setString(5,actor);p.executeUpdate();
                    }
                    audit(c,actor,"MARK_SAVE","academic_marks",assessmentId+":"+entry.getKey()+":"+attemptNo,old==null?null:old.toPlainString(),entry.getValue().toPlainString(),null);
                }
                c.commit();
            }catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;if(e instanceof RuntimeException re)throw re;throw new SQLException(e);}
            finally{c.setAutoCommit(true);}
        }
    }
    public long submitPublishRequest(String lecturer,String course,long semester) throws SQLException {
        try(Connection c=connection()) {
            c.setAutoCommit(false);
            try {
                requireAnyRole(c,lecturer,"LECTURER");
                requireAssigned(c,lecturer,course);
                try(PreparedStatement p=c.prepareStatement("INSERT INTO result_publish_requests(course_code,semester_id,lecturer_user_id,status,submitted_at) VALUES(?,?,?,'SUBMITTED',NOW())",Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1,course);p.setLong(2,semester);p.setString(3,lecturer);p.executeUpdate();
                    try(ResultSet r=p.getGeneratedKeys()){r.next();long id=r.getLong(1);audit(c,lecturer,"PUBLISH_SUBMIT","result_publish_requests",String.valueOf(id),null,"SUBMITTED",null);
                        notifyUser(c,"ADM001","PUBLISH_REQUEST","Lecturer "+lecturer+" submitted results for "+course+".");
                        queueEmails(c,"ADM001","Results publish request","Lecturer "+lecturer+" submitted results for "+course+".");
                        c.commit();return id;}
                }
            }catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;throw new SQLException(e);}
            finally{c.setAutoCommit(true);}
        }
    }
    public Long findSubmittedRequestId(String course) throws SQLException {
        try(Connection c=connection();PreparedStatement p=c.prepareStatement(
                "SELECT request_id FROM result_publish_requests WHERE course_code=? AND status='SUBMITTED' ORDER BY submitted_at LIMIT 1")) {
            p.setString(1,course);try(ResultSet r=p.executeQuery()){return r.next()?r.getLong(1):null;}
        }
    }
    public void reviewPublishRequest(String admin,long requestId,boolean approve,String reason) throws SQLException {
        if(reason==null||reason.isBlank())throw new IllegalArgumentException("A review reason is required.");
        try(Connection c=connection()) {
            c.setAutoCommit(false);
            try {
                requireAnyRole(c,admin,"ADMIN");
                String old;
                try(PreparedStatement p=c.prepareStatement("SELECT status FROM result_publish_requests WHERE request_id=? FOR UPDATE")) {
                    p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Publish request not found.");old=r.getString(1);}
                }
                if(!"SUBMITTED".equals(old))throw new SQLException("Only SUBMITTED requests can be reviewed.");
                String next=approve?"APPROVED":"REJECTED";
                try(PreparedStatement p=c.prepareStatement("UPDATE result_publish_requests SET status=?,reviewed_by=?,reviewed_at=NOW(),review_reason=? WHERE request_id=?")) {
                    p.setString(1,next);p.setString(2,admin);p.setString(3,reason);p.setLong(4,requestId);p.executeUpdate();
                }
                audit(c,admin,"PUBLISH_"+next,"result_publish_requests",String.valueOf(requestId),old,next,reason);
                String lecturerId; String courseCode; long semesterKey;
                try(PreparedStatement q=c.prepareStatement("SELECT lecturer_user_id,course_code,semester_id FROM result_publish_requests WHERE request_id=?")) {
                    q.setLong(1,requestId);try(ResultSet rr=q.executeQuery()){rr.next();lecturerId=rr.getString(1);courseCode=rr.getString(2);semesterKey=rr.getLong(3);}
                }
                String decisionMessage="Your publish request for "+courseCode+" was "+next+". "+reason;
                notifyUser(c,lecturerId,"PUBLISH_"+next,decisionMessage);
                queueEmails(c,lecturerId,"Results publish request "+next,decisionMessage);
                if(approve) {
                    recalculateRequest(c,admin,requestId);
                    try(PreparedStatement p=c.prepareStatement("UPDATE academic_results SET is_approved=TRUE WHERE approved_request_id=?")) {
                        p.setLong(1,requestId);p.executeUpdate();
                    }
                    try(PreparedStatement q=c.prepareStatement("SELECT student_user_id FROM course_enrollments WHERE course_code=? AND active=TRUE")) {
                        q.setString(1,courseCode);try(ResultSet rr=q.executeQuery()){while(rr.next()){
                            String student=rr.getString(1);String msg="Your results for semester "+semesterKey+" are now available.";
                            appendEligibilitySnapshot(c,admin,student,semesterKey);
                            notifyUser(c,student,"RESULTS_PUBLISHED",msg);queueEmails(c,student,"Your results are available",msg);
                        }}
                    }
                }
                c.commit();
            }catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;throw new SQLException(e);}
            finally{c.setAutoCommit(true);}
        }
    }
    /** Calculate SGPA and uniform-semester CGPA from approved best-attempt GPA modules and append an immutable snapshot. */
    public void calculateAndSnapshotEligibility(String admin,String student,long semesterId) throws SQLException {
        try(Connection c=connection()) {
            requireAnyRole(c,admin,"ADMIN");
            appendEligibilitySnapshot(c,admin,student,semesterId);
        }
    }
    private void appendEligibilitySnapshot(Connection c,String admin,String student,long semesterId) throws SQLException {
        BigDecimal sgpa=BigDecimal.ZERO.setScale(2),cgpa=BigDecimal.ZERO.setScale(2);
        try(PreparedStatement p=c.prepareStatement(
                "SELECT SUM(grade_point*credits)/NULLIF(SUM(credits),0) FROM academic_results "+
                        "WHERE student_user_id=? AND semester_id=? AND is_approved=TRUE AND is_best=TRUE AND gpa_module=TRUE AND grade_code NOT IN ('N','W')")) {
            p.setString(1,student);p.setLong(2,semesterId);try(ResultSet r=p.executeQuery()){if(r.next()&&r.getBigDecimal(1)!=null)sgpa=r.getBigDecimal(1).setScale(2,java.math.RoundingMode.HALF_UP);}
        }
        List<BigDecimal> semesterSgpas=new ArrayList<>();
        try(PreparedStatement p=c.prepareStatement(
                "SELECT SUM(ar.grade_point*ar.credits)/NULLIF(SUM(ar.credits),0) FROM academic_results ar "+
                        "JOIN academic_semesters s ON s.semester_id=ar.semester_id WHERE ar.student_user_id=? AND ar.semester_id<=? "+
                        "AND ar.is_approved=TRUE AND ar.is_best=TRUE AND ar.gpa_module=TRUE AND ar.grade_code NOT IN ('N','W') "+
                        "GROUP BY ar.semester_id ORDER BY s.semester_no")) {
            p.setString(1,student);p.setLong(2,semesterId);try(ResultSet r=p.executeQuery()){while(r.next())if(r.getBigDecimal(1)!=null)semesterSgpas.add(r.getBigDecimal(1).setScale(2,java.math.RoundingMode.HALF_UP));}
        }
        if(!semesterSgpas.isEmpty())cgpa=semesterSgpas.stream().reduce(BigDecimal.ZERO,BigDecimal::add)
                .divide(BigDecimal.valueOf(semesterSgpas.size()),2,java.math.RoundingMode.HALF_UP);
        String threshold="2.00";
        try(PreparedStatement p=c.prepareStatement("SELECT config_value FROM academic_system_config WHERE config_key='eligibility.threshold'");ResultSet r=p.executeQuery()){if(r.next())threshold=r.getString(1);}
        String status=AcademicResultEngine.eligibility(cgpa,new BigDecimal(threshold)).name();
        try(PreparedStatement p=c.prepareStatement("INSERT INTO eligibility_snapshots(student_user_id,semester_id,gpa_used,sgpa,cgpa,status,checked_by) VALUES(?,?,?,?,?,?,?)")) {
            p.setString(1,student);p.setLong(2,semesterId);p.setBigDecimal(3,cgpa);p.setBigDecimal(4,sgpa);p.setBigDecimal(5,cgpa);p.setString(6,status);p.setString(7,admin);p.executeUpdate();
        }
        audit(c,admin,"ELIGIBILITY_CHECK","eligibility_snapshots",student+":"+semesterId,null,status,"Automatic check after Admin result approval");
    }
    /** Fifth-semester registration gate: first three semester SGPAs are averaged equally. */
    public boolean checkFifthSemesterProgression(String admin,String student,long targetSemesterId) throws SQLException {
        int targetNo;
        try(Connection c=connection()) {
            requireAnyRole(c,admin,"ADMIN");
            try(PreparedStatement p=c.prepareStatement("SELECT semester_no FROM academic_semesters WHERE semester_id=?")) {
                p.setLong(1,targetSemesterId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Target semester not found.");targetNo=r.getInt(1);}
            }
        }
        if(targetNo!=5)throw new IllegalArgumentException("The progression gate applies only to semester 5.");
        List<BigDecimal> sgpas=new ArrayList<>();
        try(Connection c=connection();PreparedStatement p=c.prepareStatement(
                "SELECT SUM(ar.grade_point*ar.credits)/NULLIF(SUM(ar.credits),0) sgpa FROM academic_results ar "+
                        "JOIN academic_semesters s ON s.semester_id=ar.semester_id WHERE ar.student_user_id=? AND s.semester_no<=3 "+
                        "AND ar.is_approved=TRUE AND ar.is_best=TRUE AND ar.gpa_module=TRUE AND ar.grade_code NOT IN ('N','W') "+
                        "GROUP BY ar.semester_id ORDER BY s.semester_no")) {
            p.setString(1,student);try(ResultSet r=p.executeQuery()){while(r.next())sgpas.add(r.getBigDecimal(1).setScale(2,java.math.RoundingMode.HALF_UP));}
        }
        BigDecimal firstThree=sgpas.isEmpty()?BigDecimal.ZERO:
                sgpas.stream().reduce(BigDecimal.ZERO,BigDecimal::add).divide(BigDecimal.valueOf(sgpas.size()),2,java.math.RoundingMode.HALF_UP);
        BigDecimal threshold=new BigDecimal("2.00");
        try(Connection c=connection();PreparedStatement p=c.prepareStatement("SELECT config_value FROM academic_system_config WHERE config_key='progression.semester5_min_cgpa'");ResultSet r=p.executeQuery()){
            if(r.next())threshold=new BigDecimal(r.getString(1));
        }
        boolean canRegister=AcademicResultEngine.canRegisterSemester5(firstThree,threshold);
        try(Connection c=connection();PreparedStatement p=c.prepareStatement(
                "INSERT INTO progression_snapshots(student_user_id,target_semester_id,first_three_cgpa,can_register,checked_by) VALUES(?,?,?,?,?)")) {
            p.setString(1,student);p.setLong(2,targetSemesterId);p.setBigDecimal(3,firstThree);p.setBoolean(4,canRegister);p.setString(5,admin);p.executeUpdate();
        }
        return canRegister;
    }
    /** Overrides append a new immutable snapshot; the prior exam-time snapshot is never updated. */
    public void overrideEligibility(String admin,String student,long semesterId,String status,String reason) throws SQLException {
        if(!Set.of("ELIGIBLE","NOT_ELIGIBLE").contains(status)||reason==null||reason.isBlank())
            throw new IllegalArgumentException("A valid status and logged reason are required.");
        try(Connection c=connection()) {
            c.setAutoCommit(false);
            try {
                requireAnyRole(c,admin,"ADMIN");
                BigDecimal sgpa=BigDecimal.ZERO,cgpa=BigDecimal.ZERO;
                try(PreparedStatement p=c.prepareStatement("SELECT sgpa,cgpa FROM eligibility_snapshots WHERE student_user_id=? AND semester_id=? ORDER BY checked_at DESC LIMIT 1")) {
                    p.setString(1,student);p.setLong(2,semesterId);try(ResultSet r=p.executeQuery()){if(r.next()){sgpa=r.getBigDecimal(1);cgpa=r.getBigDecimal(2);}}
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO eligibility_snapshots(student_user_id,semester_id,gpa_used,sgpa,cgpa,status,checked_by,override_reason,override_by) VALUES(?,?,?,?,?,?,?,?,?)")) {
                    p.setString(1,student);p.setLong(2,semesterId);p.setBigDecimal(3,cgpa);p.setBigDecimal(4,sgpa);p.setBigDecimal(5,cgpa);p.setString(6,status);p.setString(7,admin);p.setString(8,reason);p.setString(9,admin);p.executeUpdate();
                }
                audit(c,admin,"ELIGIBILITY_OVERRIDE","eligibility_snapshots",student+":"+semesterId,null,status,reason);
                c.commit();
            }catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;throw new SQLException(e);}
            finally{c.setAutoCommit(true);}
        }
    }
    public void createEligibilitySnapshot(String admin,String student,long semester,BigDecimal sgpa,BigDecimal cgpa) throws SQLException {
        try(Connection c=connection()) {
            requireAnyRole(c,admin,"ADMIN");
            String threshold="2.00";
            try(PreparedStatement p=c.prepareStatement("SELECT config_value FROM academic_system_config WHERE config_key='eligibility.threshold'");ResultSet r=p.executeQuery()){if(r.next())threshold=r.getString(1);}
            BigDecimal gpaUsed=cgpa;
            String status=AcademicResultEngine.eligibility(gpaUsed,new BigDecimal(threshold)).name();
            try(PreparedStatement p=c.prepareStatement("INSERT INTO eligibility_snapshots(student_user_id,semester_id,gpa_used,sgpa,cgpa,status,checked_by) VALUES(?,?,?,?,?,?,?)")) {
                p.setString(1,student);p.setLong(2,semester);p.setBigDecimal(3,gpaUsed);p.setBigDecimal(4,sgpa);p.setBigDecimal(5,cgpa);p.setString(6,status);p.setString(7,admin);p.executeUpdate();
            }
        }
    }
    private void recalculateRequest(Connection c,String admin,long requestId) throws SQLException {
        String course; long semester;
        try(PreparedStatement p=c.prepareStatement("SELECT course_code,semester_id FROM result_publish_requests WHERE request_id=?")) {
            p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){r.next();course=r.getString(1);semester=r.getLong(2);}
        }
        BigDecimal caWeight=new BigDecimal("30"),esaWeight=new BigDecimal("70");
        BigDecimal caPass=new BigDecimal("40"),esaPass=new BigDecimal("35");
        try(PreparedStatement p=c.prepareStatement("SELECT ca_weight,esa_weight,ca_pass,esa_pass FROM module_result_config WHERE course_code=?")) {
            p.setString(1,course);try(ResultSet r=p.executeQuery()){if(r.next()){caWeight=r.getBigDecimal(1);esaWeight=r.getBigDecimal(2);caPass=r.getBigDecimal(3);esaPass=r.getBigDecimal(4);}}
        }
        int credits;
        try(PreparedStatement p=c.prepareStatement("SELECT COALESCE(theory_credits,0)+COALESCE(practical_credits,0) FROM courses WHERE course_code=?")) {
            p.setString(1,course);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Course not found.");credits=r.getInt(1);}
        }
        if(credits<=0)throw new SQLException("Course credits must be greater than zero.");
        List<GPAManagement.GradeBand> gradeBands=loadGradeBands(c,course);
        List<String> students=new ArrayList<>();
        try(PreparedStatement p=c.prepareStatement("SELECT student_user_id FROM course_enrollments WHERE course_code=? AND active=TRUE")) {
            p.setString(1,course);try(ResultSet r=p.executeQuery()){while(r.next())students.add(r.getString(1));}
        }
        for(String student:students) {
            BigDecimal ca=caWeight.signum()==0?new BigDecimal("100.00"):componentAverage(c,course,semester,student,"CA");
            BigDecimal esa=esaWeight.signum()==0?new BigDecimal("100.00"):componentAverage(c,course,semester,student,"ESA");
            AcademicResultEngine.ModuleResult result=AcademicResultEngine.calculate(
                    new AcademicResultEngine.ModuleMark(ca,esa,caWeight,esaWeight,credits,true,"",caPass,esaPass));
            String caGrade=GPAManagement.calculate(ca.doubleValue(),gradeBands).letterGrade();
            String esaGrade=GPAManagement.calculate(esa.doubleValue(),gradeBands).letterGrade();
            String finalGrade=result.grade(); BigDecimal finalGp=result.gp();
            if(result.status()==AcademicResultEngine.AttemptStatus.GRADED) {
                var mapped=GPAManagement.calculate(result.finalMark().doubleValue(),gradeBands);
                finalGrade=mapped.letterGrade(); finalGp=BigDecimal.valueOf(mapped.gradePoint()).setScale(2,java.math.RoundingMode.HALF_UP);
            }
            try(PreparedStatement p=c.prepareStatement(
                    "INSERT INTO academic_results(student_user_id,course_code,semester_id,attempt_no,ca_component_grade,esa_component_grade,final_mark,grade_code,grade_point,credits,gpa_module,is_best,is_approved,approved_request_id) "+
                            "VALUES(?,?,?,1,?,?,?,?,?,?,TRUE,TRUE,FALSE,?) ON DUPLICATE KEY UPDATE ca_component_grade=VALUES(ca_component_grade),esa_component_grade=VALUES(esa_component_grade),final_mark=VALUES(final_mark),grade_code=VALUES(grade_code),grade_point=VALUES(grade_point),credits=VALUES(credits),gpa_module=TRUE,is_best=TRUE,is_approved=FALSE,approved_request_id=VALUES(approved_request_id)")) {
                p.setString(1,student);p.setString(2,course);p.setLong(3,semester);p.setString(4,caGrade);p.setString(5,esaGrade);
                p.setBigDecimal(6,result.finalMark());p.setString(7,finalGrade);p.setBigDecimal(8,finalGp);p.setInt(9,credits);p.setLong(10,requestId);p.executeUpdate();
            }
        }
    }
    private List<GPAManagement.GradeBand> loadGradeBands(Connection c,String course) throws SQLException {
        Map<String,GPAManagement.GradeBand> bands=new LinkedHashMap<>();
        try(PreparedStatement p=c.prepareStatement("SELECT grade_code,minimum_mark,grade_point FROM grade_boundary_config WHERE course_code IS NULL OR course_code=? ORDER BY course_code IS NULL DESC")) {
            p.setString(1,course);try(ResultSet r=p.executeQuery()){while(r.next())bands.put(r.getString(1),new GPAManagement.GradeBand(r.getString(1),r.getBigDecimal(2),r.getBigDecimal(3)));}
        }
        return bands.isEmpty()?GPAManagement.defaultBands():new ArrayList<>(bands.values());
    }
    private BigDecimal componentAverage(Connection c,String course,long semester,String student,String component) throws SQLException {
        String sql="SELECT COUNT(*) total,COUNT(m.mark_id) entered,COALESCE(SUM(m.mark*a.weight_percentage),0) weighted,COALESCE(SUM(a.weight_percentage),0) weights "+
                "FROM academic_assessments a LEFT JOIN academic_marks m ON m.assessment_id=a.assessment_id AND m.student_user_id=? AND m.attempt_no=1 "+
                "WHERE a.course_code=? AND a.semester_id=? AND a.component=?";
        try(PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,student);p.setString(2,course);p.setLong(3,semester);p.setString(4,component);
            try(ResultSet r=p.executeQuery()){r.next();int total=r.getInt("total"),entered=r.getInt("entered");BigDecimal weights=r.getBigDecimal("weights");
                if(total==0||entered<total||weights.signum()==0)throw new SQLException("Cannot approve: missing "+component+" marks for student "+student+".");
                return r.getBigDecimal("weighted").divide(weights,8,java.math.RoundingMode.HALF_UP).setScale(2,java.math.RoundingMode.HALF_UP);
            }
        }
    }
    private void notifyUser(Connection c,String user,String type,String message) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO user_notifications(user_id,notification_type,message) VALUES(?,?,?)")) {
            p.setString(1,user);p.setString(2,type);p.setString(3,message);p.executeUpdate();
        }
    }
    private void queueEmails(Connection c,String user,String subject,String body) throws SQLException {
        try(PreparedStatement q=c.prepareStatement("SELECT email FROM users WHERE user_id=?")) {
            q.setString(1,user);try(ResultSet r=q.executeQuery()){if(r.next()&&r.getString(1)!=null&&!r.getString(1).isBlank()){
                try(PreparedStatement p=c.prepareStatement("INSERT INTO email_queue(recipient_email,subject,body) VALUES(?,?,?)")){
                    p.setString(1,r.getString(1));p.setString(2,subject);p.setString(3,body);p.executeUpdate();
                }
            }}
        }
    }
    private void requireAnyRole(Connection c,String user,String... allowed) throws SQLException {
        String actual=role(c,user);
        for(String r:allowed)if(r.equals(actual))return;
        throw new SQLException("Access denied: "+actual+" cannot access the academic results module.");
    }
    private String role(Connection c,String user) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT role FROM users WHERE user_id=?")){p.setString(1,user);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Unknown user.");return r.getString(1);}}
    }
    private void audit(Connection c,String actor,String action,String entity,String id,String oldV,String newV,String reason) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO academic_audit_log(actor_user_id,action_type,entity_type,entity_id,old_value,new_value,reason) VALUES(?,?,?,?,?,?,?)")) {
            p.setString(1,actor);p.setString(2,action);p.setString(3,entity);p.setString(4,id);p.setString(5,oldV);p.setString(6,newV);p.setString(7,reason);p.executeUpdate();
        }
    }
}
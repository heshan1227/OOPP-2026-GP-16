package com.fams.gui;

import com.fams.database.DBConnection;
import com.fams.model.UndergraduateUser;
import com.fams.model.User;
import com.fams.service.AuthService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

/** Read-only personal student portal for attendance, medicals, courses, grades and timetable. */
public class StudentPortalPanel extends JPanel {
    private final User user = AuthService.getInstance().getCurrentUser();
    private final JTabbedPane tabs = new JTabbedPane();
    public StudentPortalPanel(){setLayout(new BorderLayout(10,10));setBackground(Theme.LIGHT_BG);setBorder(new EmptyBorder(18,18,18,18));add(Theme.createHeaderLabel("My Undergraduate Portal"),BorderLayout.NORTH);add(tabs,BorderLayout.CENTER);
        tabs.addTab("Attendance",table("SELECT course_code AS Course, component_type AS Component, session_number AS Session, session_date AS Date, is_present AS Present, has_medical_submitted AS Medical, is_medical_approved AS Approved FROM attendance_records WHERE student_reg_no=? ORDER BY session_date DESC",studentKey()));
        tabs.addTab("Medicals",table("SELECT course_code AS Course, component_type AS Component, session_number AS Session, absent_date AS Date, reference_no AS Reference, status AS Status, reason AS Reason FROM medical_records WHERE student_reg_no=? ORDER BY absent_date DESC",studentKey()));
        tabs.addTab("Courses",table("SELECT c.course_code AS Code,c.course_name AS Course,c.theory_credits AS TheoryCredits,c.practical_credits AS PracticalCredits FROM course_enrollments e JOIN courses c ON c.course_code=e.course_code WHERE e.student_user_id=? AND e.active=TRUE",user.getUserId()));
        tabs.addTab("Marks & Grades",table("SELECT c.course_code AS Course,a.assessment_name AS Assessment,a.assessment_type AS Type,m.mark AS Mark FROM marks m JOIN assessments a ON a.assessment_id=m.assessment_id JOIN courses c ON c.course_code=a.course_code WHERE m.student_user_id=? ORDER BY c.course_code,a.assessment_type",user.getUserId()));
        tabs.addTab("Timetable",table("SELECT course_code AS Course,component_type AS Component,day_of_week AS Day,start_time AS Starts,end_time AS Ends,venue AS Venue,batch AS Batch FROM timetables WHERE batch=(SELECT batch FROM users WHERE user_id=?) OR department=(SELECT department FROM users WHERE user_id=?) ORDER BY FIELD(day_of_week,'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'),start_time",user.getUserId(),user.getUserId()));
    }
    private String studentKey(){return user instanceof UndergraduateUser?((UndergraduateUser)user).getStudentRegNo():user.getUserId();}
    private JScrollPane table(String sql,String... params){String[] headers={"Loading"};DefaultTableModelHolder holder=new DefaultTableModelHolder();JTable t=new JTable(holder.model);t.setFillsViewportHeight(true);try(Connection con=DBConnection.getConnection()){
        if(con==null){holder.model.addColumn("Information");holder.model.addRow(new Object[]{"Connect to MySQL to view your personal records."});}
        else try(PreparedStatement ps=con.prepareStatement(sql)){for(int i=0;i<params.length;i++)ps.setString(i+1,params[i]);try(ResultSet rs=ps.executeQuery()){ResultSetMetaData md=rs.getMetaData();holder.model.setColumnCount(0);for(int i=1;i<=md.getColumnCount();i++)holder.model.addColumn(md.getColumnLabel(i));while(rs.next()){Object[] row=new Object[md.getColumnCount()];for(int i=1;i<=row.length;i++)row[i-1]=rs.getObject(i);holder.model.addRow(row);}}}
        }catch(Exception ex){holder.model.setColumnCount(0);holder.model.addColumn("Could not load records");holder.model.addRow(new Object[]{ex.getMessage()});}return new JScrollPane(t);}
    private static class DefaultTableModelHolder { final javax.swing.table.DefaultTableModel model=new javax.swing.table.DefaultTableModel(){public boolean isCellEditable(int r,int c){return false;}}; }
}

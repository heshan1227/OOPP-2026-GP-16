package com.fams.gui;

import com.fams.database.DBConnection;
import com.fams.database.MarksDAO;
import com.fams.service.MarksService;
import com.fams.service.AuthService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

/** Mark entry and CA/attendance eligibility view. Marks are validated on the client and database. */
public class MarksManagementPanel extends JPanel {
    private final DefaultTableModel model=new DefaultTableModel(
        new Object[]{"Mark ID","Student User ID","Course","Assessment","Type","Weight %","Mark /100","CA Weighted"},0){
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable table=new JTable(model);
    private final String role;
    public MarksManagementPanel(){
        super(new BorderLayout(8,8));
        var user=AuthService.getInstance().getCurrentUser();
        role=user==null?"":user.getRole().name();
        add(new JLabel("Marks, Assessment & Eligibility"),BorderLayout.NORTH);
        add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addAssessment=new JButton("Add Assessment"), enter=new JButton("Enter / Update Mark"), eligibility=new JButton("Check Eligibility"), refresh=new JButton("Refresh");
        boolean lecturer=role.equals("LECTURER")||role.equals("ADMIN");
        addAssessment.setEnabled(lecturer);enter.setEnabled(lecturer);
        actions.add(addAssessment);actions.add(enter);actions.add(eligibility);actions.add(refresh);add(actions,BorderLayout.SOUTH);
        eligibility.addActionListener(e->checkEligibility());
        addAssessment.addActionListener(e->createAssessment());
        enter.addActionListener(e->enterMark());
        refresh.addActionListener(e->load());
        load();
    }
    private void createAssessment(){
        JTextField course=new JTextField(),name=new JTextField();
        JComboBox<String> type=new JComboBox<>(new String[]{"CA","FINAL"});
        JTextField weight=new JTextField("20");
        JPanel p=new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Course code"));p.add(course);p.add(new JLabel("Assessment name"));p.add(name);
        p.add(new JLabel("Type"));p.add(type);p.add(new JLabel("Weight % (CA)"));p.add(weight);
        if(JOptionPane.showConfirmDialog(this,p,"Add Assessment",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{
            String cc=course.getText().trim(), n=name.getText().trim(), t=(String)type.getSelectedItem();
            BigDecimal w=new BigDecimal(weight.getText().trim());
            if(cc.isEmpty()||n.isEmpty()||w.compareTo(BigDecimal.ZERO)<0||w.compareTo(new BigDecimal("100"))>0)throw new IllegalArgumentException("Enter valid course, assessment and weight (0–100).");
            try(Connection c=DBConnection.getConnection()){
                if(c==null)throw new SQLException("Assessment management requires MySQL. Start MySQL and import database_schema.sql.");
                try(PreparedStatement s=c.prepareStatement("INSERT INTO assessments(course_code,assessment_name,assessment_type,weight_percentage) VALUES(?,?,?,?)")){
                    s.setString(1,cc);s.setString(2,n);s.setString(3,t);s.setBigDecimal(4,w);s.executeUpdate();
                }
            }
            load();
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void enterMark(){
        JTextField assessment=new JTextField(), student=new JTextField(), mark=new JTextField();
        JPanel p=new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Assessment ID (from table)"));p.add(assessment);
        p.add(new JLabel("Student User ID (e.g. UG1)"));p.add(student);
        p.add(new JLabel("Mark (0–100)"));p.add(mark);
        if(JOptionPane.showConfirmDialog(this,p,"Enter / Update Mark",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{
            int aid=Integer.parseInt(assessment.getText().trim());
            String uid=student.getText().trim();
            BigDecimal value=new BigDecimal(mark.getText().trim());
            if(aid<=0||uid.isEmpty()||value.compareTo(BigDecimal.ZERO)<0||value.compareTo(new BigDecimal("100"))>0)
                throw new IllegalArgumentException("Assessment ID and student ID are required; mark must be 0–100.");
            try(Connection c=DBConnection.getConnection()){
                if(c==null)throw new SQLException("Mark entry requires MySQL. Start MySQL and import database_schema.sql.");
                try(PreparedStatement s=c.prepareStatement("INSERT INTO marks(assessment_id,student_user_id,mark) VALUES(?,?,?) ON DUPLICATE KEY UPDATE mark=VALUES(mark)")){
                    s.setInt(1,aid);s.setString(2,uid);s.setBigDecimal(3,value);s.executeUpdate();
                }
            }
            load();
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void checkEligibility(){
        String studentId=JOptionPane.showInputDialog(this,"Student user ID / registration number (leave blank for whole batch):","Database Eligibility",JOptionPane.QUESTION_MESSAGE);
        if(studentId==null)return;
        try {
            var results=new MarksService().checkEligibility(studentId);
            if(results.isEmpty()){JOptionPane.showMessageDialog(this,"No matching students/courses found. Ensure MySQL is running and attendance, CA marks and courses are seeded.","Eligibility",JOptionPane.INFORMATION_MESSAGE);return;}
            StringBuilder out=new StringBuilder("Eligibility rules: CA >= 40% AND attendance >= 80%\n\n");
            for(MarksDAO.Eligibility r:results) out.append(r.studentId()).append(" | ").append(r.studentName()).append(" | ").append(r.courseCode())
                .append(" | CA ").append(String.format("%.2f",r.caPercent())).append("% | Attendance ").append(String.format("%.2f",r.attendancePercent()))
                .append("% | ").append(r.eligible()?"ELIGIBLE":"NOT ELIGIBLE").append('\n');
            JTextArea area=new JTextArea(out.toString(),20,80);area.setEditable(false);area.setCaretPosition(0);
            JOptionPane.showMessageDialog(this,new JScrollPane(area),"Database Eligibility Results",JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception ex){error("Could not calculate eligibility from database: "+ex.getMessage());}
    }
    private void load(){
        model.setRowCount(0);
        try(Connection c=DBConnection.getConnection()){
            if(c==null)return;
            String sql="SELECT m.mark_id,m.student_user_id,a.course_code,a.assessment_name,a.assessment_type,a.weight_percentage,m.mark,CASE WHEN a.assessment_type='CA' THEN ROUND(m.mark*a.weight_percentage/100,2) ELSE NULL END AS weighted FROM marks m JOIN assessments a ON a.assessment_id=m.assessment_id ORDER BY a.course_code,m.student_user_id,a.assessment_name";
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){
                while(r.next())model.addRow(new Object[]{r.getInt("mark_id"),r.getString("student_user_id"),r.getString("course_code"),r.getString("assessment_name"),r.getString("assessment_type"),r.getBigDecimal("weight_percentage"),r.getBigDecimal("mark"),r.getBigDecimal("weighted")});
            }
        }catch(Exception ex){error("Could not load marks: "+ex.getMessage());}
    }
    private void error(String m){JOptionPane.showMessageDialog(this,m==null?"Operation failed":m,"FAMS",JOptionPane.ERROR_MESSAGE);}
}

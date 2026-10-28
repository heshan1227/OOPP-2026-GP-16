package com.fams.gui;

import com.fams.database.DBConnection;
import com.fams.database.DatabaseInitializer;
import com.fams.model.User;
import com.fams.service.AuthService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Course master-data screen. Admin manages courses; other roles have read-only access. */
public class CourseManagementPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Course Code","Course Name","Theory Credits","Practical Credits","Department"},0) {
        public boolean isCellEditable(int r,int c){ return false; }
    };
    private final JTable table = new JTable(model);
    private final boolean admin = AuthService.getInstance().getCurrentUser()!=null &&
            AuthService.getInstance().getCurrentUser().getRole().name().equals("ADMIN");

    public CourseManagementPanel() {
        super(new BorderLayout(8,8));
        add(new JLabel("Course Management"), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add"), edit = new JButton("Edit"), delete = new JButton("Delete"), refresh = new JButton("Refresh");
        JButton materials = new JButton("Course Materials"), enroll = new JButton("Enroll Undergraduate");
        add.setEnabled(admin); edit.setEnabled(admin); delete.setEnabled(admin);
        actions.add(add); actions.add(edit); actions.add(delete); actions.add(materials); actions.add(enroll); actions.add(refresh); add(actions,BorderLayout.SOUTH);
        materials.addActionListener(e -> manageMaterial());
        enroll.setEnabled(admin);
        enroll.addActionListener(e -> enrollStudent());
        add.addActionListener(e -> editCourse(null));
        edit.addActionListener(e -> {int r=table.getSelectedRow(); if(r>=0) editCourse(r); else error("Select a course first.");});
        delete.addActionListener(e -> deleteCourse());
        refresh.addActionListener(e -> load());
        load();
    }
    private void load() {
        model.setRowCount(0);
        try (Connection c=DBConnection.getConnection()) {
            if(c==null) {
                for (java.util.Map.Entry<String,String> e: DatabaseInitializer.COURSES_MAP.entrySet())
                    model.addRow(new Object[]{e.getKey(),e.getValue(),2,1,"ICT Department"});
                return;
            }
            try(Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT course_code,course_name,theory_credits,practical_credits,department FROM courses ORDER BY course_code")) {
                while(r.next()) model.addRow(new Object[]{r.getString(1),r.getString(2),r.getInt(3),r.getInt(4),r.getString(5)});
            }
        } catch(Exception ex){error(ex.getMessage());}
    }
    private void editCourse(Integer row) {
        JTextField code=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,0)));
        JTextField name=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,1)));
        JTextField theory=new JTextField(row==null?"2":String.valueOf(model.getValueAt(row,2)));
        JTextField practical=new JTextField(row==null?"1":String.valueOf(model.getValueAt(row,3)));
        JTextField dept=new JTextField(row==null?"ICT Department":String.valueOf(model.getValueAt(row,4)));
        if(row!=null) code.setEditable(false);
        JPanel p=new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Course code"));p.add(code);p.add(new JLabel("Course name"));p.add(name);
        p.add(new JLabel("Theory credits"));p.add(theory);p.add(new JLabel("Practical credits"));p.add(practical);
        p.add(new JLabel("Department"));p.add(dept);
        if(JOptionPane.showConfirmDialog(this,p,row==null?"Add Course":"Edit Course",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try {
            String cc=code.getText().trim(), cn=name.getText().trim(), dp=dept.getText().trim();
            int tc=Integer.parseInt(theory.getText().trim()), pc=Integer.parseInt(practical.getText().trim());
            if(cc.isEmpty()||cn.isEmpty()||dp.isEmpty()||tc<0||pc<0)throw new IllegalArgumentException("Enter valid course details and non-negative credits.");
            try(Connection c=DBConnection.getConnection()) {
                if(c==null){DatabaseInitializer.COURSES_MAP.put(cc,cn);load();return;}
                String sql=row==null?"INSERT INTO courses(course_code,course_name,theory_credits,practical_credits,department) VALUES(?,?,?,?,?)":
                    "UPDATE courses SET course_name=?,theory_credits=?,practical_credits=?,department=? WHERE course_code=?";
                try(PreparedStatement s=c.prepareStatement(sql)){
                    if(row==null){s.setString(1,cc);s.setString(2,cn);s.setInt(3,tc);s.setInt(4,pc);s.setString(5,dp);}
                    else{s.setString(1,cn);s.setInt(2,tc);s.setInt(3,pc);s.setString(4,dp);s.setString(5,cc);}
                    s.executeUpdate();
                }
            }
            load();
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void deleteCourse(){
        int row=table.getSelectedRow(); if(row<0){error("Select a course first.");return;}
        String code=String.valueOf(model.getValueAt(row,0));
        if(JOptionPane.showConfirmDialog(this,"Delete course "+code+"?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try(Connection c=DBConnection.getConnection()){
            if(c==null)DatabaseInitializer.COURSES_MAP.remove(code);
            else try(PreparedStatement s=c.prepareStatement("DELETE FROM courses WHERE course_code=?")){s.setString(1,code);s.executeUpdate();}
            load();
        }catch(Exception ex){error(ex.getMessage());}
    }
    private String selectedCourseCode() {
        int row=table.getSelectedRow();
        if(row<0){error("Select a course first.");return null;}
        return String.valueOf(model.getValueAt(table.convertRowIndexToModel(row),0));
    }
    private void manageMaterial() {
        String code=selectedCourseCode(); if(code==null)return;
        try(Connection c=DBConnection.getConnection()){
            if(c==null)throw new SQLException("Course materials require MySQL. Start MySQL and import database_schema.sql.");
            DefaultTableModel mm=new DefaultTableModel(new Object[]{"ID","Title","File path / URL","Uploaded"},0);
            try(PreparedStatement q=c.prepareStatement("SELECT material_id,title,file_path,uploaded_date FROM course_materials WHERE course_code=? ORDER BY uploaded_date DESC")){
                q.setString(1,code);try(ResultSet r=q.executeQuery()){while(r.next())mm.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getTimestamp(4)});}
            }
            JTable mt=new JTable(mm); JTextField title=new JTextField(),path=new JTextField();
            JPanel form=new JPanel(new BorderLayout(5,5));form.add(new JScrollPane(mt),BorderLayout.CENTER);
            JPanel bottom=new JPanel(new GridLayout(0,2,5,5));bottom.add(new JLabel("Title"));bottom.add(title);bottom.add(new JLabel("File path / URL"));bottom.add(path);form.add(bottom,BorderLayout.SOUTH);
            Object[] options={"Add material","Delete selected","Close"};
            while(true){
                int choice=JOptionPane.showOptionDialog(this,form,"Materials for "+code,JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
                if(choice==0){
                    if(title.getText().trim().isEmpty()||path.getText().trim().isEmpty()){error("Title and file path are required.");continue;}
                    try(PreparedStatement ins=c.prepareStatement("INSERT INTO course_materials(course_code,lecturer_user_id,title,file_path) VALUES(?,?,?,?)")){
                        User u=AuthService.getInstance().getCurrentUser();ins.setString(1,code);ins.setString(2,u==null?null:u.getUserId());ins.setString(3,title.getText().trim());ins.setString(4,path.getText().trim());ins.executeUpdate();
                    }
                    title.setText("");path.setText("");mm.setRowCount(0);
                    try(PreparedStatement q=c.prepareStatement("SELECT material_id,title,file_path,uploaded_date FROM course_materials WHERE course_code=? ORDER BY uploaded_date DESC")){q.setString(1,code);try(ResultSet r=q.executeQuery()){while(r.next())mm.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getTimestamp(4)});}}
                } else if(choice==1){
                    int rr=mt.getSelectedRow();if(rr<0){error("Select a material first.");continue;}
                    try(PreparedStatement del=c.prepareStatement("DELETE FROM course_materials WHERE material_id=?")){del.setInt(1,Integer.parseInt(String.valueOf(mm.getValueAt(mt.convertRowIndexToModel(rr),0))));del.executeUpdate();}
                    mm.removeRow(mt.convertRowIndexToModel(rr));
                } else break;
            }
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void enrollStudent() {
        String code=selectedCourseCode();if(code==null)return;
        String uid=JOptionPane.showInputDialog(this,"Undergraduate user ID (example: UG1):","Enroll Undergraduate",JOptionPane.QUESTION_MESSAGE);
        if(uid==null||uid.trim().isEmpty())return;
        try(Connection c=DBConnection.getConnection()){
            if(c==null)throw new SQLException("Enrollment requires MySQL. Start MySQL and import database_schema.sql.");
            try(PreparedStatement s=c.prepareStatement("INSERT INTO course_enrollments(student_user_id,course_code) VALUES(?,?) ON DUPLICATE KEY UPDATE active=TRUE")){
                s.setString(1,uid.trim());s.setString(2,code);s.executeUpdate();
            }
            JOptionPane.showMessageDialog(this,"Undergraduate enrolled in "+code+".");
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void error(String m){JOptionPane.showMessageDialog(this,m==null?"Operation failed":m,"FAMS",JOptionPane.ERROR_MESSAGE);}
}

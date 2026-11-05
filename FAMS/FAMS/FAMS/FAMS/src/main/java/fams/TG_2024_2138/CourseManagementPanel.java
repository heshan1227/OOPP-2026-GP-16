package fams.TG_2024_2138;

import fams.TG_2024_2133.Theme;

import fams.TG_2024_2133.User;
import fams.TG_2024_2133.UserManagement;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Course master-data screen. Admin manages courses; other roles have read-only access. */
public class CourseManagementPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Course Code","Course Name","Theory Credits","Practical Credits","Department"},0) {
        public boolean isCellEditable(int r,int c){ return false; }
    };
    private final JTable table = new JTable(model);
    private final CourseManagement courses = new CourseManagement();
    private final AcademicManagement academic = new AcademicManagement();
    private final boolean admin = UserManagement.getInstance().getCurrentUser()!=null &&
            UserManagement.getInstance().getCurrentUser().getRole().name().equals("ADMIN");

    public CourseManagementPanel() {
        super(new BorderLayout(8,8));
        add(new JLabel("Course Management"), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = Theme.styleButton(new JButton("Add")), edit = Theme.styleButton(new JButton("Edit")), delete = Theme.styleButton(new JButton("Delete")), refresh = Theme.styleButton(new JButton("Refresh"));
        JButton materials = Theme.styleButton(new JButton("Course Materials")), enroll = Theme.styleButton(new JButton("Enroll Undergraduate"));
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
        try {
            for (CourseManagement.Course c : courses.listCourses())
                model.addRow(new Object[]{c.code(),c.name(),c.theoryCredits(),c.practicalCredits(),c.department()});
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
            courses.saveCourse(new CourseManagement.Course(code.getText().trim(),name.getText().trim(),
                    Integer.parseInt(theory.getText().trim()),Integer.parseInt(practical.getText().trim()),dept.getText().trim()),row==null);
            load();
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void deleteCourse(){
        int row=table.getSelectedRow(); if(row<0){error("Select a course first.");return;}
        String code=String.valueOf(model.getValueAt(row,0));
        if(JOptionPane.showConfirmDialog(this,"Delete course "+code+"?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{ courses.deleteCourse(code); load(); }catch(Exception ex){error(ex.getMessage());}
    }
    private String selectedCourseCode() {
        int row=table.getSelectedRow();
        if(row<0){error("Select a course first.");return null;}
        return String.valueOf(model.getValueAt(table.convertRowIndexToModel(row),0));
    }
    private void fillMaterials(DefaultTableModel mm,String code) throws java.sql.SQLException {
        mm.setRowCount(0);
        for(CourseManagement.Material m:courses.listMaterials(code))
            mm.addRow(new Object[]{m.id(),m.title(),m.filePath(),m.uploaded()});
    }
    private void manageMaterial() {
        String code=selectedCourseCode(); if(code==null)return;
        try{
            DefaultTableModel mm=new DefaultTableModel(new Object[]{"ID","Title","File path / URL","Uploaded"},0);
            fillMaterials(mm,code);
            JTable mt=new JTable(mm); JTextField title=new JTextField(),path=new JTextField();
            JPanel form=new JPanel(new BorderLayout(5,5));form.add(new JScrollPane(mt),BorderLayout.CENTER);
            JPanel bottom=new JPanel(new GridLayout(0,2,5,5));bottom.add(new JLabel("Title"));bottom.add(title);bottom.add(new JLabel("File path / URL"));bottom.add(path);form.add(bottom,BorderLayout.SOUTH);
            Object[] options={"Add material","Delete selected","Close"};
            while(true){
                int choice=JOptionPane.showOptionDialog(this,form,"Materials for "+code,JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
                if(choice==0){
                    try{
                        User u=UserManagement.getInstance().getCurrentUser();
                        courses.addMaterial(code,u==null?null:u.getUserId(),title.getText(),path.getText());
                        title.setText("");path.setText("");fillMaterials(mm,code);
                    }catch(Exception ex){error(ex.getMessage());}
                } else if(choice==1){
                    int rr=mt.getSelectedRow();if(rr<0){error("Select a material first.");continue;}
                    int modelRow=mt.convertRowIndexToModel(rr);
                    try{
                        courses.deleteMaterial(Integer.parseInt(String.valueOf(mm.getValueAt(modelRow,0))));
                        mm.removeRow(modelRow);
                    }catch(Exception ex){error(ex.getMessage());}
                } else break;
            }
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void enrollStudent() {
        String code=selectedCourseCode();if(code==null)return;
        String uid=JOptionPane.showInputDialog(this,"Undergraduate user ID (example: UG1):","Enroll Undergraduate",JOptionPane.QUESTION_MESSAGE);
        if(uid==null||uid.trim().isEmpty())return;
        try{
            academic.enrollStudent(uid.trim(),code);
            JOptionPane.showMessageDialog(this,"Undergraduate enrolled in "+code+".");
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void error(String m){JOptionPane.showMessageDialog(this,m==null?"Operation failed":m,"FAMS",JOptionPane.ERROR_MESSAGE);}
}

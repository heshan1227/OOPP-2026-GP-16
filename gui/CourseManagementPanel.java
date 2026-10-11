package gui;

import dao.CourseDAO;
import dao.CourseDAOImpl;
import model.Course;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** Admin-facing TG2138 module: courses, course materials, and course enrollment. */
public class CourseManagementPanel extends JPanel {
    private final CourseDAO dao = new CourseDAOImpl();
    private final DefaultTableModel coursesModel = model("ID", "Code", "Course Name", "Credits", "Components", "Period ID", "Description");
    private final DefaultTableModel materialsModel = model("Material ID", "Title", "File / URL", "Uploaded");
    private final DefaultTableModel enrollmentModel = model("Enrollment ID", "UG ID", "Registration No.", "Status", "Enrolled");
    private final JTable coursesTable = new JTable(coursesModel);
    private final JTable materialsTable = new JTable(materialsModel);
    private final JTable enrollmentTable = new JTable(enrollmentModel);
    private final JTextField searchField = new JTextField(22);

    public CourseManagementPanel() {
        super(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        add(createCourseToolbar(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Courses", new JScrollPane(coursesTable));
        tabs.addTab("Course Materials", createMaterialsTab());
        tabs.addTab("Undergraduate Enrollments", createEnrollmentTab());
        add(tabs, BorderLayout.CENTER);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        coursesTable.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) refreshRelated(); });
        loadCourses("");
    }

    private static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols,0) { @Override public boolean isCellEditable(int r,int c){return false;} };
    }
    private JPanel createCourseToolbar() {
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton search=new JButton("Search"), all=new JButton("Refresh"), add=new JButton("Add Course"), edit=new JButton("Edit Course"), delete=new JButton("Delete Course");
        p.add(new JLabel("Code or name:")); p.add(searchField); p.add(search); p.add(all); p.add(add); p.add(edit); p.add(delete);
        search.addActionListener(e->loadCourses(searchField.getText()));
        searchField.addActionListener(e->loadCourses(searchField.getText()));
        all.addActionListener(e->{searchField.setText("");loadCourses("");});
        add.addActionListener(e->editCourse(null)); edit.addActionListener(e->editCourse(selectedCourse())); delete.addActionListener(e->deleteCourse());
        return p;
    }
    private JPanel createMaterialsTab() {
        JPanel p=new JPanel(new BorderLayout(6,6)); JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=new JButton("Add Material"), edit=new JButton("Edit Selected"), remove=new JButton("Remove Selected"); bar.add(add);bar.add(edit);bar.add(remove);p.add(bar,BorderLayout.NORTH);p.add(new JScrollPane(materialsTable),BorderLayout.CENTER);
        add.addActionListener(e->addMaterial()); edit.addActionListener(e->editMaterial()); remove.addActionListener(e->deleteMaterial()); return p;
    }
    private JPanel createEnrollmentTab() {
        JPanel p=new JPanel(new BorderLayout(6,6)); JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton enroll=new JButton("Enroll Undergraduate"), withdraw=new JButton("Withdraw Selected");bar.add(enroll);bar.add(withdraw);p.add(bar,BorderLayout.NORTH);p.add(new JScrollPane(enrollmentTable),BorderLayout.CENTER);
        enroll.addActionListener(e->enrollStudent()); withdraw.addActionListener(e->withdrawStudent()); return p;
    }
    private Course selectedCourse() {
        int row=coursesTable.getSelectedRow(); if(row<0){message("Select a course first.",JOptionPane.WARNING_MESSAGE);return null;}
        row=coursesTable.convertRowIndexToModel(row); int id=(Integer)coursesModel.getValueAt(row,0);
        return new Course(id,(String)coursesModel.getValueAt(row,1),(String)coursesModel.getValueAt(row,2),
            (Integer)coursesModel.getValueAt(row,3),(String)coursesModel.getValueAt(row,4),
            coursesModel.getValueAt(row,6)==null?"":String.valueOf(coursesModel.getValueAt(row,6)),(Integer)coursesModel.getValueAt(row,5));
    }
    private void loadCourses(String q) {
        coursesModel.setRowCount(0);
        try { for(Course c:dao.searchCourses(q)) coursesModel.addRow(new Object[]{c.getCourseId(),c.getCourseCode(),c.getCourseName(),c.getCredits(),c.getComponentType(),c.getPeriodId(),c.getDescription()}); }
        catch(SQLException ex){showDbError(ex);}
    }
    private void refreshRelated() {
        Course c=selectedCourseOrNull(); materialsModel.setRowCount(0);enrollmentModel.setRowCount(0); if(c==null)return;
        try { for(String[] r:dao.getMaterials(c.getCourseId())) materialsModel.addRow(r); for(String[] r:dao.getEnrollments(c.getCourseId())) enrollmentModel.addRow(r); }
        catch(SQLException ex){showDbError(ex);}
    }
    private Course selectedCourseOrNull(){int row=coursesTable.getSelectedRow();return row<0?null:selectedCourseQuiet();}
    private Course selectedCourseQuiet(){int row=coursesTable.convertRowIndexToModel(coursesTable.getSelectedRow());return new Course((Integer)coursesModel.getValueAt(row,0),"","",0,"","",null);}
    private void editCourse(Course existing) {
        JTextField code=new JTextField(existing==null?"":existing.getCourseCode()); JTextField name=new JTextField(existing==null?"":existing.getCourseName());
        JTextField credits=new JTextField(existing==null?"":String.valueOf(existing.getCredits()));
        JComboBox<String> component=new JComboBox<>(new String[]{"Theory","Practical","Both"});
        JTextField period=new JTextField(existing==null||existing.getPeriodId()==null?"":String.valueOf(existing.getPeriodId()));
        JTextArea description=new JTextArea(existing==null?"":existing.getDescription(),4,24); description.setLineWrap(true);description.setWrapStyleWord(true);
        JPanel form=new JPanel(new GridLayout(0,2,5,5)); form.add(new JLabel("Course code *"));form.add(code);form.add(new JLabel("Course name *"));form.add(name);form.add(new JLabel("Credits (1-20) *"));form.add(credits);form.add(new JLabel("Component type"));form.add(component);form.add(new JLabel("Academic period ID (optional)"));form.add(period);form.add(new JLabel("Description"));form.add(new JScrollPane(description));
        if(existing!=null) component.setSelectedItem(existing.getComponentType());
        if(JOptionPane.showConfirmDialog(this,form,existing==null?"Add Course":"Edit Course",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return;
        try {
            int cr=Integer.parseInt(credits.getText().trim()); if(code.getText().trim().isEmpty()||name.getText().trim().isEmpty()||cr<1||cr>20)throw new IllegalArgumentException("Enter a code, name and credits from 1 to 20.");
            Integer periodId=period.getText().trim().isEmpty()?null:Integer.valueOf(period.getText().trim());
            Course c=new Course(existing==null?0:existing.getCourseId(),code.getText().trim(),name.getText().trim(),cr,(String)component.getSelectedItem(),description.getText(),periodId);
            if(dao.saveCourse(c)){message("Course saved.",JOptionPane.INFORMATION_MESSAGE);loadCourses(searchField.getText());}else message("No course record was changed.",JOptionPane.WARNING_MESSAGE);
        } catch(NumberFormatException ex){message("Credits and period ID must be whole numbers.",JOptionPane.WARNING_MESSAGE);} catch(IllegalArgumentException ex){message(ex.getMessage(),JOptionPane.WARNING_MESSAGE);} catch(SQLException ex){showDbError(ex);}
    }
    private void deleteCourse() {
        Course c=selectedCourse();if(c==null)return;
        if(JOptionPane.showConfirmDialog(this,"Delete course "+c.getCourseCode()+"? Deletion is blocked when dependent records exist.","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{dao.deleteCourse(c.getCourseId());loadCourses(searchField.getText());refreshRelated();}catch(SQLException ex){message("Could not delete course; remove or retain its dependent academic records first.\n"+ex.getMessage(),JOptionPane.ERROR_MESSAGE);}
    }
    private void addMaterial() {
        Course c=selectedCourse();if(c==null)return;
        JTextField title=new JTextField(),path=new JTextField();JPanel f=new JPanel(new GridLayout(0,2,5,5));f.add(new JLabel("Title *"));f.add(title);f.add(new JLabel("File path or URL *"));f.add(path);
        if(JOptionPane.showConfirmDialog(this,f,"Add course material",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        if(title.getText().trim().isEmpty()||path.getText().trim().isEmpty()){message("Both material title and file path/URL are required.",JOptionPane.WARNING_MESSAGE);return;}
        try{if(dao.saveMaterial(c.getCourseId(),0,title.getText(),path.getText())){message("Material added.",JOptionPane.INFORMATION_MESSAGE);refreshRelated();}}catch(SQLException ex){showDbError(ex);}
    }
    private void editMaterial(){
        int r=materialsTable.getSelectedRow();if(r<0){message("Select a material first.",JOptionPane.WARNING_MESSAGE);return;}
        int id=Integer.parseInt((String)materialsModel.getValueAt(r,0));JTextField title=new JTextField((String)materialsModel.getValueAt(r,1));JTextField path=new JTextField((String)materialsModel.getValueAt(r,2));
        JPanel f=new JPanel(new GridLayout(0,2,5,5));f.add(new JLabel("Title *"));f.add(title);f.add(new JLabel("File path or URL *"));f.add(path);
        if(JOptionPane.showConfirmDialog(this,f,"Edit course material",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        if(title.getText().trim().isEmpty()||path.getText().trim().isEmpty()){message("Both material title and file path/URL are required.",JOptionPane.WARNING_MESSAGE);return;}
        try{if(dao.updateMaterial(id,title.getText(),path.getText())){message("Material updated.",JOptionPane.INFORMATION_MESSAGE);refreshRelated();}}catch(SQLException ex){showDbError(ex);}
    }
    private void deleteMaterial(){int r=materialsTable.getSelectedRow();if(r<0){message("Select a material first.",JOptionPane.WARNING_MESSAGE);return;}int id=Integer.parseInt((String)materialsModel.getValueAt(r,0));if(JOptionPane.showConfirmDialog(this,"Remove selected material?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try{dao.deleteMaterial(id);refreshRelated();}catch(SQLException ex){showDbError(ex);}}
    private void enrollStudent(){Course c=selectedCourse();if(c==null)return;try{
        List<String[]> ugs=dao.getUndergraduates();if(ugs.isEmpty()){message("No undergraduate records found. Add undergraduate records before enrolling.",JOptionPane.INFORMATION_MESSAGE);return;}
        String[] options=new String[ugs.size()];for(int i=0;i<ugs.size();i++)options[i]=ugs.get(i)[1]+" (UG ID "+ugs.get(i)[0]+")";
        int picked=JOptionPane.showOptionDialog(this,"Choose an undergraduate to enroll:","Course Enrollment",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0]);if(picked<0)return;
        if(dao.enroll(Integer.parseInt(ugs.get(picked)[0]),c.getCourseId())){message("Undergraduate enrolled.",JOptionPane.INFORMATION_MESSAGE);refreshRelated();}
    }catch(SQLException ex){showDbError(ex);}}
    private void withdrawStudent(){int r=enrollmentTable.getSelectedRow();if(r<0){message("Select an enrollment first.",JOptionPane.WARNING_MESSAGE);return;}int id=Integer.parseInt((String)enrollmentModel.getValueAt(r,0));if(JOptionPane.showConfirmDialog(this,"Mark this enrollment as dropped?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try{dao.withdraw(id);refreshRelated();}catch(SQLException ex){showDbError(ex);}}
    private void showDbError(SQLException ex){message("Database operation failed. Check MySQL is running and run database/fams_tg2138_setup.sql.\n"+ex.getMessage(),JOptionPane.ERROR_MESSAGE);}
    private void message(String text,int type){JOptionPane.showMessageDialog(this,text,type==JOptionPane.ERROR_MESSAGE?"Database Error":"TG2138 Course Management",type);}
}

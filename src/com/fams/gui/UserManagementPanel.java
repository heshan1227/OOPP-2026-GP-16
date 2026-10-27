package com.fams.gui;

import com.fams.database.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Administrator-only user CRUD screen backed by the shared users table. */
public class UserManagementPanel extends JPanel {
    private final DefaultTableModel model=new DefaultTableModel(new Object[]{"User ID","Username","Full Name","Email","Department","Role","Student Reg No","Batch"},0){
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable table=new JTable(model);
    public UserManagementPanel(){
        super(new BorderLayout(8,8));add(new JLabel("User & Role Management — Administrator"),BorderLayout.NORTH);
        add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=new JButton("Add User"),edit=new JButton("Edit User"),deactivate=new JButton("Delete User"),refresh=new JButton("Refresh");
        actions.add(add);actions.add(edit);actions.add(deactivate);actions.add(refresh);add(actions,BorderLayout.SOUTH);
        boolean admin=com.fams.service.AuthService.getInstance().getCurrentUser()!=null &&
            com.fams.service.AuthService.getInstance().getCurrentUser().getRole().name().equals("ADMIN");
        add.setEnabled(admin);edit.setEnabled(admin);deactivate.setEnabled(admin);
        add.addActionListener(e->editUser(null));
        edit.addActionListener(e->{int r=table.getSelectedRow();if(r<0){error("Select a user first.");return;}editUser(table.convertRowIndexToModel(r));});
        deactivate.addActionListener(e->deleteUser());refresh.addActionListener(e->load());load();
    }
    private void load(){
        model.setRowCount(0);
        try(Connection c=DBConnection.getConnection()){
            if(c==null){error("User management requires MySQL. Start MySQL and import database_schema.sql.");return;}
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT user_id,username,full_name,email,department,role,student_reg_no,batch FROM users ORDER BY role,full_name")){
                while(r.next())model.addRow(new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getString(7),r.getString(8)});
            }
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void editUser(Integer row){
        JTextField id=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,0)));
        JTextField username=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,1)));
        JPasswordField password=new JPasswordField();
        JTextField name=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,2)));
        JTextField email=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,3)));
        JTextField dept=new JTextField(row==null?"ICT Department":String.valueOf(model.getValueAt(row,4)));
        JComboBox<String> role=new JComboBox<>(new String[]{"ADMIN","LECTURER","TECHNICAL_OFFICER","UNDERGRADUATE"});
        JTextField reg=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,6)));
        JTextField batch=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,7)));
        if(row!=null){role.setSelectedItem(String.valueOf(model.getValueAt(row,5)));id.setEditable(false);username.setEditable(false);}
        JPanel f=new JPanel(new GridLayout(0,2,5,5));
        f.add(new JLabel("User ID"));f.add(id);f.add(new JLabel("Username"));f.add(username);f.add(new JLabel(row==null?"Password":"New password (optional)"));f.add(password);
        f.add(new JLabel("Full name"));f.add(name);f.add(new JLabel("Email"));f.add(email);f.add(new JLabel("Department"));f.add(dept);f.add(new JLabel("Role"));f.add(role);f.add(new JLabel("Student registration no"));f.add(reg);f.add(new JLabel("Batch"));f.add(batch);
        if(JOptionPane.showConfirmDialog(this,f,row==null?"Add User":"Edit User",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{
            if(id.getText().trim().isEmpty()||username.getText().trim().isEmpty()||name.getText().trim().isEmpty()||dept.getText().trim().isEmpty())throw new IllegalArgumentException("User ID, username, name and department are required.");
            try(Connection c=DBConnection.getConnection()){
                if(c==null)throw new SQLException("User management requires MySQL.");
                if(row==null){
                    if(password.getPassword().length==0)throw new IllegalArgumentException("Password is required for a new user.");
                    try(PreparedStatement s=c.prepareStatement("INSERT INTO users(user_id,username,password,full_name,email,phone,department,role,student_reg_no,batch) VALUES(?,?,?,?,?,'',?,?,?,?)")){
                        s.setString(1,id.getText().trim());s.setString(2,username.getText().trim());s.setString(3,new String(password.getPassword()));s.setString(4,name.getText().trim());s.setString(5,email.getText().trim());s.setString(6,dept.getText().trim());s.setString(7,String.valueOf(role.getSelectedItem()));s.setString(8,emptyToNull(reg.getText()));s.setString(9,emptyToNull(batch.getText()));s.executeUpdate();
                    }
                }else{
                    String sql=password.getPassword().length==0?
                        "UPDATE users SET full_name=?,email=?,department=?,role=?,student_reg_no=?,batch=? WHERE user_id=?":
                        "UPDATE users SET full_name=?,email=?,department=?,role=?,student_reg_no=?,batch=?,password=? WHERE user_id=?";
                    try(PreparedStatement s=c.prepareStatement(sql)){
                        s.setString(1,name.getText().trim());s.setString(2,email.getText().trim());s.setString(3,dept.getText().trim());s.setString(4,String.valueOf(role.getSelectedItem()));s.setString(5,emptyToNull(reg.getText()));s.setString(6,emptyToNull(batch.getText()));
                        if(password.getPassword().length==0)s.setString(7,id.getText().trim());else{s.setString(7,new String(password.getPassword()));s.setString(8,id.getText().trim());}s.executeUpdate();
                    }
                }
            }
            load();
        }catch(Exception ex){error(ex.getMessage());}
    }
    private String emptyToNull(String s){return s==null||s.trim().isEmpty()?null:s.trim();}
    private void deleteUser(){
        int r=table.getSelectedRow();if(r<0){error("Select a user first.");return;}
        String id=String.valueOf(model.getValueAt(table.convertRowIndexToModel(r),0));
        if(JOptionPane.showConfirmDialog(this,"Delete user "+id+"? Dependent records may also be removed.","Confirm deletion",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try(Connection c=DBConnection.getConnection()){if(c==null)throw new SQLException("User management requires MySQL.");try(PreparedStatement s=c.prepareStatement("DELETE FROM users WHERE user_id=?")){s.setString(1,id);s.executeUpdate();}load();}
        catch(Exception ex){error(ex.getMessage());}
    }
    private void error(String m){JOptionPane.showMessageDialog(this,m==null?"Operation failed":m,"FAMS",JOptionPane.ERROR_MESSAGE);}
}

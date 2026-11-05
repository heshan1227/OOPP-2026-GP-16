package com.fams.gui;

import com.fams.database.DBConnection;
import com.fams.service.SriLankanNic;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;

/** Administrator-only user CRUD screen backed by the shared users table. */
public class UserManagementPanel extends JPanel {
    private final DefaultTableModel model=new DefaultTableModel(new Object[]{"User ID","Username","Full Name","Email","Department","Role","Student Reg No","Batch","NIC Number","Date of Birth"},0){ public boolean isCellEditable(int r,int c){return false;} };
    private final JTable table=new JTable(model);
    public UserManagementPanel(){
        super(new BorderLayout(8,8)); add(new JLabel("User & Role Management — Administrator"),BorderLayout.NORTH); add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=Theme.styleButton(new JButton("Add User")),edit=Theme.styleButton(new JButton("Edit User")),view=Theme.styleButton(new JButton("View User")),deactivate=Theme.styleButton(new JButton("Delete User")),refresh=Theme.styleButton(new JButton("Refresh"));
        actions.add(add);actions.add(edit);actions.add(view);actions.add(deactivate);actions.add(refresh);add(actions,BorderLayout.SOUTH);
        boolean admin=com.fams.service.AuthService.getInstance().getCurrentUser()!=null && com.fams.service.AuthService.getInstance().getCurrentUser().getRole().name().equals("ADMIN");
        add.setEnabled(admin);edit.setEnabled(admin);deactivate.setEnabled(admin);
        add.addActionListener(e->editUser(null)); edit.addActionListener(e->{int r=selected();if(r>=0)editUser(r);});
        view.addActionListener(e->viewUser()); deactivate.addActionListener(e->deleteUser());refresh.addActionListener(e->load());load();
    }
    private int selected(){int r=table.getSelectedRow();if(r<0){error("Select a user first.");return -1;}return table.convertRowIndexToModel(r);}
    private void load(){
        model.setRowCount(0);
        try(Connection c=DBConnection.getConnection()){
            if(c==null){error("User management requires MySQL. Start MySQL and run the database setup script.");return;}
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT user_id,username,full_name,email,department,role,student_reg_no,batch,nic_number,date_of_birth FROM users ORDER BY role,full_name")){
                while(r.next())model.addRow(new Object[]{r.getString("user_id"),r.getString("username"),r.getString("full_name"),r.getString("email"),r.getString("department"),r.getString("role"),r.getString("student_reg_no"),r.getString("batch"),r.getString("nic_number"),r.getDate("date_of_birth")});
            }
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void viewUser(){int r=selected();if(r<0)return;StringBuilder b=new StringBuilder();String[] labels={"User ID","Username","Full Name","Email","Department","Role","Student Reg No","Batch","NIC Number","Date of Birth"};for(int i=0;i<labels.length;i++)b.append(labels[i]).append(": ").append(model.getValueAt(r,i)==null?"—":model.getValueAt(r,i)).append('\n');JOptionPane.showMessageDialog(this,b.toString(),"User Profile",JOptionPane.INFORMATION_MESSAGE);}
    private void editUser(Integer row){
        JTextField id=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,0))),username=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,1)));
        JPasswordField password=new JPasswordField(); JTextField name=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,2))),email=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,3))),dept=new JTextField(row==null?"ICT Department":String.valueOf(model.getValueAt(row,4)));
        JComboBox<String> role=new JComboBox<>(new String[]{"ADMIN","LECTURER","TECHNICAL_OFFICER","UNDERGRADUATE"});
        JTextField reg=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,6))),batch=new JTextField(row==null?"":String.valueOf(model.getValueAt(row,7)));
        JTextField nic=new JTextField(row==null?"":nvl(model.getValueAt(row,8))); JTextField dob=new JTextField(row==null?"":nvl(model.getValueAt(row,9))),birthYear=new JTextField();
        dob.setEditable(false);birthYear.setEditable(false);dob.setToolTipText("Automatically extracted from NIC; read-only");birthYear.setToolTipText("Automatically extracted from NIC; read-only");
        if(row!=null){role.setSelectedItem(String.valueOf(model.getValueAt(row,5)));id.setEditable(false);username.setEditable(false);}
        Runnable updateDob=()->{try{SriLankanNic.Details d=SriLankanNic.parse(nic.getText());dob.setText(d.getFormattedDateOfBirth());birthYear.setText(d.getBirthYear());dob.setForeground(new Color(20,110,55));}catch(Exception ex){dob.setText("");birthYear.setText("");dob.setForeground(Color.RED);}};
        nic.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){updateDob.run();}public void removeUpdate(DocumentEvent e){updateDob.run();}public void changedUpdate(DocumentEvent e){updateDob.run();}});if(!nic.getText().isEmpty())updateDob.run();
        JPanel f=new JPanel(new GridLayout(0,2,5,5));
        add(f,"User ID",id);add(f,"Username",username);add(f,row==null?"Password":"New password (optional)",password);add(f,"Full name",name);add(f,"Email",email);add(f,"Department",dept);add(f,"Role",role);add(f,"Student registration no",reg);add(f,"Batch",batch);add(f,"NIC Number",nic);add(f,"Date of Birth (auto; read only)",dob);add(f,"Birth Year (locked)",birthYear);
        if(JOptionPane.showConfirmDialog(this,f,row==null?"Add User":"Edit User",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{
            if(id.getText().trim().isEmpty()||username.getText().trim().isEmpty()||name.getText().trim().isEmpty()||dept.getText().trim().isEmpty())throw new IllegalArgumentException("User ID, username, name and department are required.");
            SriLankanNic.Details nd=SriLankanNic.parse(nic.getText());
            try(Connection c=DBConnection.getConnection()){
                if(c==null)throw new SQLException("User management requires MySQL.");
                if(row==null){if(password.getPassword().length==0)throw new IllegalArgumentException("Password is required for a new user.");
                    try(PreparedStatement s=c.prepareStatement("INSERT INTO users(user_id,username,password,full_name,email,phone,department,role,student_reg_no,batch,nic_number,date_of_birth) VALUES(?,?,?,?,?,'',?,?,?,?,?,?)")){
                        s.setString(1,id.getText().trim());s.setString(2,username.getText().trim());s.setString(3,new String(password.getPassword()));s.setString(4,name.getText().trim());s.setString(5,emptyToNull(email.getText()));s.setString(6,dept.getText().trim());s.setString(7,String.valueOf(role.getSelectedItem()));s.setString(8,emptyToNull(reg.getText()));s.setString(9,emptyToNull(batch.getText()));s.setString(10,nd.getNic());s.setDate(11,Date.valueOf(nd.getDateOfBirth()));s.executeUpdate();}
                }else{String sql=password.getPassword().length==0?"UPDATE users SET full_name=?,email=?,department=?,role=?,student_reg_no=?,batch=?,nic_number=?,date_of_birth=? WHERE user_id=?":"UPDATE users SET full_name=?,email=?,department=?,role=?,student_reg_no=?,batch=?,nic_number=?,date_of_birth=?,password=? WHERE user_id=?";
                    try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,name.getText().trim());s.setString(2,emptyToNull(email.getText()));s.setString(3,dept.getText().trim());s.setString(4,String.valueOf(role.getSelectedItem()));s.setString(5,emptyToNull(reg.getText()));s.setString(6,emptyToNull(batch.getText()));s.setString(7,nd.getNic());s.setDate(8,Date.valueOf(nd.getDateOfBirth()));int ix=9;if(password.getPassword().length>0)s.setString(ix++,new String(password.getPassword()));s.setString(ix,id.getText().trim());s.executeUpdate();}}
            }load();
        }catch(Exception ex){error(ex.getMessage());}
    }
    private void add(JPanel p,String label,Component c){p.add(new JLabel(label));p.add(c);}
    private String nvl(Object o){return o==null?"":String.valueOf(o);}
    private String emptyToNull(String s){return s==null||s.trim().isEmpty()?null:s.trim();}
    private void deleteUser(){int r=selected();if(r<0)return;String id=String.valueOf(model.getValueAt(r,0));if(JOptionPane.showConfirmDialog(this,"Delete user "+id+"? Dependent records may also be removed.","Confirm deletion",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try(Connection c=DBConnection.getConnection()){if(c==null)throw new SQLException("User management requires MySQL.");try(PreparedStatement s=c.prepareStatement("DELETE FROM users WHERE user_id=?")){s.setString(1,id);s.executeUpdate();}load();}catch(Exception ex){error(ex.getMessage());}}
    private void error(String m){JOptionPane.showMessageDialog(this,m==null?"Operation failed":m,"FAMS",JOptionPane.ERROR_MESSAGE);}
}

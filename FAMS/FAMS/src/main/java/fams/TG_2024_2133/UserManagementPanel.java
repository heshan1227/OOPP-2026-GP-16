package fams.TG_2024_2133;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Administrator-only user CRUD screen backed by the shared users table. */
public class UserManagementPanel extends JPanel {
    private final DefaultTableModel model=new DefaultTableModel(new Object[]{"User ID","Username","Full Name","Email","Department","Role","Student Reg No","Batch"},0){
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable table=new JTable(model);
    private final AdminManagement adminMgmt=new AdminManagement();
    public UserManagementPanel(){
        super(new BorderLayout(8,8));add(new JLabel("User & Role Management — Administrator"),BorderLayout.NORTH);
        add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=Theme.styleButton(new JButton("Add User")),edit=Theme.styleButton(new JButton("Edit User")),deactivate=Theme.styleButton(new JButton("Delete User")),refresh=Theme.styleButton(new JButton("Refresh"));
        actions.add(add);actions.add(edit);actions.add(deactivate);actions.add(refresh);add(actions,BorderLayout.SOUTH);
        boolean admin=fams.TG_2024_2133.UserManagement.getInstance().getCurrentUser()!=null &&
            fams.TG_2024_2133.UserManagement.getInstance().getCurrentUser().getRole().name().equals("ADMIN");
        add.setEnabled(admin);edit.setEnabled(admin);deactivate.setEnabled(admin);
        add.addActionListener(e->editUser(null));
        edit.addActionListener(e->{int r=table.getSelectedRow();if(r<0){error("Select a user first.");return;}editUser(table.convertRowIndexToModel(r));});
        deactivate.addActionListener(e->deleteUser());refresh.addActionListener(e->load());load();
    }
    private void load(){
        model.setRowCount(0);
        try{
            for(AdminManagement.UserRow u:adminMgmt.listUsers())
                model.addRow(new Object[]{u.userId(),u.username(),u.fullName(),u.email(),u.department(),u.role(),u.studentRegNo(),u.batch()});
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
            AdminManagement.UserRow u=new AdminManagement.UserRow(id.getText().trim(),username.getText().trim(),name.getText().trim(),email.getText().trim(),dept.getText().trim(),String.valueOf(role.getSelectedItem()),emptyToNull(reg.getText()),emptyToNull(batch.getText()));
            String pw=new String(password.getPassword());
            if(row==null)adminMgmt.createUser(u,pw);else adminMgmt.updateUser(u,pw);
            load();
        }catch(Exception ex){error(ex.getMessage());}
    }
    private String emptyToNull(String s){return s==null||s.trim().isEmpty()?null:s.trim();}
    private void deleteUser(){
        int r=table.getSelectedRow();if(r<0){error("Select a user first.");return;}
        String id=String.valueOf(model.getValueAt(table.convertRowIndexToModel(r),0));
        if(JOptionPane.showConfirmDialog(this,"Delete user "+id+"? Dependent records may also be removed.","Confirm deletion",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{adminMgmt.deleteUser(id);load();}
        catch(Exception ex){error(ex.getMessage());}
    }
    private void error(String m){JOptionPane.showMessageDialog(this,m==null?"Operation failed":m,"FAMS",JOptionPane.ERROR_MESSAGE);}
}

package com.fams.gui;

import com.fams.model.ComponentType;
import com.fams.model.Role;
import com.fams.model.TimetableEntry;
import com.fams.model.User;
import com.fams.service.AuthService;
import com.fams.service.TimetableService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TimetablePanel extends JPanel {
    private final TimetableService timetableService = new TimetableService();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private JComboBox<String> cbDepartmentFilter, cbBatchFilter, cbDayFilter;
    private List<TimetableEntry> currentEntries = new ArrayList<>();

    public TimetablePanel() {
        setLayout(new BorderLayout(12,12)); setBackground(Theme.LIGHT_BG); setBorder(new EmptyBorder(15,15,15,15)); initComponents();
    }

    private void initComponents() {
        User user=AuthService.getInstance().getCurrentUser(); boolean admin=user!=null && user.getRole()==Role.ADMIN;
        JPanel top=Theme.createCardPanel(); top.setLayout(new BorderLayout(10,10));
        JLabel title=Theme.createHeaderLabel(admin ? "Timetable Management - Admin" : "Department Academic Timetables");
        top.add(title,BorderLayout.WEST);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); actions.setOpaque(false);
        if(admin){
            JButton add=Theme.createPrimaryButton("+ Add New Entry"); add.addActionListener(e->showTimetableDialog(null));
            JButton edit=Theme.createPrimaryButton("Edit Selected"); edit.addActionListener(e->editSelected());
            JButton del=Theme.createPrimaryButton("Delete Selected"); del.addActionListener(e->deleteSelected());
            actions.add(add); actions.add(edit); actions.add(del);
        }
        top.add(actions,BorderLayout.EAST); add(top,BorderLayout.NORTH);

        JPanel filter=Theme.createCardPanel(); filter.setLayout(new FlowLayout(FlowLayout.LEFT,8,2));
        filter.add(new JLabel("Search:")); txtSearch=new JTextField(14); filter.add(txtSearch);
        filter.add(new JLabel("Department:")); cbDepartmentFilter=new JComboBox<>(new String[]{"All","ICT Department","ET Department","BST Department","Other"}); filter.add(cbDepartmentFilter);
        filter.add(new JLabel("Batch:")); cbBatchFilter=new JComboBox<>(new String[]{"All","Batch 15","Batch 16","Other"}); filter.add(cbBatchFilter);
        filter.add(new JLabel("Day:")); cbDayFilter=new JComboBox<>(new String[]{"All","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"}); filter.add(cbDayFilter);
        JButton clear=Theme.createSecondaryButton("Clear Filters"); clear.addActionListener(e->{txtSearch.setText("");cbDepartmentFilter.setSelectedIndex(0);cbBatchFilter.setSelectedIndex(0);cbDayFilter.setSelectedIndex(0);applyFilters();}); filter.add(clear);
        javax.swing.event.DocumentListener dl=new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){applyFilters();}public void removeUpdate(javax.swing.event.DocumentEvent e){applyFilters();}public void changedUpdate(javax.swing.event.DocumentEvent e){applyFilters();}};
        txtSearch.getDocument().addDocumentListener(dl); cbDepartmentFilter.addActionListener(e->applyFilters()); cbBatchFilter.addActionListener(e->applyFilters()); cbDayFilter.addActionListener(e->applyFilters());
        add(filter,BorderLayout.SOUTH);

        String[] cols={"ID","Day","Start","End","Course Module","Course Name","Component","Venue","Lecturer","Department","Batch","Academic Period"};
        tableModel=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel); table.setFont(Theme.FONT_REGULAR); table.setRowHeight(27); table.setAutoCreateRowSorter(true); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(45); table.getColumnModel().getColumn(4).setPreferredWidth(100); table.getColumnModel().getColumn(5).setPreferredWidth(210);
        JPanel card=Theme.createCardPanel(); card.setLayout(new BorderLayout()); card.add(new JScrollPane(table),BorderLayout.CENTER); add(card,BorderLayout.CENTER);
        refreshTable();
    }

    private void refreshTable(){ currentEntries=timetableService.getTimetablesForCurrentSession(); applyFilters(); }

    private void applyFilters(){
        if(tableModel==null)return; tableModel.setRowCount(0); String q=txtSearch==null?"":txtSearch.getText().trim().toLowerCase();
        String dept=cbDepartmentFilter==null?"All":String.valueOf(cbDepartmentFilter.getSelectedItem());
        String batch=cbBatchFilter==null?"All":String.valueOf(cbBatchFilter.getSelectedItem()); String day=cbDayFilter==null?"All":String.valueOf(cbDayFilter.getSelectedItem());
        for(TimetableEntry t:currentEntries){
            String all=(t.getCourseCode()+" "+t.getCourseName()+" "+t.getVenue()+" "+t.getLecturerName()+" "+t.getDepartment()+" "+t.getBatch()+" "+t.getAcademicPeriod()).toLowerCase();
            if(!q.isEmpty()&&!all.contains(q))continue; if(!"All".equals(dept)&&!t.getDepartment().equalsIgnoreCase(dept)&&!("Other".equals(dept)&&!isKnownDept(t.getDepartment())))continue;
            if(!"All".equals(batch)&&!t.getBatch().equalsIgnoreCase(batch)&&!("Other".equals(batch)&&!isKnownBatch(t.getBatch())))continue; if(!"All".equals(day)&&!t.getDayOfWeek().equalsIgnoreCase(day))continue;
            tableModel.addRow(new Object[]{t.getEntryId(),t.getDayOfWeek(),t.getStartTime(),t.getEndTime(),t.getCourseCode(),t.getCourseName(),t.getComponentType().getDisplayName(),t.getVenue(),t.getLecturerName(),t.getDepartment(),t.getBatch(),t.getAcademicPeriod()});
        }
    }
    private boolean isKnownDept(String d){return d.equalsIgnoreCase("ICT Department")||d.equalsIgnoreCase("ET Department")||d.equalsIgnoreCase("BST Department");}
    private boolean isKnownBatch(String b){return b.equalsIgnoreCase("Batch 15")||b.equalsIgnoreCase("Batch 16");}

    private void editSelected(){
        int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a timetable entry first.","Edit Timetable",JOptionPane.WARNING_MESSAGE);return;}
        int modelRow=table.convertRowIndexToModel(row); int id=(Integer)tableModel.getValueAt(modelRow,0); TimetableEntry selected=null;
        for(TimetableEntry t:currentEntries)if(t.getEntryId()==id){selected=t;break;} if(selected!=null)showTimetableDialog(selected);
    }
    private void deleteSelected(){
        int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a timetable entry first.","Delete Timetable",JOptionPane.WARNING_MESSAGE);return;}
        int mr=table.convertRowIndexToModel(row); int id=(Integer)tableModel.getValueAt(mr,0);
        int ok=JOptionPane.showConfirmDialog(this,"Delete this timetable entry?\nID: "+id,"Confirm Delete",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(ok==JOptionPane.YES_OPTION){try{timetableService.deleteTimetableEntry(id);refreshTable();}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}}
    }

    private void showTimetableDialog(TimetableEntry existing){
        boolean edit=existing!=null; JDialog dlg=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),edit?"Edit Timetable Entry":"Add New Timetable Entry",true); dlg.setSize(650,650); dlg.setLocationRelativeTo(this);
        JPanel form=new JPanel(new GridBagLayout()); form.setBorder(new EmptyBorder(15,18,15,18)); GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(5,6,5,6); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1; int y=0;
        JTextField code=field(existing==null?"":existing.getCourseCode()), name=field(existing==null?"":existing.getCourseName()), start=field(existing==null?"08:00 AM":existing.getStartTime()), end=field(existing==null?"10:00 AM":existing.getEndTime()), venue=field(existing==null?"":existing.getVenue()), lec=field(existing==null?"":existing.getLecturerName()), dept=field(existing==null?"":existing.getDepartment()), batch=field(existing==null?"":existing.getBatch()), period=field(existing==null?"Semester 1 - 2026":existing.getAcademicPeriod());
        JComboBox<ComponentType> comp=new JComboBox<>(ComponentType.values()); JComboBox<String> day=new JComboBox<>(new String[]{"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"});
        if(edit){comp.setSelectedItem(existing.getComponentType());day.setSelectedItem(existing.getDayOfWeek());}
        y=addRow(form,g,y,"Course Module / Code:",code); y=addRow(form,g,y,"Course / Module Name:",name); y=addRow(form,g,y,"Component:",comp); y=addRow(form,g,y,"Day:",day); y=addRow(form,g,y,"Start Time:",start); y=addRow(form,g,y,"End Time:",end); y=addRow(form,g,y,"Venue:",venue); y=addRow(form,g,y,"Lecturer:",lec); y=addRow(form,g,y,"Department:",dept); y=addRow(form,g,y,"Batch:",batch); y=addRow(form,g,y,"Academic Period:",period);
        g.gridx=0;g.gridy=y;g.gridwidth=2; JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT)); bp.setOpaque(false); JButton save=Theme.createPrimaryButton(edit?"Save Changes":"Add Timetable Entry"); JButton cancel=Theme.createSecondaryButton("Cancel"); cancel.addActionListener(e->dlg.dispose()); bp.add(cancel);bp.add(save);form.add(bp,g);
        save.addActionListener(e->{try{TimetableEntry t=new TimetableEntry(edit?existing.getEntryId():0,code.getText().trim(),name.getText().trim(),(ComponentType)comp.getSelectedItem(),(String)day.getSelectedItem(),start.getText().trim(),end.getText().trim(),venue.getText().trim(),lec.getText().trim(),dept.getText().trim(),batch.getText().trim(),period.getText().trim()); if(edit)timetableService.updateTimetableEntry(t);else timetableService.addTimetableEntry(t); dlg.dispose();refreshTable();}catch(Exception ex){JOptionPane.showMessageDialog(dlg,ex.getMessage(),"Validation / Database Error",JOptionPane.ERROR_MESSAGE);}});
        dlg.add(new JScrollPane(form)); dlg.setVisible(true);
    }
    private JTextField field(String s){JTextField f=new JTextField(s);f.setFont(Theme.FONT_REGULAR);return f;}
    private int addRow(JPanel p,GridBagConstraints g,int y,String label,JComponent c){g.gridx=0;g.gridy=y;g.gridwidth=1;g.weightx=.35;p.add(new JLabel(label),g);g.gridx=1;g.weightx=.65;p.add(c,g);return y+1;}
}

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
        tabs.addTab("My Grades & GPA",gradesAndGpaPanel());
    }

    /** Displays final subject grades and the credit-weighted GPA for this undergraduate. */
    private JPanel gradesAndGpaPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Theme.LIGHT_BG);
        panel.setBorder(new EmptyBorder(14, 8, 8, 8));

        JLabel summary = new JLabel("Loading your academic results...");
        summary.setFont(new Font("Segoe UI", Font.BOLD, 16));
        summary.setForeground(Theme.PRIMARY_DARK);
        summary.setBorder(new EmptyBorder(8, 10, 8, 10));
        panel.add(summary, BorderLayout.NORTH);

        String[] columns = {"Subject", "Grade"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable resultTable = new JTable(model);
        resultTable.setRowHeight(28);
        resultTable.setFillsViewportHeight(true);
        resultTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        String sql = "SELECT c.course_code,c.course_name, " +
                "COALESCE(c.theory_credits,0)+COALESCE(c.practical_credits,0) AS credits, " +
                "COUNT(DISTINCT a.assessment_id) AS assessment_count, " +
                "COUNT(DISTINCT CASE WHEN m.mark_id IS NOT NULL THEN a.assessment_id END) AS marked_count, " +
                "COALESCE(SUM(a.weight_percentage),0) AS total_weight, " +
                "COALESCE(SUM(CASE WHEN m.mark_id IS NOT NULL THEN a.weight_percentage ELSE 0 END),0) AS marked_weight, " +
                "COALESCE(SUM(CASE WHEN m.mark_id IS NOT NULL THEN m.mark*a.weight_percentage/100.0 ELSE 0 END),0) AS final_mark " +
                "FROM course_enrollments e JOIN courses c ON c.course_code=e.course_code " +
                "LEFT JOIN assessments a ON a.course_code=c.course_code " +
                "LEFT JOIN marks m ON m.assessment_id=a.assessment_id AND m.student_user_id=e.student_user_id " +
                "WHERE e.student_user_id=? AND e.active=TRUE " +
                "GROUP BY c.course_code,c.course_name,c.theory_credits,c.practical_credits " +
                "ORDER BY c.course_code";
        double weightedPoints = 0.0;
        int totalGpaCredits = 0;
        int completed = 0;
        int pending = 0;
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                summary.setText("Connect to MySQL to view your grades and GPA.");
                return panel;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, user.getUserId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int credits = rs.getInt("credits");
                        double totalWeight = rs.getDouble("total_weight");
                        double markedWeight = rs.getDouble("marked_weight");
                        int assessmentCount = rs.getInt("assessment_count");
                        int markedCount = rs.getInt("marked_count");
                        boolean complete = assessmentCount > 0 && markedCount == assessmentCount
                                && totalWeight > 0 && markedWeight >= totalWeight - 0.01;
                        if (!complete) {
                            model.addRow(new Object[]{rs.getString("course_name"), "Pending"});
                            pending++;
                            continue;
                        }
                        double finalMark = rs.getDouble("final_mark");
                        // Normalize to 100 in case the configured assessment weights do not total exactly 100.
                        finalMark = finalMark * 100.0 / totalWeight;
                        finalMark = Math.max(0.0, Math.min(100.0, finalMark));
                        com.fams.model.GradeResult grade = com.fams.service.GradeCalculator.calculate(finalMark);
                        model.addRow(new Object[]{rs.getString("course_name"), grade.letterGrade()});
                        if (credits > 0) {
                            weightedPoints += grade.gradePoint() * credits;
                            totalGpaCredits += credits;
                        }
                        completed++;
                    }
                }
            }
            double gpa = totalGpaCredits == 0 ? 0.0 : Math.round(weightedPoints / totalGpaCredits * 100.0) / 100.0;
            summary.setText(String.format(java.util.Locale.US,
                    "Your GPA: %.2f", gpa));
        } catch (Exception ex) {
            summary.setText("Unable to calculate grades: " + ex.getMessage());
        }
        return panel;
    }

    private String studentKey(){return user instanceof UndergraduateUser?((UndergraduateUser)user).getStudentRegNo():user.getUserId();}
    private JScrollPane table(String sql,String... params){String[] headers={"Loading"};DefaultTableModelHolder holder=new DefaultTableModelHolder();JTable t=new JTable(holder.model);t.setFillsViewportHeight(true);try(Connection con=DBConnection.getConnection()){
        if(con==null){holder.model.addColumn("Information");holder.model.addRow(new Object[]{"Connect to MySQL to view your personal records."});}
        else try(PreparedStatement ps=con.prepareStatement(sql)){for(int i=0;i<params.length;i++)ps.setString(i+1,params[i]);try(ResultSet rs=ps.executeQuery()){ResultSetMetaData md=rs.getMetaData();holder.model.setColumnCount(0);for(int i=1;i<=md.getColumnCount();i++)holder.model.addColumn(md.getColumnLabel(i));while(rs.next()){Object[] row=new Object[md.getColumnCount()];for(int i=1;i<=row.length;i++)row[i-1]=rs.getObject(i);holder.model.addRow(row);}}}
        }catch(Exception ex){holder.model.setColumnCount(0);holder.model.addColumn("Could not load records");holder.model.addRow(new Object[]{ex.getMessage()});}return new JScrollPane(t);}
    private static class DefaultTableModelHolder { final javax.swing.table.DefaultTableModel model=new javax.swing.table.DefaultTableModel(){public boolean isCellEditable(int r,int c){return false;}}; }
}

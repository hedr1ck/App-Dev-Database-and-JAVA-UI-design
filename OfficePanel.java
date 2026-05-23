import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OfficePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public OfficePanel() {

        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Office Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(45, 45, 45));

        add(title, BorderLayout.NORTH);

        String[] columns = {
                "Office ID",
                "Office Title",
                "Country",
                "Phone",
                "Contact"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(235, 235, 235));
        table.setSelectionBackground(new Color(255, 220, 220));
        table.setSelectionForeground(new Color(120, 0, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        loadOffices();
    }

    private void loadOffices() {

        model.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query =
                    """
                    SELECT
                        offices.ID,
                        offices.Title,
                        countries.Name AS CountryName,
                        offices.Phone,
                        offices.Contact
                    FROM offices
                    INNER JOIN countries
                        ON offices.CountryID = countries.ID
                    ORDER BY offices.Title
                    """;

            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(
                        new Object[]{
                                rs.getInt("ID"),
                                rs.getString("Title"),
                                rs.getString("CountryName"),
                                rs.getString("Phone"),
                                rs.getString("Contact")
                        }
                );
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading offices."
            );
        }
    }
}
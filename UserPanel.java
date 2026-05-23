import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public UserPanel() {

        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("User Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(45, 45, 45));

        add(title, BorderLayout.NORTH);

        String[] columns = {
                "User ID",
                "Name",
                "Email",
                "Role",
                "Office",
                "Birthdate",
                "Status"
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

        loadUsers();
    }

    private void loadUsers() {

        model.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query =
                    """
                    SELECT
                        users.ID,
                        CONCAT(users.FirstName, ' ', users.LastName) AS FullName,
                        users.Email,
                        roles.Title AS RoleTitle,
                        offices.Title AS OfficeTitle,
                        users.Birthdate,
                        users.Active
                    FROM users
                    INNER JOIN roles
                        ON users.RoleID = roles.ID
                    LEFT JOIN offices
                        ON users.OfficeID = offices.ID
                    ORDER BY users.LastName, users.FirstName
                    """;

            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(
                        new Object[]{
                                rs.getInt("ID"),
                                rs.getString("FullName"),
                                rs.getString("Email"),
                                rs.getString("RoleTitle"),
                                rs.getString("OfficeTitle"),
                                rs.getString("Birthdate"),
                                rs.getBoolean("Active") ? "Active" : "Inactive"
                        }
                );
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading users."
            );
        }
    }
}
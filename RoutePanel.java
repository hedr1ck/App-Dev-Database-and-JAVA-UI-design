import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RoutePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public RoutePanel() {

        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Route Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(45, 45, 45));

        add(title, BorderLayout.NORTH);

        String[] columns = {
                "Route ID",
                "Departure Airport",
                "Arrival Airport",
                "Route"
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

        loadRoutes();
    }

    private void loadRoutes() {

        model.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query =
                    """
                    SELECT
                        routes.ID,
                        dep.Name AS DepartureAirport,
                        arr.Name AS ArrivalAirport,
                        dep.IATACode AS DepartureCode,
                        arr.IATACode AS ArrivalCode
                    FROM routes
                    INNER JOIN airports dep
                        ON routes.DepartureAirportID = dep.ID
                    INNER JOIN airports arr
                        ON routes.ArrivalAirportID = arr.ID
                    ORDER BY dep.IATACode, arr.IATACode
                    """;

            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(
                        new Object[]{
                                rs.getInt("ID"),
                                rs.getString("DepartureAirport"),
                                rs.getString("ArrivalAirport"),
                                rs.getString("DepartureCode") + " → " + rs.getString("ArrivalCode")
                        }
                );
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading routes."
            );
        }
    }
}
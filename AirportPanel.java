import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AirportPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public AirportPanel() {

        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Airport Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(45, 45, 45));

        add(title, BorderLayout.NORTH);

        String[] columns = {
                "Airport ID",
                "IATA Code",
                "Airport Name",
                "Country"
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

        loadAirports();
    }

    private void loadAirports() {

        model.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query =
                    """
                    SELECT
                        airports.ID,
                        airports.IATACode,
                        airports.Name AS AirportName,
                        countries.Name AS CountryName
                    FROM airports
                    INNER JOIN countries
                        ON airports.CountryID = countries.ID
                    ORDER BY airports.IATACode
                    """;

            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(
                        new Object[]{
                                rs.getInt("ID"),
                                rs.getString("IATACode"),
                                rs.getString("AirportName"),
                                rs.getString("CountryName")
                        }
                );
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading airports."
            );
        }
    }
}
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SchedulePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField flightField, dateField;
    private JComboBox<String> departureBox, arrivalBox, sortBox;
    private int roleID;

    public SchedulePanel() {
        this(1);
    }

    public SchedulePanel(int roleID) {
        this.roleID = roleID;

        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Flight Schedule Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JPanel filterPanel = new JPanel(new GridLayout(2, 5, 15, 15));
        filterPanel.setOpaque(false);

        departureBox = new JComboBox<>();
        arrivalBox = new JComboBox<>();
        departureBox.addItem("All Departure Airports");
        arrivalBox.addItem("All Arrival Airports");
        loadAirports();

        flightField = new JTextField();
        flightField.setBorder(BorderFactory.createTitledBorder("Flight Number"));

        dateField = new JTextField();
        dateField.setBorder(BorderFactory.createTitledBorder("Date (YYYY-MM-DD)"));

        sortBox = new JComboBox<>(new String[]{"Date and Time", "Economy Price", "Confirmed"});

        Color buttonColor = new Color(180, 0, 0);

        JButton searchButton = createButton("Search", buttonColor);
        JButton refreshButton = createButton("Refresh", buttonColor);
        JButton addButton = createButton("Add Flight", buttonColor);
        JButton editButton = createButton("Edit Flight", buttonColor);
        JButton cancelButton = createButton("Cancel Flight", buttonColor);

        filterPanel.add(departureBox);
        filterPanel.add(arrivalBox);
        filterPanel.add(flightField);
        filterPanel.add(dateField);
        filterPanel.add(sortBox);
        filterPanel.add(searchButton);
        filterPanel.add(refreshButton);

        if (roleID == 1) {
            filterPanel.add(addButton);
            filterPanel.add(editButton);
            filterPanel.add(cancelButton);
        } else {
            JLabel staffMode = new JLabel("STAFF VIEW MODE", SwingConstants.CENTER);
            staffMode.setFont(new Font("Segoe UI", Font.BOLD, 13));
            staffMode.setForeground(new Color(150, 0, 0));

            filterPanel.add(staffMode);
            filterPanel.add(new JLabel(""));
            filterPanel.add(new JLabel(""));
        }

        JPanel north = new JPanel(new BorderLayout(0, 20));
        north.setOpaque(false);
        north.add(title, BorderLayout.NORTH);
        north.add(filterPanel, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        String[] columns = {
                "Schedule ID", "Date", "Time", "Departure", "Arrival",
                "Flight No.", "Aircraft", "Economy", "Business", "First Class", "Status"
        };

        model = new DefaultTableModel(columns, 0) {
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

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        loadSchedules("");

        searchButton.addActionListener(e -> performSearch());

        refreshButton.addActionListener(e -> {
            clearFilters();
            loadSchedules("");
        });

        if (roleID == 1) {
            addButton.addActionListener(e -> addFlight());
            editButton.addActionListener(e -> editSelectedFlight());
            cancelButton.addActionListener(e -> cancelSelectedFlight());
        }
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return button;
    }

    private void loadAirports() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement("SELECT Name FROM airports ORDER BY Name");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                departureBox.addItem(rs.getString("Name"));
                arrivalBox.addItem(rs.getString("Name"));
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JComboBox<ComboItem> loadRoutesCombo() {
        JComboBox<ComboItem> combo = new JComboBox<>();

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query = """
                    SELECT routes.ID, dep.IATACode AS DepartureCode, arr.IATACode AS ArrivalCode
                    FROM routes
                    INNER JOIN airports dep ON routes.DepartureAirportID = dep.ID
                    INNER JOIN airports arr ON routes.ArrivalAirportID = arr.ID
                    ORDER BY dep.IATACode, arr.IATACode
                    """;

            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("ID");
                String label = rs.getString("DepartureCode") + " → " + rs.getString("ArrivalCode");
                combo.addItem(new ComboItem(id, label));
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return combo;
    }

    private JComboBox<ComboItem> loadAircraftCombo() {
        JComboBox<ComboItem> combo = new JComboBox<>();

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT ID, Name FROM aircrafts ORDER BY Name";

            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                combo.addItem(new ComboItem(rs.getInt("ID"), rs.getString("Name")));
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return combo;
    }

    private void performSearch() {
        if (departureBox.getSelectedIndex() > 0 &&
                arrivalBox.getSelectedIndex() > 0 &&
                departureBox.getSelectedItem().toString().equals(arrivalBox.getSelectedItem().toString())) {

            JOptionPane.showMessageDialog(this, "Departure and arrival airport cannot be the same.");
            return;
        }

        StringBuilder query = new StringBuilder(baseQuery() + " WHERE 1=1 ");

        if (departureBox.getSelectedIndex() > 0) query.append(" AND dep.Name = ?");
        if (arrivalBox.getSelectedIndex() > 0) query.append(" AND arr.Name = ?");
        if (!flightField.getText().trim().isEmpty()) query.append(" AND schedules.FlightNumber LIKE ?");
        if (!dateField.getText().trim().isEmpty()) query.append(" AND schedules.Date = ?");

        switch (sortBox.getSelectedItem().toString()) {
            case "Economy Price" -> query.append(" ORDER BY schedules.EconomyPrice DESC");
            case "Confirmed" -> query.append(" ORDER BY schedules.Confirmed DESC");
            default -> query.append(" ORDER BY schedules.Date DESC, schedules.Time DESC");
        }

        loadSchedules(query.toString());
    }

    private String baseQuery() {
        return """
                SELECT schedules.ID, schedules.Date, schedules.Time,
                       dep.Name AS DepartureAirport,
                       arr.Name AS ArrivalAirport,
                       schedules.FlightNumber,
                       aircrafts.Name AS AircraftName,
                       schedules.EconomyPrice,
                       schedules.Confirmed
                FROM schedules
                INNER JOIN routes ON schedules.RouteID = routes.ID
                INNER JOIN airports dep ON routes.DepartureAirportID = dep.ID
                INNER JOIN airports arr ON routes.ArrivalAirportID = arr.ID
                INNER JOIN aircrafts ON schedules.AircraftID = aircrafts.ID
                """;
    }

    private void loadSchedules(String customQuery) {
        model.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query = customQuery.isEmpty()
                    ? baseQuery() + " ORDER BY schedules.Date DESC, schedules.Time DESC"
                    : customQuery;

            PreparedStatement pst = conn.prepareStatement(query);
            int index = 1;

            if (departureBox.getSelectedIndex() > 0) pst.setString(index++, departureBox.getSelectedItem().toString());
            if (arrivalBox.getSelectedIndex() > 0) pst.setString(index++, arrivalBox.getSelectedItem().toString());
            if (!flightField.getText().trim().isEmpty()) pst.setString(index++, "%" + flightField.getText().trim() + "%");
            if (!dateField.getText().trim().isEmpty()) pst.setString(index++, dateField.getText().trim());

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                double economy = rs.getDouble("EconomyPrice");
                double business = Math.floor(economy * 1.35);
                double firstClass = Math.floor(business * 1.30);

                model.addRow(new Object[]{
                        rs.getInt("ID"),
                        rs.getString("Date"),
                        rs.getString("Time"),
                        rs.getString("DepartureAirport"),
                        rs.getString("ArrivalAirport"),
                        rs.getString("FlightNumber"),
                        rs.getString("AircraftName"),
                        "₱" + economy,
                        "₱" + business,
                        "₱" + firstClass,
                        rs.getBoolean("Confirmed") ? "Confirmed" : "Cancelled"
                });
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading schedules.");
        }
    }

    private void addFlight() {
        JTextField dateInput = new JTextField();
        JTextField timeInput = new JTextField();
        JTextField flightInput = new JTextField();
        JTextField priceInput = new JTextField();

        dateInput.setBorder(BorderFactory.createTitledBorder("Date (YYYY-MM-DD)"));
        timeInput.setBorder(BorderFactory.createTitledBorder("Time (HH:MM:SS)"));
        flightInput.setBorder(BorderFactory.createTitledBorder("Flight Number"));
        priceInput.setBorder(BorderFactory.createTitledBorder("Economy Price"));

        JComboBox<ComboItem> routeInput = loadRoutesCombo();
        JComboBox<ComboItem> aircraftInput = loadAircraftCombo();
        JComboBox<String> statusInput = new JComboBox<>(new String[]{"Confirmed", "Cancelled"});

        JPanel panel = new JPanel(new GridLayout(7, 1, 10, 10));
        panel.add(dateInput);
        panel.add(timeInput);
        panel.add(flightInput);
        panel.add(priceInput);
        panel.add(routeInput);
        panel.add(aircraftInput);
        panel.add(statusInput);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Flight",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        if (dateInput.getText().trim().isEmpty()
                || timeInput.getText().trim().isEmpty()
                || flightInput.getText().trim().isEmpty()
                || priceInput.getText().trim().isEmpty()
                || routeInput.getSelectedItem() == null
                || aircraftInput.getSelectedItem() == null) {

            JOptionPane.showMessageDialog(this, "Please complete all fields.");
            return;
        }

        try {
            ComboItem selectedRoute = (ComboItem) routeInput.getSelectedItem();
            ComboItem selectedAircraft = (ComboItem) aircraftInput.getSelectedItem();

            Connection conn = DatabaseConnection.getConnection();

            String query = """
                    INSERT INTO schedules
                    (Date, Time, AircraftID, RouteID, EconomyPrice, Confirmed, FlightNumber)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, dateInput.getText().trim());
            pst.setString(2, timeInput.getText().trim());
            pst.setInt(3, selectedAircraft.getId());
            pst.setInt(4, selectedRoute.getId());
            pst.setDouble(5, Double.parseDouble(priceInput.getText().trim()));
            pst.setInt(6, statusInput.getSelectedItem().toString().equals("Confirmed") ? 1 : 0);
            pst.setString(7, flightInput.getText().trim());

            pst.executeUpdate();
            conn.close();

            JOptionPane.showMessageDialog(this, "Flight added successfully.");
            loadSchedules("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Economy price must be a valid number.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding flight.");
        }
    }

    private void editSelectedFlight() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a flight first.");
            return;
        }

        int scheduleID = Integer.parseInt(table.getValueAt(row, 0).toString());

        JTextField dateInput = new JTextField(table.getValueAt(row, 1).toString());
        JTextField timeInput = new JTextField(table.getValueAt(row, 2).toString());
        JTextField flightInput = new JTextField(table.getValueAt(row, 5).toString());
        JTextField priceInput = new JTextField(table.getValueAt(row, 7).toString().replace("₱", ""));
        JComboBox<String> statusInput = new JComboBox<>(new String[]{"Confirmed", "Cancelled"});
        statusInput.setSelectedItem(table.getValueAt(row, 10).toString());

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.add(new JLabel("Date:"));
        panel.add(dateInput);
        panel.add(new JLabel("Time:"));
        panel.add(timeInput);
        panel.add(new JLabel("Flight Number:"));
        panel.add(flightInput);
        panel.add(new JLabel("Economy Price:"));
        panel.add(priceInput);
        panel.add(new JLabel("Status:"));
        panel.add(statusInput);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Flight",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query = """
                    UPDATE schedules
                    SET Date = ?, Time = ?, FlightNumber = ?, EconomyPrice = ?, Confirmed = ?
                    WHERE ID = ?
                    """;

            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, dateInput.getText().trim());
            pst.setString(2, timeInput.getText().trim());
            pst.setString(3, flightInput.getText().trim());
            pst.setDouble(4, Double.parseDouble(priceInput.getText().trim()));
            pst.setInt(5, statusInput.getSelectedItem().toString().equals("Confirmed") ? 1 : 0);
            pst.setInt(6, scheduleID);

            pst.executeUpdate();
            conn.close();

            JOptionPane.showMessageDialog(this, "Flight updated successfully.");
            loadSchedules("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Economy price must be a valid number.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating flight.");
        }
    }

    private void cancelSelectedFlight() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a flight schedule first.");
            return;
        }

        if (table.getValueAt(row, 10).toString().equalsIgnoreCase("Cancelled")) {
            JOptionPane.showMessageDialog(this, "This flight is already cancelled.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel this flight?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        int scheduleID = Integer.parseInt(table.getValueAt(row, 0).toString());

        try {
            Connection conn = DatabaseConnection.getConnection();

            PreparedStatement pst = conn.prepareStatement(
                    "UPDATE schedules SET Confirmed = 0 WHERE ID = ?"
            );

            pst.setInt(1, scheduleID);
            pst.executeUpdate();
            conn.close();

            JOptionPane.showMessageDialog(this, "Flight cancelled successfully.");
            loadSchedules("");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error cancelling flight.");
        }
    }

    private void clearFilters() {
        departureBox.setSelectedIndex(0);
        arrivalBox.setSelectedIndex(0);
        flightField.setText("");
        dateField.setText("");
        sortBox.setSelectedIndex(0);
    }
}

class ComboItem {

    private final int id;
    private final String label;

    public ComboItem(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return label;
    }
}

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ATSDashboardUI extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private int roleID;

    public ATSDashboardUI() {
        this(1);
    }

    public ATSDashboardUI(int roleID) {
        this.roleID = roleID;

        setTitle("ATS Airline - Dashboard");
        setMinimumSize(new Dimension(1100, 650));
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DashboardBackground mainPanel = new DashboardBackground();
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));

        addPanels();

        JPanel sidebar = createSidebar();

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void addPanels() {
        contentPanel.add(createDashboardPanel(), "Dashboard");
        contentPanel.add(new SchedulePanel(roleID), "Schedules");
        contentPanel.add(new RoutePanel(), "Routes");
        contentPanel.add(new AirportPanel(), "Airports");
        contentPanel.add(new AircraftPanel(), "Aircrafts");

        if (roleID == 1) {
            contentPanel.add(new UserPanel(), "Users");
            contentPanel.add(new OfficePanel(), "Offices");
        }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 700));
        sidebar.setBackground(new Color(150, 0, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(35, 25, 25, 25));

        JLabel logo = new JLabel("ATS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 42));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLogo = new JLabel("AIRLINE");
        subLogo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subLogo.setForeground(new Color(255, 220, 220));
        subLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(subLogo);
        sidebar.add(Box.createVerticalStrut(45));

        SidebarButton dashboardBtn = new SidebarButton("Dashboard");
        SidebarButton schedulesBtn = new SidebarButton("Schedules");
        SidebarButton routesBtn = new SidebarButton("Routes");
        SidebarButton airportsBtn = new SidebarButton("Airports");
        SidebarButton aircraftsBtn = new SidebarButton("Aircrafts");
        SidebarButton usersBtn = new SidebarButton("Users");
        SidebarButton officesBtn = new SidebarButton("Offices");
        SidebarButton logoutBtn = new SidebarButton("Logout");

        dashboardBtn.addActionListener(e -> refreshDashboard());
        schedulesBtn.addActionListener(e -> cardLayout.show(contentPanel, "Schedules"));
        routesBtn.addActionListener(e -> cardLayout.show(contentPanel, "Routes"));
        airportsBtn.addActionListener(e -> cardLayout.show(contentPanel, "Airports"));
        aircraftsBtn.addActionListener(e -> cardLayout.show(contentPanel, "Aircrafts"));

        if (roleID == 1) {
            usersBtn.addActionListener(e -> cardLayout.show(contentPanel, "Users"));
            officesBtn.addActionListener(e -> cardLayout.show(contentPanel, "Offices"));
        }

        logoutBtn.addActionListener(e -> {
            new ATSLoginUI().setVisible(true);
            this.dispose();
        });

        sidebar.add(dashboardBtn);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(schedulesBtn);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(routesBtn);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(airportsBtn);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(aircraftsBtn);

        if (roleID == 1) {
            sidebar.add(Box.createVerticalStrut(12));
            sidebar.add(usersBtn);
            sidebar.add(Box.createVerticalStrut(12));
            sidebar.add(officesBtn);
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private void refreshDashboard() {
        contentPanel.removeAll();

        addPanels();

        cardLayout.show(contentPanel, "Dashboard");

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createDashboardPanel() {
        JPanel content = new JPanel(new BorderLayout(0, 25));
        content.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 34));
        pageTitle.setForeground(new Color(45, 45, 45));

        JLabel userLabel = new JLabel(roleID == 1 ? "Administrator" : "Staff");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(new Color(150, 0, 0));

        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(userLabel, BorderLayout.EAST);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsPanel.setOpaque(false);

        cardsPanel.add(new StatCard("Total Flights", getTotalCount("schedules")));
        cardsPanel.add(new StatCard("Confirmed Flights", getConditionalCount("schedules", "Confirmed", "1")));
        cardsPanel.add(new StatCard("Cancelled Flights", getConditionalCount("schedules", "Confirmed", "0")));
        cardsPanel.add(new StatCard("Total Aircrafts", getTotalCount("aircrafts")));

        JPanel tableCard = new GlassPanel();
        tableCard.setLayout(new BorderLayout(0, 15));
        tableCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel tableTop = new JPanel(new BorderLayout());
        tableTop.setOpaque(false);

        JLabel tableTitle = new JLabel("Recent Flight Schedules");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        tableTitle.setForeground(new Color(45, 45, 45));

        tableTop.add(tableTitle, BorderLayout.WEST);

        JTable table = createScheduleTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(tableTop, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 25));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(tableCard, BorderLayout.CENTER);

        content.add(topBar, BorderLayout.NORTH);
        content.add(centerPanel, BorderLayout.CENTER);

        return content;
    }

    private JTable createScheduleTable() {
        String[] columns = {
                "Flight No.",
                "Date",
                "Time",
                "Route",
                "Aircraft",
                "Economy Price",
                "Status"
        };

        DefaultTableModel dashboardModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            java.sql.Connection conn = DatabaseConnection.getConnection();

            String query =
                    """
                    SELECT
                        schedules.FlightNumber,
                        schedules.Date,
                        schedules.Time,
                        dep.IATACode AS DepartureCode,
                        arr.IATACode AS ArrivalCode,
                        aircrafts.Name AS AircraftName,
                        schedules.EconomyPrice,
                        schedules.Confirmed
                    FROM schedules
                    INNER JOIN routes
                        ON schedules.RouteID = routes.ID
                    INNER JOIN airports dep
                        ON routes.DepartureAirportID = dep.ID
                    INNER JOIN airports arr
                        ON routes.ArrivalAirportID = arr.ID
                    INNER JOIN aircrafts
                        ON schedules.AircraftID = aircrafts.ID
                    ORDER BY schedules.Date DESC,
                             schedules.Time DESC
                    LIMIT 8
                    """;

            java.sql.PreparedStatement pst = conn.prepareStatement(query);
            java.sql.ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                dashboardModel.addRow(
                        new Object[]{
                                rs.getString("FlightNumber"),
                                rs.getString("Date"),
                                rs.getString("Time"),
                                rs.getString("DepartureCode") + " → " + rs.getString("ArrivalCode"),
                                rs.getString("AircraftName"),
                                "₱" + rs.getDouble("EconomyPrice"),
                                rs.getBoolean("Confirmed") ? "Confirmed" : "Cancelled"
                        }
                );
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading dashboard schedules."
            );
        }

        JTable table = new JTable(dashboardModel);

        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(235, 235, 235));
        table.getTableHeader().setForeground(new Color(80, 80, 80));
        table.setGridColor(new Color(235, 235, 235));
        table.setSelectionBackground(new Color(255, 220, 220));
        table.setSelectionForeground(new Color(120, 0, 0));

        return table;
    }

    private String getTotalCount(String tableName) {
        try {
            java.sql.Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT COUNT(*) AS total FROM " + tableName;

            java.sql.PreparedStatement pst = conn.prepareStatement(query);
            java.sql.ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String total = rs.getString("total");
                conn.close();
                return total;
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "0";
    }

    private String getConditionalCount(String tableName, String column, String value) {
        try {
            java.sql.Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT COUNT(*) AS total FROM "
                    + tableName
                    + " WHERE "
                    + column
                    + " = ?";

            java.sql.PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, value);

            java.sql.ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String total = rs.getString("total");
                conn.close();
                return total;
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "0";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ATSDashboardUI(1).setVisible(true));
    }
}

class DashboardBackground extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 245, 245),
                getWidth(), getHeight(), new Color(255, 225, 225)
        );

        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(190, 0, 0, 25));
        g2.fillOval(getWidth() - 240, -90, 340, 340);
        g2.fillOval(250, getHeight() - 180, 320, 320);

        g2.dispose();
    }
}

class SidebarButton extends JButton {

    public SidebarButton(String text) {
        super(text);

        setMaximumSize(new Dimension(190, 45));
        setPreferredSize(new Dimension(190, 45));
        setMinimumSize(new Dimension(190, 45));

        setHorizontalAlignment(SwingConstants.LEFT);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isRollover()) {
            g2.setColor(new Color(255, 255, 255, 45));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        }

        super.paintComponent(g);
        g2.dispose();
    }
}

class StatCard extends JPanel {

    private final String title;
    private final String value;

    public StatCard(String title, String value) {
        this.title = title;
        this.value = value;
        setOpaque(false);
        setPreferredSize(new Dimension(180, 120));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillRoundRect(10, 12, getWidth() - 20, getHeight() - 20, 28, 28);

        g2.setColor(new Color(255, 255, 255, 240));
        g2.fillRoundRect(0, 0, getWidth() - 20, getHeight() - 20, 28, 28);

        g2.setColor(new Color(230, 0, 0));
        g2.fillRoundRect(0, 0, 8, getHeight() - 20, 20, 20);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2.setColor(new Color(120, 120, 120));
        g2.drawString(title, 28, 38);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
        g2.setColor(new Color(40, 40, 40));
        g2.drawString(value, 28, 82);

        g2.dispose();
    }
}

class GlassPanel extends JPanel {

    public GlassPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillRoundRect(12, 14, getWidth() - 24, getHeight() - 24, 30, 30);

        g2.setColor(new Color(255, 255, 255, 238));
        g2.fillRoundRect(0, 0, getWidth() - 24, getHeight() - 24, 30, 30);

        g2.dispose();

        super.paintComponent(g);
    }
}

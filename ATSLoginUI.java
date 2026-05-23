import javax.swing.*;
import java.awt.*;

public class ATSLoginUI extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;

    public ATSLoginUI() {
        setTitle("ATS Airline");
        setMinimumSize(new Dimension(1000, 600));
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new GridBagLayout());
        add(mainPanel);

        JPanel container = new JPanel(new GridLayout(1, 2, 60, 0));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(55, 65, 55, 65));

        JPanel leftPanel = createLeftPanel();
        JPanel loginCard = createLoginCard();

        container.add(leftPanel);
        container.add(loginCard);

        mainPanel.add(container);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ATS AIRLINE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Airline Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 21));
        subtitle.setForeground(new Color(255, 235, 235));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("<html><body style='width:380px'>"
                + "A modern airline management platform for schedules, routes, users, airports, and aircraft operations."
                + "</body></html>");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        description.setForeground(Color.WHITE);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel featureGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        featureGrid.setOpaque(false);
        featureGrid.setMaximumSize(new Dimension(420, 140));
        featureGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        featureGrid.add(new FeatureCard("FAST PERFORMANCE"));
        featureGrid.add(new FeatureCard("SMART CONTROL"));
        featureGrid.add(new FeatureCard("REALTIME SYSTEM"));
        featureGrid.add(new FeatureCard("ATS AIRLINE"));

        leftPanel.add(Box.createVerticalStrut(45));
        leftPanel.add(title);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(subtitle);
        leftPanel.add(Box.createVerticalStrut(40));
        leftPanel.add(description);
        leftPanel.add(Box.createVerticalStrut(65));
        leftPanel.add(featureGrid);

        return leftPanel;
    }

    private JPanel createLoginCard() {
        GlassCard loginCard = new GlassCard();
        loginCard.setLayout(new GridBagLayout());
        loginCard.setPreferredSize(new Dimension(360, 440));
        loginCard.setMaximumSize(new Dimension(400, 460));
        loginCard.setMinimumSize(new Dimension(340, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 35, 8, 35);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel welcome = new JLabel("Welcome Back!", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 30));
        welcome.setForeground(new Color(35, 35, 35));

        JLabel instruction = new JLabel("Login to continue to ATS Airline", SwingConstants.CENTER);
        instruction.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        instruction.setForeground(new Color(120, 120, 120));

        emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createTitledBorder("Email"));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton loginButton = new JButton("LOGIN");
        loginButton.setBackground(new Color(220, 0, 0));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setPreferredSize(new Dimension(260, 50));

        JLabel reminder = new JLabel("<html><center>Friendly reminder:<br>Please complete all required fields.</center></html>", SwingConstants.CENTER);
        reminder.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reminder.setForeground(new Color(120, 120, 120));

        gbc.gridy = 0;
        loginCard.add(welcome, gbc);

        gbc.gridy = 1;
        loginCard.add(instruction, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(35, 35, 8, 35);
        loginCard.add(emailField, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(12, 35, 8, 35);
        loginCard.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(30, 35, 18, 35);
        loginCard.add(loginButton, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(8, 35, 8, 35);
        loginCard.add(reminder, gbc);

        loginButton.addActionListener(e -> loginAction());

        return loginCard;
    }

    private void loginAction() {

        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Friendly reminder: Please complete all required fields.",
                    "ATS Airline",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            java.sql.Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT * FROM users WHERE Email = ? AND Password = ?";

            java.sql.PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, email);
            pst.setString(2, password);

            java.sql.ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                new ATSDashboardUI().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid email or password.",
                        "ATS Airline",
                        JOptionPane.ERROR_MESSAGE
                );
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Database connection error.",
                    "ATS Airline",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ATSLoginUI().setVisible(true));
    }
}

class BackgroundPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(175, 0, 0),
                getWidth(), getHeight(), new Color(255, 235, 235)
        );

        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(255, 255, 255, 35));
        g2.fillOval(-120, -110, 370, 370);
        g2.fillOval(getWidth() - 260, -90, 350, 350);
        g2.fillOval(getWidth() / 2 - 120, getHeight() - 180, 300, 300);

        g2.dispose();
    }
}

class FeatureCard extends JPanel {

    private final String text;

    public FeatureCard(String text) {
        this.text = text;
        setOpaque(false);
        setPreferredSize(new Dimension(180, 55));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillRoundRect(8, 9, getWidth() - 16, getHeight() - 16, 24, 24);

        g2.setColor(new Color(255, 255, 255, 225));
        g2.fillRoundRect(0, 0, getWidth() - 14, getHeight() - 14, 24, 24);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(185, 0, 0));

        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2 - 5;
        int y = (getHeight() + fm.getAscent()) / 2 - 10;

        g2.drawString(text, x, y);

        g2.dispose();
    }
}

class GlassCard extends JPanel {

    public GlassCard() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillRoundRect(14, 16, getWidth() - 28, getHeight() - 28, 35, 35);

        g2.setColor(new Color(255, 255, 255, 238));
        g2.fillRoundRect(0, 0, getWidth() - 24, getHeight() - 24, 35, 35);

        g2.setColor(new Color(255, 70, 70, 35));
        g2.fillRoundRect(0, 0, getWidth() - 24, 105, 35, 35);

        g2.dispose();

        super.paintComponent(g);
    }
}
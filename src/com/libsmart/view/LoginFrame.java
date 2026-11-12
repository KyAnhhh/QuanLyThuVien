package com.libsmart.view;

import com.libsmart.dao.NguoiDungDAO;
import com.libsmart.model.NguoiDung;
import com.libsmart.utils.UITheme;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JButton btnLogin, btnExit;
    private final NguoiDungDAO nguoiDungDAO;

    public LoginFrame() {
        nguoiDungDAO = new NguoiDungDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("LIB-SMART | Đăng nhập hệ thống");
        setSize(480, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panelMain = new JPanel(new BorderLayout(0, 16));
        panelMain.setBackground(UITheme.BG_APP);
        panelMain.setBorder(BorderFactory.createEmptyBorder(24, 30, 22, 30));

        // 1. HEADER
        JPanel panelHeader = new JPanel(new GridLayout(2, 1, 0, 4));
        panelHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("LIB-SMART", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(UITheme.PRIMARY);

        JLabel lblSub = new JLabel("Đăng nhập hệ thống quản lý thư viện", SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);

        panelHeader.add(lblTitle);
        panelHeader.add(lblSub);

        // 2. CARD FORM NHẬP LIỆU
        JPanel panelCard = new JPanel(new GridBagLayout());
        panelCard.setBackground(UITheme.BG_CARD);
        panelCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(18, 20, 14, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        lblUsername.setFont(UITheme.FONT_LABEL);
        lblUsername.setForeground(UITheme.TEXT_PRIMARY);

        txtUsername = UITheme.createTextField(true);
        txtUsername.setPreferredSize(new Dimension(220, 34));

        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(UITheme.FONT_LABEL);
        lblPassword.setForeground(UITheme.TEXT_PRIMARY);

        txtPassword = new JPasswordField();
        UITheme.styleTextField(txtPassword, true);
        txtPassword.setPreferredSize(new Dimension(220, 34));

        chkShowPassword = new JCheckBox("Hiện mật khẩu");
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setForeground(UITheme.TEXT_SECONDARY);
        chkShowPassword.setOpaque(false);
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));

        chkShowPassword.addActionListener(e -> {
            txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : '•');
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.32;
        panelCard.add(lblUsername, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.68;
        panelCard.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.32;
        panelCard.add(lblPassword, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.68;
        panelCard.add(txtPassword, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.68;
        panelCard.add(chkShowPassword, gbc);

        // 3. NÚT BẤM
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4));
        panelButtons.setOpaque(false);

        btnLogin = UITheme.createButton("Đăng Nhập", UITheme.PRIMARY, 140, 38);
        btnExit = UITheme.createButton("Thoát", UITheme.MUTED, 120, 38);

        panelButtons.add(btnLogin);
        panelButtons.add(btnExit);

        panelMain.add(panelHeader, BorderLayout.NORTH);
        panelMain.add(panelCard, BorderLayout.CENTER);
        panelMain.add(panelButtons, BorderLayout.SOUTH);

        add(panelMain);
        getRootPane().setDefaultButton(btnLogin);

        btnLogin.addActionListener(e -> handleLogin());
        btnExit.addActionListener(e -> System.exit(0));
        txtPassword.addActionListener(e -> handleLogin());

        SwingUtilities.invokeLater(() -> txtUsername.requestFocusInWindow());
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            NguoiDung nd = nguoiDungDAO.checkLogin(username, password);
            if (nd != null) {
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Chào mừng " + nd.getHoTen());
                this.dispose(); // Đóng LoginFrame
                new MainFrame(nd).setVisible(true); // Mở MainFrame kèm phiên đăng nhập
            } else {
                JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không chính xác!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối cơ sở dữ liệu:\n" + ex.getMessage(),
                    "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        new LoginFrame().setVisible(true);
    }
}
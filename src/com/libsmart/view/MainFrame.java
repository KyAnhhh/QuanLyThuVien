package com.libsmart.view;

import com.libsmart.model.NguoiDung;
import com.libsmart.utils.UITheme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Cửa sổ chính Single-Page Application (Sidebar cố định + nội dung CardLayout bên phải).
 */
public class MainFrame extends JFrame {

    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_SACH = "SACH";
    private static final String CARD_DOC_GIA = "DOC_GIA";
    private static final String CARD_MUON_TRA = "MUON_TRA";
    private static final String CARD_THONG_KE = "THONG_KE";
    private static final String CARD_TAI_KHOAN = "TAI_KHOAN";

    private final NguoiDung currentUser;
    private CardLayout cardLayout;
    private JPanel panelCardHost;

    private final Map<String, RefreshableView> viewMap = new LinkedHashMap<>();
    private final List<JButton> navButtons = new ArrayList<>();
    private JButton btnDashboard;

    public MainFrame() {
        this(null);
    }

    public MainFrame(NguoiDung currentUser) {
        this.currentUser = currentUser;
        initComponents();
        navigateTo(CARD_DASHBOARD, btnDashboard);
    }

    private void initComponents() {
        setTitle("LIB-SMART | Hệ thống quản lý thư viện");
        applyResponsiveWindowSize();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. SIDEBAR BÊN TRÁI =================
        JPanel panelSidebar = new JPanel(new BorderLayout());
        panelSidebar.setPreferredSize(new Dimension(240, 680));
        panelSidebar.setBackground(UITheme.BG_SIDEBAR);

        JLabel lblSidebarTitle = new JLabel("LIB-SMART", SwingConstants.CENTER);
        lblSidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblSidebarTitle.setForeground(new Color(96, 165, 250));
        lblSidebarTitle.setBorder(BorderFactory.createEmptyBorder(24, 12, 4, 12));

        JLabel lblSubTitle = new JLabel("Library Management", SwingConstants.CENTER);
        lblSubTitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSubTitle.setForeground(new Color(148, 163, 184));

        JPanel panelHeader = new JPanel(new GridLayout(2, 1));
        panelHeader.setOpaque(false);
        panelHeader.add(lblSidebarTitle);
        panelHeader.add(lblSubTitle);
        panelSidebar.add(panelHeader, BorderLayout.NORTH);

        JPanel panelMenuButtons = new JPanel(new GridLayout(9, 1, 0, 6));
        panelMenuButtons.setOpaque(false);
        panelMenuButtons.setBorder(BorderFactory.createEmptyBorder(16, 14, 12, 14));

        btnDashboard = registerNavButton("Tổng Quan", CARD_DASHBOARD);
        JButton btnSach = registerNavButton("Quản Lý Sách", CARD_SACH);
        JButton btnDocGia = registerNavButton("Quản Lý Độc Giả", CARD_DOC_GIA);
        JButton btnMuonTra = registerNavButton("Mượn - Trả Sách", CARD_MUON_TRA);
        JButton btnThongKe = registerNavButton("Thống Kê - Báo Cáo", CARD_THONG_KE);

        panelMenuButtons.add(btnDashboard);
        panelMenuButtons.add(btnSach);
        panelMenuButtons.add(btnDocGia);
        panelMenuButtons.add(btnMuonTra);
        panelMenuButtons.add(btnThongKe);

        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getVaiTro());
        if (isAdmin) {
            JButton btnTaiKhoan = registerNavButton("Quản Lý Tài Khoản", CARD_TAI_KHOAN);
            panelMenuButtons.add(btnTaiKhoan);
        }

        if (currentUser != null) {
            JButton btnDoiMatKhau = UITheme.createSidebarButton(
                    "Đổi Mật Khẩu", UITheme.BG_SIDEBAR_BTN, UITheme.BG_SIDEBAR_BTN_HOVER);
            btnDoiMatKhau.addActionListener(e -> new DoiMatKhauDialog(this, currentUser).setVisible(true));
            panelMenuButtons.add(btnDoiMatKhau);
        }

        panelMenuButtons.add(new JLabel()); // khoảng đệm trước nút đăng xuất

        JButton btnDangXuat = UITheme.createSidebarButton("Đăng Xuất", UITheme.DANGER, new Color(185, 28, 28));
        btnDangXuat.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginFrame().setVisible(true);
            }
        });
        panelMenuButtons.add(btnDangXuat);

        panelSidebar.add(panelMenuButtons, BorderLayout.CENTER);

        // Thẻ phiên đăng nhập ở cuối Sidebar
        String tenThuThu = (currentUser != null) ? currentUser.getHoTen() : "Chưa đăng nhập";
        String vaiTro = (currentUser != null) ? currentUser.getVaiTro() : "GUEST";
        JPanel panelSession = new JPanel(new GridLayout(2, 1, 0, 2));
        panelSession.setBackground(new Color(15, 23, 42));
        panelSession.setBorder(BorderFactory.createEmptyBorder(10, 16, 12, 16));

        JLabel lblUser = new JLabel("Thủ thư: " + tenThuThu);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel("Vai trò: " + vaiTro);
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(new Color(148, 163, 184));

        panelSession.add(lblUser);
        panelSession.add(lblRole);
        panelSidebar.add(panelSession, BorderLayout.SOUTH);

        // ================= 2. VÙNG NỘI DUNG ĐỘNG BÊN PHẢI (CARDLAYOUT) =================
        JPanel panelRightShell = new JPanel(new BorderLayout());
        panelRightShell.setBackground(UITheme.BG_APP);

        cardLayout = new CardLayout();
        panelCardHost = new JPanel(cardLayout);
        panelCardHost.setBackground(UITheme.BG_APP);

        registerView(CARD_DASHBOARD, new DashboardPanel());
        registerView(CARD_SACH, new QuanLySachPanel());
        registerView(CARD_DOC_GIA, new QuanLyDocGiaPanel());
        registerView(CARD_MUON_TRA, new QuanLyMuonTraPanel(currentUser));
        registerView(CARD_THONG_KE, new ThongKePanel());
        if (isAdmin) {
            registerView(CARD_TAI_KHOAN, new QuanLyTaiKhoanPanel(currentUser));
        }

        panelRightShell.add(panelCardHost, BorderLayout.CENTER);

        String userInfo = (currentUser != null)
                ? "Phiên làm việc: " + currentUser.getHoTen() + " (" + currentUser.getVaiTro() + ") | "
                : "";
        JLabel lblFooter = new JLabel(userInfo + "Lib-Smart v1.0 | Hệ thống Quản lý Thư viện", SwingConstants.RIGHT);
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFooter.setForeground(UITheme.TEXT_SECONDARY);
        lblFooter.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 20));
        panelRightShell.add(lblFooter, BorderLayout.SOUTH);

        add(panelSidebar, BorderLayout.WEST);
        add(panelRightShell, BorderLayout.CENTER);
    }

    private void applyResponsiveWindowSize() {
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(1160, Math.max(800, maxBounds.width - 16));
        int height = Math.min(680, Math.max(540, maxBounds.height - 16));
        setSize(width, height);
        setMinimumSize(new Dimension(Math.min(960, width), Math.min(580, height)));
    }

    private JButton registerNavButton(String title, String cardKey) {
        JButton btn = UITheme.createSidebarButton(title, UITheme.BG_SIDEBAR_BTN, UITheme.BG_SIDEBAR_BTN_HOVER);
        btn.addActionListener(e -> navigateTo(cardKey, btn));
        navButtons.add(btn);
        return btn;
    }

    private void registerView(String cardKey, JPanel panel) {
        panelCardHost.add(panel, cardKey);
        if (panel instanceof RefreshableView) {
            viewMap.put(cardKey, (RefreshableView) panel);
        }
    }

    private void navigateTo(String cardKey, JButton activeButton) {
        cardLayout.show(panelCardHost, cardKey);
        for (JButton btn : navButtons) {
            UITheme.setSidebarButtonActive(btn, btn == activeButton);
        }
        RefreshableView view = viewMap.get(cardKey);
        if (view != null) {
            view.refreshData();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

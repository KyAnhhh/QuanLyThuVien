package com.libsmart.view;

import com.libsmart.dao.DocGiaDAO;
import com.libsmart.dao.MuonTraDAO;
import com.libsmart.dao.SachDAO;
import com.libsmart.model.NguoiDung;
import com.libsmart.model.Sach;
import com.libsmart.utils.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {

    private JButton btnSach, btnDocGia, btnMuonTra, btnLamMoi, btnDangXuat;
    private JLabel lblTongSachValue, lblDocGiaValue, lblDangMuonValue;
    private DefaultTableModel tableOverviewModel;
    private final SachDAO sachDAO;
    private final DocGiaDAO docGiaDAO;
    private final MuonTraDAO muonTraDAO;
    private final NguoiDung currentUser;

    public MainFrame() {
        this(null);
    }

    public MainFrame(NguoiDung currentUser) {
        this.currentUser = currentUser;
        this.sachDAO = new SachDAO();
        this.docGiaDAO = new DocGiaDAO();
        this.muonTraDAO = new MuonTraDAO();
        initComponents();
        refreshDashboard();
    }

    private void initComponents() {
        setTitle("LIB-SMART | Bảng điều khiển hệ thống");
        setSize(1120, 700);
        setMinimumSize(new Dimension(960, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= 1. SIDEBAR BÊN TRÁI =================
        JPanel panelSidebar = new JPanel(new BorderLayout());
        panelSidebar.setPreferredSize(new Dimension(250, 700));
        panelSidebar.setBackground(UITheme.BG_SIDEBAR);

        JLabel lblSidebarTitle = new JLabel("LIB-SMART", SwingConstants.CENTER);
        lblSidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblSidebarTitle.setForeground(new Color(96, 165, 250));
        lblSidebarTitle.setBorder(BorderFactory.createEmptyBorder(28, 12, 4, 12));

        JLabel lblSubTitle = new JLabel("Library Management", SwingConstants.CENTER);
        lblSubTitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSubTitle.setForeground(new Color(148, 163, 184));

        JPanel panelHeader = new JPanel(new GridLayout(2, 1));
        panelHeader.setOpaque(false);
        panelHeader.add(lblSidebarTitle);
        panelHeader.add(lblSubTitle);
        panelSidebar.add(panelHeader, BorderLayout.NORTH);

        JPanel panelMenuButtons = new JPanel(new GridLayout(6, 1, 0, 12));
        panelMenuButtons.setOpaque(false);
        panelMenuButtons.setBorder(BorderFactory.createEmptyBorder(26, 16, 20, 16));

        btnSach = UITheme.createSidebarButton("Quản Lý Sách", UITheme.BG_SIDEBAR_BTN, UITheme.BG_SIDEBAR_BTN_HOVER);
        btnDocGia = UITheme.createSidebarButton("Quản Lý Độc Giả", UITheme.BG_SIDEBAR_BTN, UITheme.BG_SIDEBAR_BTN_HOVER);
        btnMuonTra = UITheme.createSidebarButton("Mượn - Trả Sách", UITheme.BG_SIDEBAR_BTN, UITheme.BG_SIDEBAR_BTN_HOVER);
        btnDangXuat = UITheme.createSidebarButton("Đăng Xuất", UITheme.DANGER, new Color(185, 28, 28));

        panelMenuButtons.add(btnSach);
        panelMenuButtons.add(btnDocGia);
        panelMenuButtons.add(btnMuonTra);
        panelMenuButtons.add(new JLabel()); // khoảng đệm trước nút đăng xuất
        panelMenuButtons.add(btnDangXuat);

        panelSidebar.add(panelMenuButtons, BorderLayout.CENTER);

        // Thẻ phiên đăng nhập ở cuối Sidebar
        String tenThuThu = (currentUser != null) ? currentUser.getHoTen() : "Chưa đăng nhập";
        String vaiTro = (currentUser != null) ? currentUser.getVaiTro() : "GUEST";
        JPanel panelSession = new JPanel(new GridLayout(2, 1, 0, 2));
        panelSession.setBackground(new Color(15, 23, 42));
        panelSession.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));

        JLabel lblUser = new JLabel("Thủ thư: " + tenThuThu);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel("Vai trò: " + vaiTro);
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(new Color(148, 163, 184));

        panelSession.add(lblUser);
        panelSession.add(lblRole);
        panelSidebar.add(panelSession, BorderLayout.SOUTH);

        // ================= 2. NỘI DUNG CHÍNH BÊN PHẢI =================
        JPanel panelMainContent = new JPanel(new BorderLayout(16, 16));
        panelMainContent.setBackground(UITheme.BG_APP);
        panelMainContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JPanel panelTopHeader = new JPanel(new BorderLayout(10, 14));
        panelTopHeader.setOpaque(false);

        JPanel panelTitleRow = new JPanel(new BorderLayout());
        panelTitleRow.setOpaque(false);

        JLabel lblWelcome = new JLabel("BẢNG ĐIỀU KHIỂN HỆ THỐNG");
        lblWelcome.setFont(UITheme.FONT_TITLE);
        lblWelcome.setForeground(UITheme.TEXT_PRIMARY);

        btnLamMoi = UITheme.createButton("Làm Mới Số Liệu", UITheme.PRIMARY, 155, 34);
        panelTitleRow.add(lblWelcome, BorderLayout.WEST);
        panelTitleRow.add(btnLamMoi, BorderLayout.EAST);

        JPanel panelCards = new JPanel(new GridLayout(1, 3, 16, 0));
        panelCards.setOpaque(false);

        lblTongSachValue = createCardValueLabel(UITheme.PRIMARY);
        lblDocGiaValue = createCardValueLabel(UITheme.SUCCESS);
        lblDangMuonValue = createCardValueLabel(UITheme.WARNING);

        panelCards.add(createStatCard("TỔNG SỐ SÁCH", lblTongSachValue, UITheme.PRIMARY));
        panelCards.add(createStatCard("ĐỘC GIẢ TÍCH CỰC", lblDocGiaValue, UITheme.SUCCESS));
        panelCards.add(createStatCard("ĐANG CHO MƯỢN", lblDangMuonValue, UITheme.WARNING));

        panelTopHeader.add(panelTitleRow, BorderLayout.NORTH);
        panelTopHeader.add(panelCards, BorderLayout.CENTER);

        panelMainContent.add(panelTopHeader, BorderLayout.NORTH);

        JPanel panelTableOverview = UITheme.createTitledCard("DANH SÁCH SÁCH HIỆN CÓ TRONG HỆ THỐNG (TOP 10)");
        panelTableOverview.setLayout(new BorderLayout(5, 5));

        String[] columns = {"Mã Sách", "Tên Sách", "Năm XB", "Tổng Số", "Còn Lại", "Giá Bìa"};
        tableOverviewModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tableOverview = new JTable(tableOverviewModel);
        JScrollPane scrollPane = new JScrollPane(tableOverview);
        UITheme.styleTable(tableOverview, scrollPane, 0, 2, 3, 4);
        tableOverview.getColumnModel().getColumn(5).setCellRenderer(UITheme.createStripedRenderer(SwingConstants.RIGHT));
        tableOverview.getColumnModel().getColumn(0).setPreferredWidth(75);
        tableOverview.getColumnModel().getColumn(1).setPreferredWidth(300);
        tableOverview.getColumnModel().getColumn(2).setPreferredWidth(85);
        tableOverview.getColumnModel().getColumn(3).setPreferredWidth(85);
        tableOverview.getColumnModel().getColumn(4).setPreferredWidth(85);
        tableOverview.getColumnModel().getColumn(5).setPreferredWidth(120);

        panelTableOverview.add(scrollPane, BorderLayout.CENTER);
        panelMainContent.add(panelTableOverview, BorderLayout.CENTER);

        String userInfo = (currentUser != null)
                ? "Phiên làm việc: " + currentUser.getHoTen() + " (" + currentUser.getVaiTro() + ") | "
                : "";
        JLabel lblFooter = new JLabel(userInfo + "Lib-Smart v1.0 | Hệ thống Quản lý Thư viện", SwingConstants.RIGHT);
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFooter.setForeground(UITheme.TEXT_SECONDARY);
        panelMainContent.add(lblFooter, BorderLayout.SOUTH);

        add(panelSidebar, BorderLayout.WEST);
        add(panelMainContent, BorderLayout.CENTER);

        // ================= 3. SỰ KIỆN =================
        btnSach.addActionListener(e -> openChildFrame(new QuanLySachFrame(this::refreshDashboard)));
        btnDocGia.addActionListener(e -> openChildFrame(new QuanLyDocGiaFrame(this::refreshDashboard)));
        btnMuonTra.addActionListener(e -> openChildFrame(new QuanLyMuonTraFrame(currentUser, this::refreshDashboard)));
        btnLamMoi.addActionListener(e -> refreshDashboard());

        btnDangXuat.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginFrame().setVisible(true);
            }
        });
    }

    private void openChildFrame(JFrame childFrame) {
        childFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                refreshDashboard();
            }
        });
        childFrame.setVisible(true);
    }

    public void refreshDashboard() {
        List<Sach> listSach = Collections.emptyList();
        int tongSoSach = 0;
        try {
            listSach = sachDAO.getAllSach();
            tongSoSach = sachDAO.sumTongSoLuongSach();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải dữ liệu sách từ cơ sở dữ liệu:\n" + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }

        int docGiaTichCuc = docGiaDAO.countActiveDocGia();
        int dangChoMuon = muonTraDAO.countPhieuDangMuon();

        lblTongSachValue.setText(String.valueOf(tongSoSach));
        lblDocGiaValue.setText(String.valueOf(docGiaTichCuc));
        lblDangMuonValue.setText(String.valueOf(dangChoMuon));

        tableOverviewModel.setRowCount(0);
        for (int i = 0; i < Math.min(listSach.size(), 10); i++) {
            Sach s = listSach.get(i);
            tableOverviewModel.addRow(new Object[]{
                s.getMaSach(), s.getTenSach(), s.getNamXuatBan(),
                s.getSoLuongTong(), s.getSoLuongCon(), UITheme.formatCurrency(s.getGiaBia())
            });
        }
    }

    private JLabel createCardValueLabel(Color color) {
        JLabel lblValue = new JLabel("0");
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(color);
        lblValue.setBorder(BorderFactory.createEmptyBorder(0, 14, 10, 14));
        return lblValue;
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(210, 92));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createMatteBorder(0, 5, 0, 0, color)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(12, 14, 4, 14));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
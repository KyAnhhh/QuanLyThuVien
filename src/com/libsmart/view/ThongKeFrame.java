package com.libsmart.view;

import com.libsmart.dao.ThongKeDAO;
import com.libsmart.utils.CsvExporter;
import com.libsmart.utils.UITheme;
import com.libsmart.utils.ValidationUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình Thống kê & Báo cáo tổng hợp của hệ thống Lib-Smart.
 */
public class ThongKeFrame extends JFrame {

    private final ThongKeDAO thongKeDAO;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private DefaultTableModel modelQuaHan, modelTienPhat, modelTopSach;
    private JTable tableQuaHan, tableTienPhat, tableTopSach;
    private JLabel lblTongQuaHan, lblTongTienPhat;
    private JComboBox<String> cbNamPhat;

    public ThongKeFrame() {
        this.thongKeDAO = new ThongKeDAO();
        initComponents();
        loadAllReports();
    }

    private void initComponents() {
        setTitle("LIB-SMART | Thống kê & Báo cáo thư viện");
        setSize(1080, 680);
        setMinimumSize(new Dimension(920, 580));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelMain = new JPanel(new BorderLayout(12, 12));
        panelMain.setBackground(UITheme.BG_APP);
        panelMain.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("THỐNG KÊ & BÁO CÁO HOẠT ĐỘNG THƯ VIỆN", SwingConstants.LEFT);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);
        JButton btnRefreshAll = UITheme.createButton("Làm Mới Tất Cả", UITheme.PRIMARY, 150, 34);
        btnRefreshAll.addActionListener(e -> loadAllReports());
        panelHeader.add(lblTitle, BorderLayout.WEST);
        panelHeader.add(btnRefreshAll, BorderLayout.EAST);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_SECTION);
        tabbedPane.addTab("Phiếu Quá Hạn & Liên Hệ", buildOverdueTab());
        tabbedPane.addTab("Doanh Thu Tiền Phạt", buildFinesTab());
        tabbedPane.addTab("Top Sách Mượn Nhiều Nhất", buildTopBooksTab());

        panelMain.add(panelHeader, BorderLayout.NORTH);
        panelMain.add(tabbedPane, BorderLayout.CENTER);
        add(panelMain);
    }

    private JPanel buildOverdueTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_APP);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        lblTongQuaHan = new JLabel("Tổng số sách đang quá hạn: 0");
        lblTongQuaHan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongQuaHan.setForeground(UITheme.DANGER);

        JButton btnExport = UITheme.createButton("Xuất CSV", UITheme.SUCCESS, 120, 32);
        top.add(lblTongQuaHan, BorderLayout.WEST);
        top.add(btnExport, BorderLayout.EAST);

        String[] cols = {"Mã Phiếu", "Độc Giả", "Số Điện Thoại", "Email", "Sách Mượn", "Ngày Mượn", "Hẹn Trả", "Quá Hạn (Ngày)", "Phạt Tạm Tính"};
        modelQuaHan = createReadOnlyModel(cols);
        tableQuaHan = new JTable(modelQuaHan);
        JScrollPane scroll = new JScrollPane(tableQuaHan);
        UITheme.styleTable(tableQuaHan, scroll, 0, 2, 5, 6, 7);
        tableQuaHan.getColumnModel().getColumn(8).setCellRenderer(UITheme.createStripedRenderer(SwingConstants.RIGHT));
        tableQuaHan.getColumnModel().getColumn(0).setPreferredWidth(70);
        tableQuaHan.getColumnModel().getColumn(1).setPreferredWidth(150);
        tableQuaHan.getColumnModel().getColumn(2).setPreferredWidth(115);
        tableQuaHan.getColumnModel().getColumn(3).setPreferredWidth(160);
        tableQuaHan.getColumnModel().getColumn(4).setPreferredWidth(190);
        tableQuaHan.getColumnModel().getColumn(5).setPreferredWidth(120);
        tableQuaHan.getColumnModel().getColumn(6).setPreferredWidth(95);
        tableQuaHan.getColumnModel().getColumn(7).setPreferredWidth(100);
        tableQuaHan.getColumnModel().getColumn(8).setPreferredWidth(115);

        btnExport.addActionListener(e -> CsvExporter.exportTableToCsv(this, tableQuaHan, "phieu_qua_han.csv"));

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFinesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_APP);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel lblNam = new JLabel("Chọn năm:");
        lblNam.setFont(UITheme.FONT_LABEL);
        cbNamPhat = new JComboBox<>(new String[]{"Tất cả các năm"});
        UITheme.styleComboBox(cbNamPhat);
        cbNamPhat.setPreferredSize(new Dimension(160, 32));
        cbNamPhat.addActionListener(e -> loadFinesForSelectedYear());

        lblTongTienPhat = new JLabel("Tổng tiền phạt đã thu: 0 đ");
        lblTongTienPhat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongTienPhat.setForeground(UITheme.PRIMARY);

        left.add(lblNam);
        left.add(cbNamPhat);
        left.add(lblTongTienPhat);

        JButton btnExport = UITheme.createButton("Xuất CSV", UITheme.SUCCESS, 120, 32);
        btnExport.addActionListener(e -> CsvExporter.exportTableToCsv(this, tableTienPhat, "thong_ke_tien_phat.csv"));

        top.add(left, BorderLayout.WEST);
        top.add(btnExport, BorderLayout.EAST);

        String[] cols = {"Năm", "Tháng", "Số Lượt Có Phạt", "Tổng Tiền Phạt"};
        modelTienPhat = createReadOnlyModel(cols);
        tableTienPhat = new JTable(modelTienPhat);
        JScrollPane scroll = new JScrollPane(tableTienPhat);
        UITheme.styleTable(tableTienPhat, scroll, 0, 1, 2);
        tableTienPhat.getColumnModel().getColumn(3).setCellRenderer(UITheme.createStripedRenderer(SwingConstants.RIGHT));

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTopBooksTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_APP);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblInfo = new JLabel("Bảng xếp hạng Top 20 đầu sách được mượn nhiều nhất");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInfo.setForeground(UITheme.TEXT_PRIMARY);

        JButton btnExport = UITheme.createButton("Xuất CSV", UITheme.SUCCESS, 120, 32);
        btnExport.addActionListener(e -> CsvExporter.exportTableToCsv(this, tableTopSach, "top_sach_muon_nhieu.csv"));

        top.add(lblInfo, BorderLayout.WEST);
        top.add(btnExport, BorderLayout.EAST);

        String[] cols = {"Hạng", "Mã Sách", "Tên Sách", "Tác Giả", "Thể Loại", "Số Lượt Mượn", "Đang Cho Mượn", "Còn Trong Kho"};
        modelTopSach = createReadOnlyModel(cols);
        tableTopSach = new JTable(modelTopSach);
        JScrollPane scroll = new JScrollPane(tableTopSach);
        UITheme.styleTable(tableTopSach, scroll, 0, 1, 5, 6, 7);
        tableTopSach.getColumnModel().getColumn(0).setPreferredWidth(60);
        tableTopSach.getColumnModel().getColumn(1).setPreferredWidth(75);
        tableTopSach.getColumnModel().getColumn(2).setPreferredWidth(250);
        tableTopSach.getColumnModel().getColumn(3).setPreferredWidth(150);
        tableTopSach.getColumnModel().getColumn(4).setPreferredWidth(150);
        tableTopSach.getColumnModel().getColumn(5).setPreferredWidth(110);
        tableTopSach.getColumnModel().getColumn(6).setPreferredWidth(115);
        tableTopSach.getColumnModel().getColumn(7).setPreferredWidth(110);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private DefaultTableModel createReadOnlyModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void loadAllReports() {
        try {
            // 1. Quá hạn
            List<Object[]> overdueRows = thongKeDAO.getDanhSachQuaHan();
            modelQuaHan.setRowCount(0);
            for (Object[] r : overdueRows) {
                Object ngayMuon = (r[5] instanceof java.util.Date) ? sdf.format((java.util.Date) r[5]) : r[5];
                modelQuaHan.addRow(new Object[]{
                    r[0], r[1], r[2], r[3], r[4], ngayMuon, r[6], r[7],
                    UITheme.formatCurrency((BigDecimal) r[8])
                });
            }
            lblTongQuaHan.setText("Tổng số sách đang quá hạn: " + overdueRows.size());

            // 2. Năm tiền phạt
            Object prevSelected = cbNamPhat.getSelectedItem();
            cbNamPhat.removeAllItems();
            cbNamPhat.addItem("Tất cả các năm");
            for (Integer year : thongKeDAO.getDanhSachNamPhat()) {
                cbNamPhat.addItem(String.valueOf(year));
            }
            if (prevSelected != null) {
                cbNamPhat.setSelectedItem(prevSelected);
            }
            loadFinesForSelectedYear();

            // 3. Top sách
            List<Object[]> topBooks = thongKeDAO.getTopSachMuonNhieuNhat(20);
            modelTopSach.setRowCount(0);
            for (Object[] r : topBooks) {
                modelTopSach.addRow(r);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi tải dữ liệu thống kê"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFinesForSelectedYear() {
        if (cbNamPhat.getItemCount() == 0 || cbNamPhat.getSelectedItem() == null) {
            return;
        }
        Integer nam = null;
        if (cbNamPhat.getSelectedIndex() > 0) {
            try {
                nam = Integer.parseInt(cbNamPhat.getSelectedItem().toString());
            } catch (NumberFormatException ignored) {}
        }
        try {
            BigDecimal tong = thongKeDAO.getTongTienPhat(nam);
            lblTongTienPhat.setText("Tổng tiền phạt đã thu: " + UITheme.formatCurrency(tong));

            List<Object[]> rows = thongKeDAO.getThongKeTienPhatTheoThang(nam);
            modelTienPhat.setRowCount(0);
            for (Object[] r : rows) {
                modelTienPhat.addRow(new Object[]{
                    r[0], r[1], r[2], UITheme.formatCurrency((BigDecimal) r[3])
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi tải thống kê tiền phạt"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }
}

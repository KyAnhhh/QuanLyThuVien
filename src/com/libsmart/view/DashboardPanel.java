package com.libsmart.view;

import com.libsmart.dao.DocGiaDAO;
import com.libsmart.dao.MuonTraDAO;
import com.libsmart.dao.SachDAO;
import com.libsmart.model.Sach;
import com.libsmart.utils.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình Tổng quan Dashboard hiển thị các chỉ số thực tế và danh sách sách mới cập nhật.
 */
public class DashboardPanel extends JPanel implements RefreshableView {

    private final SachDAO sachDAO;
    private final DocGiaDAO docGiaDAO;
    private final MuonTraDAO muonTraDAO;

    private JLabel lblTongSachValue, lblDocGiaValue, lblDangMuonValue;
    private DefaultTableModel tableOverviewModel;

    public DashboardPanel() {
        this.sachDAO = new SachDAO();
        this.docGiaDAO = new DocGiaDAO();
        this.muonTraDAO = new MuonTraDAO();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));

        JPanel panelTopHeader = new JPanel(new BorderLayout(10, 12));
        panelTopHeader.setOpaque(false);

        JPanel panelTitleRow = new JPanel(new BorderLayout());
        panelTitleRow.setOpaque(false);

        JLabel lblWelcome = new JLabel("BẢNG ĐIỀU KHIỂN HỆ THỐNG");
        lblWelcome.setFont(UITheme.FONT_TITLE);
        lblWelcome.setForeground(UITheme.TEXT_PRIMARY);

        JButton btnLamMoi = UITheme.createButton("Làm Mới Số Liệu", UITheme.PRIMARY, 155, 34);
        btnLamMoi.addActionListener(e -> refreshData());

        panelTitleRow.add(lblWelcome, BorderLayout.WEST);
        panelTitleRow.add(btnLamMoi, BorderLayout.EAST);

        JPanel panelCards = new JPanel(new GridLayout(1, 3, 14, 0));
        panelCards.setOpaque(false);

        lblTongSachValue = createCardValueLabel(UITheme.PRIMARY);
        lblDocGiaValue = createCardValueLabel(UITheme.SUCCESS);
        lblDangMuonValue = createCardValueLabel(UITheme.WARNING);

        panelCards.add(createStatCard("TỔNG SỐ SÁCH", lblTongSachValue, UITheme.PRIMARY));
        panelCards.add(createStatCard("ĐỘC GIẢ TÍCH CỰC", lblDocGiaValue, UITheme.SUCCESS));
        panelCards.add(createStatCard("ĐANG CHO MƯỢN", lblDangMuonValue, UITheme.WARNING));

        panelTopHeader.add(panelTitleRow, BorderLayout.NORTH);
        panelTopHeader.add(panelCards, BorderLayout.CENTER);
        add(panelTopHeader, BorderLayout.NORTH);

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
        add(panelTableOverview, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        List<Sach> listSach = Collections.emptyList();
        int tongSoSach = 0;
        int docGiaTichCuc = 0;
        int dangChoMuon = muonTraDAO.countPhieuDangMuon();
        try {
            listSach = sachDAO.getAllSach();
            tongSoSach = sachDAO.sumTongSoLuongSach();
            docGiaTichCuc = docGiaDAO.countActiveDocGia();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải dữ liệu từ cơ sở dữ liệu:\n" + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }

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
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(color);
        lblValue.setBorder(BorderFactory.createEmptyBorder(0, 14, 10, 14));
        return lblValue;
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 88));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createMatteBorder(0, 5, 0, 0, color)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 14, 4, 14));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }
}

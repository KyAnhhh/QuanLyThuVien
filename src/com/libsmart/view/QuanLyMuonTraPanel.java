package com.libsmart.view;

import com.libsmart.dao.DocGiaDAO;
import com.libsmart.dao.MuonTraDAO;
import com.libsmart.dao.SachDAO;
import com.libsmart.model.DocGia;
import com.libsmart.model.NguoiDung;
import com.libsmart.model.Sach;
import com.libsmart.utils.CsvExporter;
import com.libsmart.utils.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình Quản lý Mượn - Trả sách dạng JPanel trong kiến trúc Single-Page.
 */
public class QuanLyMuonTraPanel extends JPanel implements RefreshableView {

    private JComboBox<String> cbDocGia, cbSach, cbFilterTrangThai;
    private JTextField txtSoNgayMuon, txtMaPhieu, txtSearch;
    private JButton btnThemVaoPhieu, btnXoaKhoiPhieu, btnChoMuon, btnTraTungCuon, btnTraCaPhieu, btnLamMoi;
    private JLabel lblCartCount;
    private DefaultListModel<String> cartListModel;
    private JList<String> listCart;
    private final List<Integer> cartSachIds = new ArrayList<>();
    private int selectedMaChiTiet = -1;
    private List<Object[]> cachedAllRows = new ArrayList<>();

    private JTable table;
    private DefaultTableModel tableModel;

    private final MuonTraDAO muonTraDAO;
    private final DocGiaDAO docGiaDAO;
    private final SachDAO sachDAO;
    private final NguoiDung currentUser;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public QuanLyMuonTraPanel(NguoiDung currentUser) {
        this.currentUser = currentUser;
        this.muonTraDAO = new MuonTraDAO();
        this.docGiaDAO = new DocGiaDAO();
        this.sachDAO = new SachDAO();
        initComponents();
    }

    @Override
    public void refreshData() {
        loadDataToCB();
        reloadTableData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 20, 12, 20));

        // 1. TIÊU ĐỀ
        JLabel lblTitle = new JLabel("QUẢN LÝ MƯỢN TRẢ SÁCH", SwingConstants.LEFT);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);

        // 2. TÌM KIẾM & LỌC TRẠNG THÁI
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panelSearch.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm (Độc giả / Sách):");
        lblSearch.setFont(UITheme.FONT_LABEL);
        lblSearch.setForeground(UITheme.TEXT_PRIMARY);

        txtSearch = UITheme.createTextField(true);
        txtSearch.setPreferredSize(new Dimension(250, 32));

        JLabel lblFilter = new JLabel("Trạng thái:");
        lblFilter.setFont(UITheme.FONT_LABEL);
        lblFilter.setForeground(UITheme.TEXT_PRIMARY);

        cbFilterTrangThai = new JComboBox<>(new String[]{"Tất cả", "Đang mượn", "Quá hạn", "Đã trả"});
        UITheme.styleComboBox(cbFilterTrangThai);
        cbFilterTrangThai.setPreferredSize(new Dimension(145, 32));

        panelSearch.add(lblSearch);
        panelSearch.add(txtSearch);
        panelSearch.add(lblFilter);
        panelSearch.add(cbFilterTrangThai);

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });
        cbFilterTrangThai.addActionListener(e -> applyFilter());

        // 3. FORM LẬP PHIẾU
        JPanel panelForm = UITheme.createTitledCard("Lập Phiếu Mượn Sách (Tối đa " + MuonTraDAO.MAX_SACH_DANG_MUON + " cuốn đang mượn / độc giả)");
        panelForm.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbDocGia = new JComboBox<>();
        UITheme.styleComboBox(cbDocGia);

        cbSach = new JComboBox<>();
        UITheme.styleComboBox(cbSach);

        JPanel panelSachRow = new JPanel(new BorderLayout(6, 0));
        panelSachRow.setOpaque(false);
        panelSachRow.add(cbSach, BorderLayout.CENTER);
        btnThemVaoPhieu = UITheme.createButton("+ Thêm Vào Phiếu", UITheme.PRIMARY, 140, 32);
        panelSachRow.add(btnThemVaoPhieu, BorderLayout.EAST);

        txtSoNgayMuon = UITheme.createTextField(true);
        txtSoNgayMuon.setText("7");

        txtMaPhieu = UITheme.createTextField(false);

        UITheme.addFormCell(panelForm, gbc, "Chọn Độc Giả:", cbDocGia, 0, 0);
        UITheme.addFormCell(panelForm, gbc, "Chọn Sách Mượn:", panelSachRow, 2, 0);
        UITheme.addFormCell(panelForm, gbc, "Số Ngày Mượn:", txtSoNgayMuon, 0, 1);
        UITheme.addFormCell(panelForm, gbc, "Mã Phiếu Đang Chọn:", txtMaPhieu, 2, 1);

        cartListModel = new DefaultListModel<>();
        listCart = new JList<>(cartListModel);
        listCart.setFont(UITheme.FONT_INPUT);
        JScrollPane cartScroll = new JScrollPane(listCart);
        cartScroll.setPreferredSize(new Dimension(380, 58));
        cartScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        JPanel panelCartControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        panelCartControls.setOpaque(false);
        lblCartCount = new JLabel("Giỏ mượn: 0 / " + MuonTraDAO.MAX_SACH_DANG_MUON + " cuốn (nếu trống sẽ mượn sách đang chọn ở ComboBox)");
        lblCartCount.setFont(UITheme.FONT_SUBTITLE);
        lblCartCount.setForeground(UITheme.TEXT_SECONDARY);
        btnXoaKhoiPhieu = UITheme.createButton("Xóa Sách Chọn", UITheme.DANGER, 125, 26);
        panelCartControls.add(btnXoaKhoiPhieu);
        panelCartControls.add(lblCartCount);

        JPanel panelCartBox = new JPanel(new BorderLayout(4, 4));
        panelCartBox.setOpaque(false);
        panelCartBox.add(cartScroll, BorderLayout.CENTER);
        panelCartBox.add(panelCartControls, BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.12;
        JLabel lblCart = new JLabel("Giỏ Sách Mượn:");
        lblCart.setFont(UITheme.FONT_LABEL);
        lblCart.setForeground(UITheme.TEXT_PRIMARY);
        panelForm.add(lblCart, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 0.88;
        panelForm.add(panelCartBox, gbc);
        gbc.gridwidth = 1;

        // 4. BỘ NÚT BẤM
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        panelButtons.setOpaque(false);

        btnChoMuon = UITheme.createButton("Cho Mượn Sách", UITheme.SUCCESS, 145, 34);
        btnTraTungCuon = UITheme.createButton("Trả Cuốn Đang Chọn", UITheme.PRIMARY, 165, 34);
        btnTraCaPhieu = UITheme.createButton("Trả Cả Phiếu", UITheme.WARNING, 130, 34);
        btnLamMoi = UITheme.createButton("Làm Mới", UITheme.MUTED, 110, 34);
        JButton btnXuatCsv = UITheme.createButton("Xuất CSV", new Color(15, 118, 110), 110, 34);
        btnXuatCsv.addActionListener(e -> CsvExporter.exportTableToCsv(this, table, "danh_sach_muon_tra.csv"));

        panelButtons.add(btnChoMuon);
        panelButtons.add(btnTraTungCuon);
        panelButtons.add(btnTraCaPhieu);
        panelButtons.add(btnLamMoi);
        panelButtons.add(btnXuatCsv);

        JPanel panelTopHeader = new JPanel(new BorderLayout(6, 6));
        panelTopHeader.setOpaque(false);
        panelTopHeader.add(lblTitle, BorderLayout.NORTH);
        panelTopHeader.add(panelSearch, BorderLayout.CENTER);

        JPanel panelCenterTop = new JPanel(new BorderLayout(6, 6));
        panelCenterTop.setOpaque(false);
        panelCenterTop.add(panelForm, BorderLayout.CENTER);
        panelCenterTop.add(panelButtons, BorderLayout.SOUTH);

        JPanel panelHeaderContainer = new JPanel(new BorderLayout(6, 6));
        panelHeaderContainer.setOpaque(false);
        panelHeaderContainer.add(panelTopHeader, BorderLayout.NORTH);
        panelHeaderContainer.add(panelCenterTop, BorderLayout.SOUTH);

        add(panelHeaderContainer, BorderLayout.NORTH);

        // 5. BẢNG JTABLE
        String[] columns = {"Mã Phiếu", "Mã CT", "Độc Giả", "Sách Mượn", "Thủ Thư", "Ngày Mượn", "Hẹn Trả", "Trạng Thái", "Tiền Phạt", "Ghi Chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        UITheme.styleTable(table, scrollPane, 0, 1, 5, 6);
        table.getColumnModel().getColumn(7).setCellRenderer(UITheme.createStatusRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(UITheme.createStripedRenderer(SwingConstants.RIGHT));
        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(55);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(115);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);
        table.getColumnModel().getColumn(8).setPreferredWidth(85);
        table.getColumnModel().getColumn(9).setPreferredWidth(135);

        add(scrollPane, BorderLayout.CENTER);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtMaPhieu.setText(getVal(row, 0));
                    try {
                        selectedMaChiTiet = Integer.parseInt(getVal(row, 1));
                    } catch (NumberFormatException ex) {
                        selectedMaChiTiet = -1;
                    }
                }
            }
        });

        btnThemVaoPhieu.addActionListener(e -> addBookToCart());
        btnXoaKhoiPhieu.addActionListener(e -> removeSelectedBookFromCart());
        btnLamMoi.addActionListener(e -> clearForm());
        btnChoMuon.addActionListener(e -> choMuonSach());
        btnTraTungCuon.addActionListener(e -> traCuonDangChon());
        btnTraCaPhieu.addActionListener(e -> traCaPhieu());
    }

    private String getVal(int row, int col) {
        Object obj = tableModel.getValueAt(row, col);
        return obj != null ? obj.toString() : "";
    }

    private void loadDataToCB() {
        cbDocGia.removeAllItems();
        LocalDate today = LocalDate.now();
        try {
            List<DocGia> listDG = docGiaDAO.getAllDocGia();
            if (listDG != null) {
                for (DocGia dg : listDG) {
                    String tag = "";
                    if (!"HOAT_DONG".equalsIgnoreCase(dg.getTrangThai())) {
                        tag = " [Khóa]";
                    } else if (dg.getNgayHetHan() != null && dg.getNgayHetHan().toLocalDate().isBefore(today)) {
                        tag = " [Hết hạn]";
                    }
                    cbDocGia.addItem(dg.getMaDocGia() + " - " + dg.getHoTen() + tag);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh sách độc giả:\n" + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }

        cbSach.removeAllItems();
        try {
            List<Sach> listS = sachDAO.getAllSach();
            if (listS != null) {
                for (Sach s : listS) {
                    cbSach.addItem(s.getMaSach() + " - " + s.getTenSach() + " (Còn: " + s.getSoLuongCon() + ")");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh sách sách:\n" + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String resolveDisplayStatus(Object[] row) {
        if (row[7] == null) {
            return "";
        }
        String tt = row[7].toString();
        if (tt.equalsIgnoreCase("DANG_MUON")) {
            boolean quaHan = (row[6] instanceof java.sql.Date)
                    && ((java.sql.Date) row[6]).toLocalDate().isBefore(LocalDate.now());
            return quaHan ? "Quá hạn" : "Đang mượn";
        }
        if (tt.equalsIgnoreCase("DA_TRA")) {
            return "Đã trả";
        }
        return tt;
    }

    private void formatAndAddRow(Object[] row) {
        Object[] formattedRow = new Object[row.length];
        System.arraycopy(row, 0, formattedRow, 0, row.length);

        if (row[5] instanceof java.util.Date) {
            formattedRow[5] = sdf.format((java.util.Date) row[5]);
        }

        formattedRow[7] = resolveDisplayStatus(row);

        if (row.length > 8 && row[8] instanceof BigDecimal) {
            formattedRow[8] = UITheme.formatCurrency((BigDecimal) row[8]);
        }

        tableModel.addRow(formattedRow);
    }

    private void reloadTableData() {
        cachedAllRows = muonTraDAO.getAllPhieuMuon();
        applyFilter();
    }

    private void applyFilter() {
        String kw = txtSearch.getText().trim().toLowerCase();
        String statusFilter = cbFilterTrangThai.getSelectedItem() != null
                ? cbFilterTrangThai.getSelectedItem().toString()
                : "Tất cả";

        tableModel.setRowCount(0);
        for (Object[] row : cachedAllRows) {
            String docGia = row[2] != null ? row[2].toString().toLowerCase() : "";
            String sach = row[3] != null ? row[3].toString().toLowerCase() : "";
            String thuThu = row[4] != null ? row[4].toString().toLowerCase() : "";
            boolean matchKw = kw.isEmpty() || docGia.contains(kw) || sach.contains(kw) || thuThu.contains(kw);

            String displayStatus = resolveDisplayStatus(row);
            boolean matchStatus = "Tất cả".equals(statusFilter) || statusFilter.equalsIgnoreCase(displayStatus);

            if (matchKw && matchStatus) {
                formatAndAddRow(row);
            }
        }
    }

    private void updateCartLabel() {
        lblCartCount.setText("Giỏ mượn: " + cartSachIds.size()
                + " / " + MuonTraDAO.MAX_SACH_DANG_MUON + " cuốn (nếu trống sẽ mượn sách đang chọn ở ComboBox)");
    }

    private void addBookToCart() {
        if (cbSach.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách!");
            return;
        }
        if (cartSachIds.size() >= MuonTraDAO.MAX_SACH_DANG_MUON) {
            JOptionPane.showMessageDialog(this, "Mỗi phiếu chỉ chọn tối đa " + MuonTraDAO.MAX_SACH_DANG_MUON + " cuốn!");
            return;
        }
        String item = cbSach.getSelectedItem().toString();
        int maSach = Integer.parseInt(item.split(" - ")[0]);
        if (cartSachIds.contains(maSach)) {
            JOptionPane.showMessageDialog(this, "Sách này đã có trong giỏ mượn!");
            return;
        }
        cartSachIds.add(maSach);
        cartListModel.addElement(item);
        updateCartLabel();
    }

    private void removeSelectedBookFromCart() {
        int idx = listCart.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Chọn 1 sách trong giỏ mượn để xóa!");
            return;
        }
        cartSachIds.remove(idx);
        cartListModel.remove(idx);
        updateCartLabel();
    }

    private void clearForm() {
        txtMaPhieu.setText("");
        selectedMaChiTiet = -1;
        txtSoNgayMuon.setText("7");
        txtSearch.setText("");
        cbFilterTrangThai.setSelectedIndex(0);
        cartSachIds.clear();
        cartListModel.clear();
        updateCartLabel();
        table.clearSelection();
        refreshData();
    }

    private void choMuonSach() {
        if (currentUser == null || currentUser.getMaNguoiDung() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Không xác định được tài khoản thủ thư đang đăng nhập. Vui lòng đăng nhập lại!",
                    "Lỗi phiên đăng nhập", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cbDocGia.getSelectedItem() == null || cbSach.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Độc giả và Sách!");
            return;
        }

        try {
            int maDG = Integer.parseInt(cbDocGia.getSelectedItem().toString().split(" - ")[0]);
            int soNgay = Integer.parseInt(txtSoNgayMuon.getText().trim());

            List<Integer> booksToBorrow = new ArrayList<>(cartSachIds);
            if (booksToBorrow.isEmpty()) {
                booksToBorrow.add(Integer.parseInt(cbSach.getSelectedItem().toString().split(" - ")[0]));
            }

            MuonTraDAO.KetQuaMuon kq = muonTraDAO.taoPhieuMuonNhieuSach(
                    maDG, booksToBorrow, soNgay, currentUser.getMaNguoiDung());
            if (kq.isThanhCong()) {
                JOptionPane.showMessageDialog(this, kq.getThongBao());
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, kq.getThongBao(), "Không thể lập phiếu mượn", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số ngày mượn phải là số nguyên hợp lệ!");
        }
    }

    private void traCuonDangChon() {
        if (selectedMaChiTiet <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng sách trên bảng để trả!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận trả cuốn sách (Mã chi tiết #" + selectedMaChiTiet + ")?",
                "Xác nhận trả sách", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            showReturnResult(muonTraDAO.traTheoChiTiet(selectedMaChiTiet));
        }
    }

    private void traCaPhieu() {
        if (txtMaPhieu.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 phiếu mượn trên bảng để xác nhận trả!");
            return;
        }

        int maPhieu = Integer.parseInt(txtMaPhieu.getText().trim());
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận trả toàn bộ sách chưa trả trong phiếu #" + maPhieu + "?",
                "Xác nhận trả cả phiếu", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            showReturnResult(muonTraDAO.traSachChiTiet(maPhieu));
        }
    }

    private void showReturnResult(MuonTraDAO.KetQuaTra kq) {
        if (kq.isThanhCong()) {
            int msgType = kq.getSoNgayTre() > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
            JOptionPane.showMessageDialog(this, kq.getThongBao(), "Kết quả trả sách", msgType);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, kq.getThongBao(), "Trả sách thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }
}

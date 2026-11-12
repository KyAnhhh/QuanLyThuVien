package com.libsmart.view;

import com.libsmart.dao.SachDAO;
import com.libsmart.model.Sach;
import com.libsmart.utils.CsvExporter;
import com.libsmart.utils.UITheme;
import com.libsmart.utils.ValidationUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình Quản lý Danh mục Sách dạng JPanel trong kiến trúc Single-Page.
 */
public class QuanLySachPanel extends JPanel implements RefreshableView {

    private JTextField txtMaSach, txtTenSach, txtNamXB, txtSoLuongTong, txtSoLuongCon, txtGiaBia, txtSearch;
    private JComboBox<String> cbTacGia, cbTheLoai, cbFilterTacGia, cbFilterTheLoai;
    private JCheckBox chkConHang;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;
    private final SachDAO sachDAO;
    private Map<Integer, String> mapTacGia = new TreeMap<>();
    private Map<Integer, String> mapTheLoai = new TreeMap<>();
    private List<Sach> allSachList = new ArrayList<>();
    private List<Sach> currentSachList = new ArrayList<>();

    public QuanLySachPanel() {
        this.sachDAO = new SachDAO();
        initComponents();
    }

    @Override
    public void refreshData() {
        loadDanhMucComboBox();
        loadAllSachSafe();
    }

    private void loadDanhMucComboBox() {
        cbTacGia.removeAllItems();
        cbTheLoai.removeAllItems();
        cbFilterTacGia.removeAllItems();
        cbFilterTheLoai.removeAllItems();
        cbFilterTacGia.addItem("Tất cả tác giả");
        cbFilterTheLoai.addItem("Tất cả thể loại");
        try {
            mapTacGia = new TreeMap<>(sachDAO.getMapTacGia());
            for (Map.Entry<Integer, String> entry : mapTacGia.entrySet()) {
                String item = entry.getKey() + " - " + entry.getValue();
                cbTacGia.addItem(item);
                cbFilterTacGia.addItem(item);
            }

            mapTheLoai = new TreeMap<>(sachDAO.getMapTheLoai());
            for (Map.Entry<Integer, String> entry : mapTheLoai.entrySet()) {
                String item = entry.getKey() + " - " + entry.getValue();
                cbTheLoai.addItem(item);
                cbFilterTheLoai.addItem(item);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh mục Tác giả / Thể loại:\n" + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectComboById(JComboBox<String> comboBox, int id) {
        String prefix = id + " - ";
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if (item != null && item.startsWith(prefix)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadAllSachSafe() {
        try {
            allSachList = sachDAO.getAllSach();
            applySachFilter();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh sách sách:\n" + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applySachFilter() {
        String kw = txtSearch.getText().trim().toLowerCase();
        int filterTgId = parseFilterComboId(cbFilterTacGia);
        int filterTlId = parseFilterComboId(cbFilterTheLoai);
        boolean onlyInStock = chkConHang.isSelected();

        List<Sach> filtered = new ArrayList<>();
        for (Sach s : allSachList) {
            boolean matchKw = kw.isEmpty() || (s.getTenSach() != null && s.getTenSach().toLowerCase().contains(kw));
            boolean matchTg = filterTgId < 0 || s.getMaTacGia() == filterTgId;
            boolean matchTl = filterTlId < 0 || s.getMaTheLoai() == filterTlId;
            boolean matchStock = !onlyInStock || s.getSoLuongCon() > 0;
            if (matchKw && matchTg && matchTl && matchStock) {
                filtered.add(s);
            }
        }
        loadDataToTable(filtered);
    }

    private int parseFilterComboId(JComboBox<String> comboBox) {
        if (comboBox.getSelectedIndex() <= 0 || comboBox.getSelectedItem() == null) {
            return -1;
        }
        try {
            return Integer.parseInt(comboBox.getSelectedItem().toString().split(" - ")[0]);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 14, 20));

        // 1. TIÊU ĐỀ
        JLabel lblTitle = new JLabel("QUẢN LÝ DANH MỤC SÁCH", SwingConstants.LEFT);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);

        // 2. TÌM KIẾM & BỘ LỌC NÂNG CAO
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelSearch.setOpaque(false);

        JLabel lblSearch = new JLabel("Tên sách:");
        lblSearch.setFont(UITheme.FONT_LABEL);
        lblSearch.setForeground(UITheme.TEXT_PRIMARY);

        txtSearch = UITheme.createTextField(true);
        txtSearch.setPreferredSize(new Dimension(200, 32));

        cbFilterTacGia = new JComboBox<>();
        UITheme.styleComboBox(cbFilterTacGia);
        cbFilterTacGia.setPreferredSize(new Dimension(155, 32));

        cbFilterTheLoai = new JComboBox<>();
        UITheme.styleComboBox(cbFilterTheLoai);
        cbFilterTheLoai.setPreferredSize(new Dimension(155, 32));

        chkConHang = new JCheckBox("Chỉ sách còn hàng");
        chkConHang.setFont(UITheme.FONT_INPUT);
        chkConHang.setForeground(UITheme.TEXT_PRIMARY);
        chkConHang.setOpaque(false);
        chkConHang.setFocusPainted(false);

        panelSearch.add(lblSearch);
        panelSearch.add(txtSearch);
        panelSearch.add(cbFilterTacGia);
        panelSearch.add(cbFilterTheLoai);
        panelSearch.add(chkConHang);

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applySachFilter();
            }
        });
        cbFilterTacGia.addActionListener(e -> applySachFilter());
        cbFilterTheLoai.addActionListener(e -> applySachFilter());
        chkConHang.addActionListener(e -> applySachFilter());

        // 3. FORM NHẬP LIỆU
        JPanel panelForm = UITheme.createTitledCard("Thông Tin Sách");
        panelForm.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMaSach = UITheme.createTextField(false);
        txtTenSach = UITheme.createTextField(true);
        txtNamXB = UITheme.createTextField(true);
        txtSoLuongTong = UITheme.createTextField(true);
        txtSoLuongCon = UITheme.createTextField(true);
        txtGiaBia = UITheme.createTextField(true);

        cbTacGia = new JComboBox<>();
        UITheme.styleComboBox(cbTacGia);

        cbTheLoai = new JComboBox<>();
        UITheme.styleComboBox(cbTheLoai);

        UITheme.addFormCell(panelForm, gbc, "Mã Sách:", txtMaSach, 0, 0);
        UITheme.addFormCell(panelForm, gbc, "Tên Sách:", txtTenSach, 2, 0);
        UITheme.addFormCell(panelForm, gbc, "Tác Giả:", wrapComboWithManageButton(cbTacGia, 0), 0, 1);
        UITheme.addFormCell(panelForm, gbc, "Thể Loại:", wrapComboWithManageButton(cbTheLoai, 1), 2, 1);
        UITheme.addFormCell(panelForm, gbc, "Năm Xuất Bản:", txtNamXB, 0, 2);
        UITheme.addFormCell(panelForm, gbc, "Số Lượng Tổng:", txtSoLuongTong, 2, 2);
        UITheme.addFormCell(panelForm, gbc, "Số Lượng Còn:", txtSoLuongCon, 0, 3);
        UITheme.addFormCell(panelForm, gbc, "Giá Bìa (VNĐ):", txtGiaBia, 2, 3);

        // 4. BỘ NÚT BẤM
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        panelButtons.setOpaque(false);

        btnThem = UITheme.createButton("Thêm Sách", UITheme.SUCCESS, 120, 34);
        btnSua = UITheme.createButton("Sửa Sách", UITheme.PRIMARY, 120, 34);
        btnXoa = UITheme.createButton("Xóa Sách", UITheme.DANGER, 120, 34);
        btnLamMoi = UITheme.createButton("Làm Mới", UITheme.MUTED, 115, 34);
        JButton btnXuatCsv = UITheme.createButton("Xuất CSV", new Color(15, 118, 110), 115, 34);
        btnXuatCsv.addActionListener(e -> CsvExporter.exportTableToCsv(this, table, "danh_sach_sach.csv"));

        panelButtons.add(btnThem);
        panelButtons.add(btnSua);
        panelButtons.add(btnXoa);
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

        JPanel panelHeaderContainer = new JPanel(new BorderLayout(8, 8));
        panelHeaderContainer.setOpaque(false);
        panelHeaderContainer.add(panelTopHeader, BorderLayout.NORTH);
        panelHeaderContainer.add(panelCenterTop, BorderLayout.SOUTH);

        add(panelHeaderContainer, BorderLayout.NORTH);

        // 5. BẢNG DỮ LIỆU JTABLE
        String[] columns = {"Mã Sách", "Tên Sách", "Tác Giả", "Thể Loại", "Năm XB", "Tổng Số", "Còn Lại", "Giá Bìa"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        UITheme.styleTable(table, scrollPane, 0, 4, 5, 6);
        table.getColumnModel().getColumn(7).setCellRenderer(UITheme.createStripedRenderer(SwingConstants.RIGHT));
        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(135);
        table.getColumnModel().getColumn(3).setPreferredWidth(145);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(75);
        table.getColumnModel().getColumn(6).setPreferredWidth(75);
        table.getColumnModel().getColumn(7).setPreferredWidth(105);

        add(scrollPane, BorderLayout.CENTER);

        // 6. SỰ KIỆN
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < currentSachList.size()) {
                    Sach selected = currentSachList.get(row);
                    txtMaSach.setText(String.valueOf(selected.getMaSach()));
                    txtTenSach.setText(selected.getTenSach());
                    txtNamXB.setText(String.valueOf(selected.getNamXuatBan()));
                    txtSoLuongTong.setText(String.valueOf(selected.getSoLuongTong()));
                    txtSoLuongCon.setText(String.valueOf(selected.getSoLuongCon()));
                    txtGiaBia.setText(selected.getGiaBia() != null ? selected.getGiaBia().toPlainString() : "");
                    selectComboById(cbTacGia, selected.getMaTacGia());
                    selectComboById(cbTheLoai, selected.getMaTheLoai());
                }
            }
        });

        btnLamMoi.addActionListener(e -> clearForm());
        btnThem.addActionListener(e -> addSach());
        btnSua.addActionListener(e -> updateSach());
        btnXoa.addActionListener(e -> deleteSach());
    }

    private void loadDataToTable(List<Sach> list) {
        tableModel.setRowCount(0);
        currentSachList = (list != null) ? new ArrayList<>(list) : new ArrayList<>();
        for (Sach s : currentSachList) {
            String tenTG = mapTacGia.getOrDefault(s.getMaTacGia(), String.valueOf(s.getMaTacGia()));
            String tenTL = mapTheLoai.getOrDefault(s.getMaTheLoai(), String.valueOf(s.getMaTheLoai()));
            tableModel.addRow(new Object[]{
                s.getMaSach(), s.getTenSach(), tenTG, tenTL,
                s.getNamXuatBan(), s.getSoLuongTong(), s.getSoLuongCon(),
                UITheme.formatCurrency(s.getGiaBia())
            });
        }
    }

    private void clearForm() {
        txtMaSach.setText("");
        txtTenSach.setText("");
        txtNamXB.setText("");
        txtSoLuongTong.setText("");
        txtSoLuongCon.setText("");
        txtGiaBia.setText("");
        txtSearch.setText("");
        chkConHang.setSelected(false);
        table.clearSelection();
        refreshData();
    }

    private JPanel wrapComboWithManageButton(JComboBox<String> comboBox, int initialTabIndex) {
        JPanel wrapper = new JPanel(new BorderLayout(6, 0));
        wrapper.setOpaque(false);
        wrapper.add(comboBox, BorderLayout.CENTER);

        JButton btnManage = UITheme.createButton("+", UITheme.PRIMARY, 38, 32);
        btnManage.setToolTipText("Quản lý / Thêm nhanh Tác giả & Thể loại");
        btnManage.addActionListener(e -> openDanhMucDialog(initialTabIndex));
        wrapper.add(btnManage, BorderLayout.EAST);
        return wrapper;
    }

    private void openDanhMucDialog(int initialTabIndex) {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame owner = (window instanceof Frame) ? (Frame) window : null;
        DanhMucDialog dialog = new DanhMucDialog(owner, sachDAO, initialTabIndex);
        dialog.setVisible(true);
        if (dialog.isDataChanged()) {
            refreshData();
            if (dialog.getLastAddedTacGiaId() > 0) {
                selectComboById(cbTacGia, dialog.getLastAddedTacGiaId());
            }
            if (dialog.getLastAddedTheLoaiId() > 0) {
                selectComboById(cbTheLoai, dialog.getLastAddedTheLoaiId());
            }
        }
    }

    private Sach parseAndValidateSach(int maSach, boolean isUpdate) {
        String tenSach = txtTenSach.getText().trim();
        if (tenSach.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sách!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (cbTacGia.getSelectedItem() == null || cbTheLoai.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Tác giả và Thể loại!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        try {
            int maTG = Integer.parseInt(cbTacGia.getSelectedItem().toString().split(" - ")[0]);
            int maTL = Integer.parseInt(cbTheLoai.getSelectedItem().toString().split(" - ")[0]);
            int namXB = Integer.parseInt(txtNamXB.getText().trim());
            int maxYear = LocalDate.now().getYear() + 1;
            if (namXB < 1900 || namXB > maxYear) {
                JOptionPane.showMessageDialog(this, "Năm xuất bản phải nằm trong khoảng 1900 - " + maxYear + "!", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            int soLuongTong = Integer.parseInt(txtSoLuongTong.getText().trim());
            if (soLuongTong < 0) {
                JOptionPane.showMessageDialog(this, "Số lượng tổng không được âm!", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            int soLuongCon = isUpdate
                    ? Integer.parseInt(txtSoLuongCon.getText().trim())
                    : soLuongTong;
            if (soLuongCon < 0 || soLuongCon > soLuongTong) {
                JOptionPane.showMessageDialog(this, "Số lượng còn phải nằm trong khoảng 0 đến " + soLuongTong + "!", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            BigDecimal giaBia = new BigDecimal(txtGiaBia.getText().trim());
            if (giaBia.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Giá bìa không được âm!", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return new Sach(maSach, tenSach, maTG, maTL, namXB, soLuongTong, soLuongCon, giaBia);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ cho Năm xuất bản, Số lượng và Giá bìa!", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private void addSach() {
        Sach s = parseAndValidateSach(0, false);
        if (s == null) {
            return;
        }
        try {
            if (sachDAO.insertSach(s)) {
                JOptionPane.showMessageDialog(this, "Thêm sách thành công!");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi cơ sở dữ liệu khi thêm sách"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSach() {
        if (txtMaSach.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn 1 dòng để sửa!");
            return;
        }
        int maSach = Integer.parseInt(txtMaSach.getText().trim());
        Sach s = parseAndValidateSach(maSach, true);
        if (s == null) {
            return;
        }
        try {
            if (sachDAO.updateSach(s)) {
                JOptionPane.showMessageDialog(this, "Sửa thành công!");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Sửa thất bại!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi cơ sở dữ liệu khi cập nhật sách"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSach() {
        if (txtMaSach.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn 1 dòng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa sách này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int maSach = Integer.parseInt(txtMaSach.getText().trim());
                if (sachDAO.deleteSach(maSach)) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        ValidationUtils.friendlySqlError(ex, "Không thể xóa sách"),
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

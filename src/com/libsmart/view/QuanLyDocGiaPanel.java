package com.libsmart.view;

import com.libsmart.dao.DocGiaDAO;
import com.libsmart.model.DocGia;
import com.libsmart.utils.CsvExporter;
import com.libsmart.utils.UITheme;
import com.libsmart.utils.ValidationUtils;
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
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình Quản lý Độc giả & Thẻ thư viện dạng JPanel trong kiến trúc Single-Page.
 */
public class QuanLyDocGiaPanel extends JPanel implements RefreshableView {

    private JTextField txtMaDG, txtHoTen, txtSoDienThoai, txtEmail, txtNgayHetHan, txtSearch;
    private JComboBox<String> cbTrangThai;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnGiaHan, btnKhoaMo;
    private JTable table;
    private DefaultTableModel tableModel;
    private final DocGiaDAO docGiaDAO;
    private List<DocGia> currentDocGiaList = new ArrayList<>();

    public QuanLyDocGiaPanel() {
        this.docGiaDAO = new DocGiaDAO();
        initComponents();
    }

    @Override
    public void refreshData() {
        loadAllDocGiaSafe();
    }

    private void loadAllDocGiaSafe() {
        try {
            loadDataToTable(docGiaDAO.getAllDocGia());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi tải danh sách độc giả"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 14, 20));

        // 1. TIÊU ĐỀ
        JLabel lblTitle = new JLabel("QUẢN LÝ DANH MỤC ĐỘC GIẢ", SwingConstants.LEFT);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);

        // 2. Ô TÌM KIẾM
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panelSearch.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm độc giả:");
        lblSearch.setFont(UITheme.FONT_LABEL);
        lblSearch.setForeground(UITheme.TEXT_PRIMARY);

        txtSearch = UITheme.createTextField(true);
        txtSearch.setPreferredSize(new Dimension(300, 32));

        panelSearch.add(lblSearch);
        panelSearch.add(txtSearch);

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String kw = txtSearch.getText().trim();
                try {
                    if (kw.isEmpty()) {
                        loadDataToTable(docGiaDAO.getAllDocGia());
                    } else {
                        loadDataToTable(docGiaDAO.searchDocGiaByTen(kw));
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(QuanLyDocGiaPanel.this,
                            ValidationUtils.friendlySqlError(ex, "Lỗi tìm kiếm độc giả"),
                            "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 3. FORM NHẬP LIỆU
        JPanel panelForm = UITheme.createTitledCard("Thông Tin Độc Giả & Thẻ Thư Viện");
        panelForm.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMaDG = UITheme.createTextField(false);
        txtHoTen = UITheme.createTextField(true);
        txtSoDienThoai = UITheme.createTextField(true);
        txtEmail = UITheme.createTextField(true);
        txtNgayHetHan = UITheme.createTextField(true);
        txtNgayHetHan.setToolTipText("Định dạng yyyy-MM-dd (để trống khi thêm mới sẽ mặc định +1 năm)");

        cbTrangThai = new JComboBox<>(new String[]{"HOAT_DONG - Hoạt động", "KHOA - Khóa"});
        UITheme.styleComboBox(cbTrangThai);

        UITheme.addFormCell(panelForm, gbc, "Mã Độc Giả:", txtMaDG, 0, 0);
        UITheme.addFormCell(panelForm, gbc, "Họ và Tên:", txtHoTen, 2, 0);
        UITheme.addFormCell(panelForm, gbc, "Số Điện Thoại:", txtSoDienThoai, 0, 1);
        UITheme.addFormCell(panelForm, gbc, "Email:", txtEmail, 2, 1);
        UITheme.addFormCell(panelForm, gbc, "Hạn Thẻ (yyyy-MM-dd):", txtNgayHetHan, 0, 2);
        UITheme.addFormCell(panelForm, gbc, "Trạng Thái Thẻ:", cbTrangThai, 2, 2);

        // 4. BỘ NÚT BẤM
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        panelButtons.setOpaque(false);

        btnThem = UITheme.createButton("Thêm Độc Giả", UITheme.SUCCESS, 120, 34);
        btnSua = UITheme.createButton("Sửa Độc Giả", UITheme.PRIMARY, 120, 34);
        btnGiaHan = UITheme.createButton("Gia Hạn +1 Năm", UITheme.WARNING, 136, 34);
        btnKhoaMo = UITheme.createButton("Khóa / Mở Thẻ", new Color(124, 58, 237), 130, 34);
        btnXoa = UITheme.createButton("Xóa Độc Giả", UITheme.DANGER, 115, 34);
        btnLamMoi = UITheme.createButton("Làm Mới", UITheme.MUTED, 100, 34);
        JButton btnXuatCsv = UITheme.createButton("Xuất CSV", new Color(15, 118, 110), 105, 34);
        btnXuatCsv.addActionListener(e -> CsvExporter.exportTableToCsv(this, table, "danh_sach_doc_gia.csv"));

        panelButtons.add(btnThem);
        panelButtons.add(btnSua);
        panelButtons.add(btnGiaHan);
        panelButtons.add(btnKhoaMo);
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
        String[] columns = {"Mã Độc Giả", "Họ và Tên", "Số Điện Thoại", "Email", "Ngày Hết Hạn", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        UITheme.styleTable(table, scrollPane, 0, 2, 4);
        table.getColumnModel().getColumn(5).setCellRenderer(UITheme.createStatusRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(115);

        add(scrollPane, BorderLayout.CENTER);

        // 6. SỰ KIỆN
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < currentDocGiaList.size()) {
                    DocGia selected = currentDocGiaList.get(row);
                    txtMaDG.setText(String.valueOf(selected.getMaDocGia()));
                    txtHoTen.setText(selected.getHoTen());
                    txtSoDienThoai.setText(selected.getSoDienThoai());
                    txtEmail.setText(selected.getEmail());
                    txtNgayHetHan.setText(selected.getNgayHetHan() != null ? selected.getNgayHetHan().toString() : "");
                    boolean isActive = "HOAT_DONG".equalsIgnoreCase(selected.getTrangThai());
                    cbTrangThai.setSelectedIndex(isActive ? 0 : 1);
                    btnKhoaMo.setText(isActive ? "Khóa Thẻ" : "Mở Khóa Thẻ");
                }
            }
        });

        btnLamMoi.addActionListener(e -> clearForm());
        btnThem.addActionListener(e -> addDocGia());
        btnSua.addActionListener(e -> updateDocGia());
        btnGiaHan.addActionListener(e -> giaHanTheDocGia());
        btnKhoaMo.addActionListener(e -> toggleKhoaMoThe());
        btnXoa.addActionListener(e -> deleteDocGia());
    }

    private void loadDataToTable(List<DocGia> list) {
        tableModel.setRowCount(0);
        currentDocGiaList = (list != null) ? new ArrayList<>(list) : new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (DocGia dg : currentDocGiaList) {
            String trangThai;
            if (!"HOAT_DONG".equalsIgnoreCase(dg.getTrangThai())) {
                trangThai = "Khóa (" + (dg.getTrangThai() != null ? dg.getTrangThai() : "N/A") + ")";
            } else if (dg.getNgayHetHan() != null && dg.getNgayHetHan().toLocalDate().isBefore(today)) {
                trangThai = "Hết hạn";
            } else {
                trangThai = "Hoạt động";
            }
            tableModel.addRow(new Object[]{
                dg.getMaDocGia(), dg.getHoTen(), dg.getSoDienThoai(), dg.getEmail(),
                dg.getNgayHetHan(), trangThai
            });
        }
    }

    private void clearForm() {
        txtMaDG.setText("");
        txtHoTen.setText("");
        txtSoDienThoai.setText("");
        txtEmail.setText("");
        txtNgayHetHan.setText("");
        cbTrangThai.setSelectedIndex(0);
        btnKhoaMo.setText("Khóa / Mở Thẻ");
        txtSearch.setText("");
        table.clearSelection();
        loadAllDocGiaSafe();
    }

    private DocGia buildValidatedDocGia(int maDG) {
        String hoTen = txtHoTen.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();
        String email = txtEmail.getText().trim();
        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ và tên độc giả!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (!ValidationUtils.isValidPhone(sdt)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 8 đến 15 chữ số!", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (!email.isEmpty() && !ValidationUtils.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Địa chỉ email không hợp lệ!", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Date ngayHetHan;
        try {
            ngayHetHan = ValidationUtils.parseSqlDate(txtNgayHetHan.getText());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String trangThai = cbTrangThai.getSelectedIndex() == 1 ? "KHOA" : "HOAT_DONG";
        return new DocGia(maDG, hoTen, sdt, email, null, ngayHetHan, trangThai);
    }

    private void addDocGia() {
        DocGia dg = buildValidatedDocGia(0);
        if (dg == null) {
            return;
        }
        try {
            if (docGiaDAO.insertDocGia(dg)) {
                JOptionPane.showMessageDialog(this, "Thêm độc giả thành công!");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm độc giả thất bại!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi cơ sở dữ liệu khi thêm độc giả"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDocGia() {
        if (txtMaDG.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn 1 độc giả trên bảng để sửa!");
            return;
        }
        int maDG = Integer.parseInt(txtMaDG.getText().trim());
        DocGia dg = buildValidatedDocGia(maDG);
        if (dg == null) {
            return;
        }
        try {
            if (docGiaDAO.updateDocGia(dg)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi cơ sở dữ liệu khi cập nhật độc giả"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void giaHanTheDocGia() {
        if (txtMaDG.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn 1 độc giả trên bảng để gia hạn thẻ!");
            return;
        }
        int maDG = Integer.parseInt(txtMaDG.getText().trim());
        int confirm = JOptionPane.showConfirmDialog(this,
                "Gia hạn thẻ độc giả mã " + maDG + " thêm 1 năm (và kích hoạt lại nếu đang khóa/hết hạn)?",
                "Xác nhận gia hạn", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (docGiaDAO.giaHanThe(maDG, 1)) {
                    JOptionPane.showMessageDialog(this, "Gia hạn thẻ thêm 1 năm thành công!");
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Gia hạn thẻ thất bại!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        ValidationUtils.friendlySqlError(ex, "Lỗi gia hạn thẻ độc giả"),
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleKhoaMoThe() {
        if (txtMaDG.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn 1 độc giả trên bảng để khóa hoặc mở khóa thẻ!");
            return;
        }
        int row = table.getSelectedRow();
        String currentStatus = (row >= 0 && row < currentDocGiaList.size())
                ? currentDocGiaList.get(row).getTrangThai()
                : (cbTrangThai.getSelectedIndex() == 0 ? "HOAT_DONG" : "KHOA");
        boolean isActive = "HOAT_DONG".equalsIgnoreCase(currentStatus);
        String targetStatus = isActive ? "KHOA" : "HOAT_DONG";
        String actionLabel = isActive ? "khóa" : "mở khóa";

        int maDG = Integer.parseInt(txtMaDG.getText().trim());
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn " + actionLabel + " thẻ độc giả mã " + maDG + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (docGiaDAO.capNhatTrangThai(maDG, targetStatus)) {
                    JOptionPane.showMessageDialog(this, "Đã " + actionLabel + " thẻ độc giả thành công!");
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Thao tác thất bại!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        ValidationUtils.friendlySqlError(ex, "Lỗi cập nhật trạng thái thẻ"),
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteDocGia() {
        if (txtMaDG.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn 1 độc giả trên bảng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa độc giả này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int maDG = Integer.parseInt(txtMaDG.getText().trim());
                if (docGiaDAO.deleteDocGia(maDG)) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        ValidationUtils.friendlySqlError(ex, "Không thể xóa độc giả"),
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

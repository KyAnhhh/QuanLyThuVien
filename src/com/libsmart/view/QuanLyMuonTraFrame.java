package com.libsmart.view;

import com.libsmart.dao.DocGiaDAO;
import com.libsmart.dao.MuonTraDAO;
import com.libsmart.dao.SachDAO;
import com.libsmart.model.DocGia;
import com.libsmart.model.NguoiDung;
import com.libsmart.model.Sach;
import com.libsmart.utils.UITheme;
import java.awt.BorderLayout;
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
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class QuanLyMuonTraFrame extends JFrame {

    private JComboBox<String> cbDocGia, cbSach;
    private JTextField txtSoNgayMuon, txtMaPhieu, txtSearch;
    private JButton btnChoMuon, btnXacNhanTra, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;

    private MuonTraDAO muonTraDAO;
    private DocGiaDAO docGiaDAO;
    private SachDAO sachDAO;
    private final NguoiDung currentUser;
    private final Runnable onDataChanged;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public QuanLyMuonTraFrame() {
        this(null, null);
    }

    public QuanLyMuonTraFrame(NguoiDung currentUser) {
        this(currentUser, null);
    }

    public QuanLyMuonTraFrame(NguoiDung currentUser, Runnable onDataChanged) {
        this.currentUser = currentUser;
        this.onDataChanged = onDataChanged;
        muonTraDAO = new MuonTraDAO();
        docGiaDAO = new DocGiaDAO();
        sachDAO = new SachDAO();

        initComponents();
        loadDataToCB();
        loadDataToTable(muonTraDAO.getAllPhieuMuon());
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private void initComponents() {
        setTitle("LIB-SMART | Quản lý mượn - trả sách");
        setSize(1060, 700);
        setMinimumSize(new Dimension(920, 620));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelMain = new JPanel(new BorderLayout(14, 14));
        panelMain.setBackground(UITheme.BG_APP);
        panelMain.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        // 1. TIÊU ĐỀ
        JLabel lblTitle = new JLabel("QUẢN LÝ MƯỢN TRẢ SÁCH", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);

        // 2. TÌM KIẾM
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        panelSearch.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm phiếu mượn:");
        lblSearch.setFont(UITheme.FONT_LABEL);
        lblSearch.setForeground(UITheme.TEXT_PRIMARY);

        txtSearch = UITheme.createTextField(true);
        txtSearch.setPreferredSize(new Dimension(320, 34));

        panelSearch.add(lblSearch);
        panelSearch.add(txtSearch);

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String kw = txtSearch.getText().trim().toLowerCase();
                if (kw.isEmpty()) {
                    loadDataToTable(muonTraDAO.getAllPhieuMuon());
                } else {
                    List<Object[]> allData = muonTraDAO.getAllPhieuMuon();
                    tableModel.setRowCount(0);
                    for (Object[] row : allData) {
                        String docGia = row[1] != null ? row[1].toString().toLowerCase() : "";
                        String sach = row[2] != null ? row[2].toString().toLowerCase() : "";
                        if (docGia.contains(kw) || sach.contains(kw)) {
                            formatAndAddRow(row);
                        }
                    }
                }
            }
        });

        // 3. FORM LẬP PHIẾU
        JPanel panelForm = UITheme.createTitledCard("Lập Phiếu Mượn Sách");
        panelForm.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 12, 7, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbDocGia = new JComboBox<>();
        UITheme.styleComboBox(cbDocGia);

        cbSach = new JComboBox<>();
        UITheme.styleComboBox(cbSach);

        txtSoNgayMuon = UITheme.createTextField(true);
        txtSoNgayMuon.setText("7");

        txtMaPhieu = UITheme.createTextField(false);

        UITheme.addFormCell(panelForm, gbc, "Chọn Độc Giả:", cbDocGia, 0, 0);
        UITheme.addFormCell(panelForm, gbc, "Chọn Sách Mượn:", cbSach, 2, 0);
        UITheme.addFormCell(panelForm, gbc, "Số Ngày Mượn:", txtSoNgayMuon, 0, 1);
        UITheme.addFormCell(panelForm, gbc, "Mã Phiếu Đang Chọn:", txtMaPhieu, 2, 1);

        // 4. BỘ NÚT BẤM
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        panelButtons.setOpaque(false);

        btnChoMuon = UITheme.createButton("Cho Mượn Sách", UITheme.SUCCESS, 165, 36);
        btnXacNhanTra = UITheme.createButton("Xác Nhận Trả Sách", UITheme.PRIMARY, 175, 36);
        btnLamMoi = UITheme.createButton("Làm Mới", UITheme.MUTED, 130, 36);

        panelButtons.add(btnChoMuon);
        panelButtons.add(btnXacNhanTra);
        panelButtons.add(btnLamMoi);

        JPanel panelTopHeader = new JPanel(new BorderLayout(8, 8));
        panelTopHeader.setOpaque(false);
        panelTopHeader.add(lblTitle, BorderLayout.NORTH);
        panelTopHeader.add(panelSearch, BorderLayout.CENTER);

        JPanel panelCenterTop = new JPanel(new BorderLayout(8, 8));
        panelCenterTop.setOpaque(false);
        panelCenterTop.add(panelForm, BorderLayout.CENTER);
        panelCenterTop.add(panelButtons, BorderLayout.SOUTH);

        JPanel panelHeaderContainer = new JPanel(new BorderLayout(10, 10));
        panelHeaderContainer.setOpaque(false);
        panelHeaderContainer.add(panelTopHeader, BorderLayout.NORTH);
        panelHeaderContainer.add(panelCenterTop, BorderLayout.SOUTH);

        panelMain.add(panelHeaderContainer, BorderLayout.NORTH);

        // 5. BẢNG JTABLE
        String[] columns = {"Mã Phiếu", "Độc Giả", "Sách Mượn", "Ngày Mượn", "Hẹn Trả", "Trạng Thái", "Tiền Phạt"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        UITheme.styleTable(table, scrollPane, 0, 3, 4);
        table.getColumnModel().getColumn(5).setCellRenderer(UITheme.createStatusRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(UITheme.createStripedRenderer(SwingConstants.RIGHT));
        table.getColumnModel().getColumn(0).setPreferredWidth(75);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(230);
        table.getColumnModel().getColumn(3).setPreferredWidth(135);
        table.getColumnModel().getColumn(4).setPreferredWidth(105);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(105);

        panelMain.add(scrollPane, BorderLayout.CENTER);
        add(panelMain);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtMaPhieu.setText(getVal(row, 0));
                }
            }
        });

        btnLamMoi.addActionListener(e -> clearForm());
        btnChoMuon.addActionListener(e -> choMuonSach());
        btnXacNhanTra.addActionListener(e -> traSach());
    }

    private String getVal(int row, int col) {
        Object obj = tableModel.getValueAt(row, col);
        return obj != null ? obj.toString() : "";
    }

    private void loadDataToCB() {
        cbDocGia.removeAllItems();
        List<DocGia> listDG = docGiaDAO.getAllDocGia();
        LocalDate today = LocalDate.now();
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

    private void formatAndAddRow(Object[] row) {
        Object[] formattedRow = new Object[row.length];
        System.arraycopy(row, 0, formattedRow, 0, row.length);

        // Định dạng Ngày Mượn gọn gàng (không dính ms)
        if (row[3] instanceof java.util.Date) {
            formattedRow[3] = sdf.format((java.util.Date) row[3]);
        }

        // Chuyển đổi mã Trạng Thái sang Tiếng Việt có dấu (nhận diện Quá hạn nếu đang mượn)
        if (row[5] != null) {
            String tt = row[5].toString();
            if (tt.equalsIgnoreCase("DANG_MUON")) {
                boolean quaHan = (row[4] instanceof java.sql.Date)
                        && ((java.sql.Date) row[4]).toLocalDate().isBefore(LocalDate.now());
                formattedRow[5] = quaHan ? "Quá hạn" : "Đang mượn";
            } else if (tt.equalsIgnoreCase("DA_TRA")) {
                formattedRow[5] = "Đã trả";
            }
        }

        // Định dạng Tiền Phạt nếu có cột tiền phạt
        if (row.length > 6 && row[6] instanceof BigDecimal) {
            formattedRow[6] = UITheme.formatCurrency((BigDecimal) row[6]);
        }

        tableModel.addRow(formattedRow);
    }

    private void loadDataToTable(List<Object[]> list) {
        tableModel.setRowCount(0);
        if (list != null) {
            for (Object[] row : list) {
                formatAndAddRow(row);
            }
        }
    }

    private void clearForm() {
        txtMaPhieu.setText("");
        txtSoNgayMuon.setText("7");
        txtSearch.setText("");
        table.clearSelection();
        loadDataToCB();
        loadDataToTable(muonTraDAO.getAllPhieuMuon());
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
            int maSach = Integer.parseInt(cbSach.getSelectedItem().toString().split(" - ")[0]);
            int soNgay = Integer.parseInt(txtSoNgayMuon.getText().trim());

            MuonTraDAO.KetQuaMuon kq = muonTraDAO.taoPhieuMuonChiTiet(
                    maDG, maSach, soNgay, currentUser.getMaNguoiDung());
            if (kq.isThanhCong()) {
                JOptionPane.showMessageDialog(this, kq.getThongBao());
                clearForm();
                notifyDataChanged();
            } else {
                JOptionPane.showMessageDialog(this, kq.getThongBao(), "Không thể lập phiếu mượn", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số ngày mượn phải là số nguyên hợp lệ!");
        }
    }

    private void traSach() {
        if (txtMaPhieu.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 phiếu mượn trên bảng để xác nhận trả!");
            return;
        }

        int maPhieu = Integer.parseInt(txtMaPhieu.getText().trim());
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận trả sách cho phiếu " + maPhieu + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            MuonTraDAO.KetQuaTra kq = muonTraDAO.traSachChiTiet(maPhieu);
            if (kq.isThanhCong()) {
                int msgType = kq.getSoNgayTre() > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
                JOptionPane.showMessageDialog(this, kq.getThongBao(), "Kết quả trả sách", msgType);
                clearForm();
                notifyDataChanged();
            } else {
                JOptionPane.showMessageDialog(this, kq.getThongBao(), "Trả sách thất bại", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        new QuanLyMuonTraFrame().setVisible(true);
    }
}



package com.libsmart.view;

import com.libsmart.dao.DocGiaDAO;
import com.libsmart.model.DocGia;
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
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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

public class QuanLyDocGiaFrame extends JFrame {

    private JTextField txtMaDG, txtHoTen, txtSoDienThoai, txtEmail, txtSearch;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;
    private DocGiaDAO docGiaDAO;
    private final Runnable onDataChanged;

    public QuanLyDocGiaFrame() {
        this(null);
    }

    public QuanLyDocGiaFrame(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
        docGiaDAO = new DocGiaDAO();
        initComponents();
        loadDataToTable(docGiaDAO.getAllDocGia());
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private void initComponents() {
        setTitle("LIB-SMART | Quản lý độc giả");
        setSize(980, 640);
        setMinimumSize(new Dimension(860, 560));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelMain = new JPanel(new BorderLayout(14, 14));
        panelMain.setBackground(UITheme.BG_APP);
        panelMain.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        // 1. TIÊU ĐỀ
        JLabel lblTitle = new JLabel("QUẢN LÝ DANH MỤC ĐỘC GIẢ", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);

        // 2. Ô TÌM KIẾM
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        panelSearch.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm độc giả:");
        lblSearch.setFont(UITheme.FONT_LABEL);
        lblSearch.setForeground(UITheme.TEXT_PRIMARY);

        txtSearch = UITheme.createTextField(true);
        txtSearch.setPreferredSize(new Dimension(320, 34));

        panelSearch.add(lblSearch);
        panelSearch.add(txtSearch);

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String kw = txtSearch.getText().trim();
                if (kw.isEmpty()) {
                    loadDataToTable(docGiaDAO.getAllDocGia());
                } else {
                    loadDataToTable(docGiaDAO.searchDocGiaByTen(kw));
                }
            }
        });

        // 3. FORM NHẬP LIỆU
        JPanel panelForm = UITheme.createTitledCard("Thông Tin Độc Giả");
        panelForm.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMaDG = UITheme.createTextField(false);
        txtHoTen = UITheme.createTextField(true);
        txtSoDienThoai = UITheme.createTextField(true);
        txtEmail = UITheme.createTextField(true);

        UITheme.addFormCell(panelForm, gbc, "Mã Độc Giả:", txtMaDG, 0, 0);
        UITheme.addFormCell(panelForm, gbc, "Họ và Tên:", txtHoTen, 2, 0);
        UITheme.addFormCell(panelForm, gbc, "Số Điện Thoại:", txtSoDienThoai, 0, 1);
        UITheme.addFormCell(panelForm, gbc, "Email:", txtEmail, 2, 1);

        // 4. BỘ NÚT BẤM
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        panelButtons.setOpaque(false);

        btnThem = UITheme.createButton("Thêm Độc Giả", UITheme.SUCCESS, 140, 36);
        btnSua = UITheme.createButton("Sửa Độc Giả", UITheme.PRIMARY, 140, 36);
        btnXoa = UITheme.createButton("Xóa Độc Giả", UITheme.DANGER, 140, 36);
        btnLamMoi = UITheme.createButton("Làm Mới", UITheme.MUTED, 130, 36);

        panelButtons.add(btnThem);
        panelButtons.add(btnSua);
        panelButtons.add(btnXoa);
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
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(210);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        panelMain.add(scrollPane, BorderLayout.CENTER);
        add(panelMain);

        // 6. SỰ KIỆN CLICK BẢNG & BẤM NÚT
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtMaDG.setText(getVal(row, 0));
                    txtHoTen.setText(getVal(row, 1));
                    txtSoDienThoai.setText(getVal(row, 2));
                    txtEmail.setText(getVal(row, 3));
                }
            }
        });

        btnLamMoi.addActionListener(e -> clearForm());
        btnThem.addActionListener(e -> addDocGia());
        btnSua.addActionListener(e -> updateDocGia());
        btnXoa.addActionListener(e -> deleteDocGia());
    }

    private String getVal(int row, int col) {
        Object obj = tableModel.getValueAt(row, col);
        return obj != null ? obj.toString() : "";
    }

    private void loadDataToTable(List<DocGia> list) {
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        if (list != null) {
            for (DocGia dg : list) {
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
    }

    private void clearForm() {
        txtMaDG.setText("");
        txtHoTen.setText("");
        txtSoDienThoai.setText("");
        txtEmail.setText("");
        txtSearch.setText("");
        table.clearSelection();
        loadDataToTable(docGiaDAO.getAllDocGia());
    }

    private void addDocGia() {
        if (txtHoTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ và tên độc giả!");
            return;
        }
        DocGia dg = new DocGia(0, txtHoTen.getText().trim(), txtSoDienThoai.getText().trim(), txtEmail.getText().trim(), null, null, "HOAT_DONG");
        if (docGiaDAO.insertDocGia(dg)) {
            JOptionPane.showMessageDialog(this, "Thêm độc giả thành công!");
            clearForm();
            notifyDataChanged();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm độc giả thất bại!");
        }
    }

    private void updateDocGia() {
        if (txtMaDG.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn 1 độc giả trên bảng để sửa!");
            return;
        }
        try {
            int maDG = Integer.parseInt(txtMaDG.getText().trim());
            DocGia dg = new DocGia(maDG, txtHoTen.getText().trim(), txtSoDienThoai.getText().trim(), txtEmail.getText().trim(), null, null, null);
            if (docGiaDAO.updateDocGia(dg)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                clearForm();
                notifyDataChanged();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Mã độc giả không hợp lệ!");
        }
    }

    private void deleteDocGia() {
        if (txtMaDG.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn 1 độc giả trên bảng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa độc giả này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int maDG = Integer.parseInt(txtMaDG.getText().trim());
            if (docGiaDAO.deleteDocGia(maDG)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                clearForm();
                notifyDataChanged();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        new QuanLyDocGiaFrame().setVisible(true);
    }
}
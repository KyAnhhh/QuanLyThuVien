package com.libsmart.view;

import com.libsmart.dao.SachDAO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

public class QuanLySachFrame extends JFrame {

    private JTextField txtMaSach, txtTenSach, txtNamXB, txtSoLuongTong, txtSoLuongCon, txtGiaBia, txtSearch;
    private JComboBox<String> cbTacGia, cbTheLoai;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;
    private SachDAO sachDAO;
    private final Runnable onDataChanged;
    private Map<Integer, String> mapTacGia = new TreeMap<>();
    private Map<Integer, String> mapTheLoai = new TreeMap<>();
    private List<Sach> currentSachList = new ArrayList<>();

    public QuanLySachFrame() {
        this(null);
    }

    public QuanLySachFrame(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
        sachDAO = new SachDAO();
        initComponents();
        loadDanhMucComboBox();
        loadAllSachSafe();
    }

    private void loadDanhMucComboBox() {
        cbTacGia.removeAllItems();
        cbTheLoai.removeAllItems();
        try {
            mapTacGia = new TreeMap<>(sachDAO.getMapTacGia());
            for (Map.Entry<Integer, String> entry : mapTacGia.entrySet()) {
                cbTacGia.addItem(entry.getKey() + " - " + entry.getValue());
            }

            mapTheLoai = new TreeMap<>(sachDAO.getMapTheLoai());
            for (Map.Entry<Integer, String> entry : mapTheLoai.entrySet()) {
                cbTheLoai.addItem(entry.getKey() + " - " + entry.getValue());
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

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private void loadAllSachSafe() {
        try {
            loadDataToTable(sachDAO.getAllSach());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh sách sách:\n" + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setTitle("LIB-SMART | Quản lý danh mục sách");
        setSize(1040, 700);
        setMinimumSize(new Dimension(900, 620));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelMain = new JPanel(new BorderLayout(14, 14));
        panelMain.setBackground(UITheme.BG_APP);
        panelMain.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        // 1. TIÊU ĐỀ
        JLabel lblTitle = new JLabel("QUẢN LÝ DANH MỤC SÁCH", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);

        // 2. TÌM KIẾM
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        panelSearch.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm tên sách:");
        lblSearch.setFont(UITheme.FONT_LABEL);
        lblSearch.setForeground(UITheme.TEXT_PRIMARY);

        txtSearch = UITheme.createTextField(true);
        txtSearch.setPreferredSize(new Dimension(340, 34));

        panelSearch.add(lblSearch);
        panelSearch.add(txtSearch);

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String kw = txtSearch.getText().trim();
                try {
                    if (kw.isEmpty()) {
                        loadDataToTable(sachDAO.getAllSach());
                    } else {
                        loadDataToTable(sachDAO.searchSachByTen(kw));
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(QuanLySachFrame.this,
                            "Lỗi tìm kiếm sách:\n" + ex.getMessage(),
                            "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 3. FORM NHẬP LIỆU
        JPanel panelForm = UITheme.createTitledCard("Thông Tin Sách");
        panelForm.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 12, 7, 12);
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
        UITheme.addFormCell(panelForm, gbc, "Tác Giả:", cbTacGia, 0, 1);
        UITheme.addFormCell(panelForm, gbc, "Thể Loại:", cbTheLoai, 2, 1);
        UITheme.addFormCell(panelForm, gbc, "Năm Xuất Bản:", txtNamXB, 0, 2);
        UITheme.addFormCell(panelForm, gbc, "Số Lượng Tổng:", txtSoLuongTong, 2, 2);
        UITheme.addFormCell(panelForm, gbc, "Số Lượng Còn:", txtSoLuongCon, 0, 3);
        UITheme.addFormCell(panelForm, gbc, "Giá Bìa (VNĐ):", txtGiaBia, 2, 3);

        // 4. BỘ NÚT BẤM
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        panelButtons.setOpaque(false);

        btnThem = UITheme.createButton("Thêm Sách", UITheme.SUCCESS, 130, 36);
        btnSua = UITheme.createButton("Sửa Sách", UITheme.PRIMARY, 130, 36);
        btnXoa = UITheme.createButton("Xóa Sách", UITheme.DANGER, 130, 36);
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
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(75);
        table.getColumnModel().getColumn(6).setPreferredWidth(75);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);

        panelMain.add(scrollPane, BorderLayout.CENTER);
        add(panelMain);

        // 6. SỰ KIỆN CLICK BẢNG & NÚT BẤM
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
        table.clearSelection();
        loadDanhMucComboBox();
        loadAllSachSafe();
    }

    private void addSach() {
        if (txtTenSach.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sách!");
            return;
        }
        if (cbTacGia.getSelectedItem() == null || cbTheLoai.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Tác giả và Thể loại!");
            return;
        }
        try {
            int maTG = Integer.parseInt(cbTacGia.getSelectedItem().toString().split(" - ")[0]);
            int maTL = Integer.parseInt(cbTheLoai.getSelectedItem().toString().split(" - ")[0]);
            int namXB = Integer.parseInt(txtNamXB.getText().trim());
            int soLuong = Integer.parseInt(txtSoLuongTong.getText().trim());
            BigDecimal giaBia = new BigDecimal(txtGiaBia.getText().trim());

            Sach s = new Sach(0, txtTenSach.getText().trim(), maTG, maTL, namXB, soLuong, soLuong, giaBia);
            if (sachDAO.insertSach(s)) {
                JOptionPane.showMessageDialog(this, "Thêm sách thành công!");
                clearForm();
                notifyDataChanged();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ cho Năm, Số lượng và Giá!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu khi thêm sách:\n" + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSach() {
        if (txtMaSach.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn 1 dòng để sửa!");
            return;
        }
        if (cbTacGia.getSelectedItem() == null || cbTheLoai.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Tác giả và Thể loại!");
            return;
        }
        try {
            int maSach = Integer.parseInt(txtMaSach.getText().trim());
            int maTG = Integer.parseInt(cbTacGia.getSelectedItem().toString().split(" - ")[0]);
            int maTL = Integer.parseInt(cbTheLoai.getSelectedItem().toString().split(" - ")[0]);
            int namXB = Integer.parseInt(txtNamXB.getText().trim());
            int soLuong = Integer.parseInt(txtSoLuongTong.getText().trim());
            int soLuongCon = Integer.parseInt(txtSoLuongCon.getText().trim());
            BigDecimal giaBia = new BigDecimal(txtGiaBia.getText().trim());

            Sach s = new Sach(maSach, txtTenSach.getText().trim(), maTG, maTL, namXB, soLuong, soLuongCon, giaBia);
            if (sachDAO.updateSach(s)) {
                JOptionPane.showMessageDialog(this, "Sửa thành công!");
                clearForm();
                notifyDataChanged();
            } else {
                JOptionPane.showMessageDialog(this, "Sửa thất bại!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi nhập liệu!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu khi cập nhật sách:\n" + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
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
                    notifyDataChanged();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa sách (có thể sách đang được tham chiếu trong phiếu mượn):\n" + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        new QuanLySachFrame().setVisible(true);
    }
}
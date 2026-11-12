package com.libsmart.view;

import com.libsmart.dao.NguoiDungDAO;
import com.libsmart.model.NguoiDung;
import com.libsmart.utils.UITheme;
import com.libsmart.utils.ValidationUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình quản trị tài khoản Người dùng / Thủ thư (chỉ dành cho ADMIN).
 */
public class QuanLyTaiKhoanFrame extends JFrame {

    private JTextField txtMaND, txtTenDangNhap, txtHoTen;
    private JPasswordField txtMatKhau;
    private JComboBox<String> cbVaiTro;
    private JButton btnThem, btnSua, btnResetPass, btnKhoaMo, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;

    private final NguoiDungDAO nguoiDungDAO;
    private final NguoiDung currentUser;
    private List<NguoiDung> currentList = new ArrayList<>();

    public QuanLyTaiKhoanFrame(NguoiDung currentUser) {
        this.currentUser = currentUser;
        this.nguoiDungDAO = new NguoiDungDAO();
        initComponents();
        loadDataSafe();
    }

    private void initComponents() {
        setTitle("LIB-SMART | Quản lý tài khoản hệ thống");
        setSize(980, 640);
        setMinimumSize(new Dimension(860, 560));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelMain = new JPanel(new BorderLayout(14, 14));
        panelMain.setBackground(UITheme.BG_APP);
        panelMain.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        JLabel lblTitle = new JLabel("QUẢN LÝ TÀI KHOẢN THỦ THƯ & QUẢN TRỊ", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);

        JPanel panelForm = UITheme.createTitledCard("Thông Tin Tài Khoản");
        panelForm.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 12, 7, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMaND = UITheme.createTextField(false);
        txtTenDangNhap = UITheme.createTextField(true);
        txtHoTen = UITheme.createTextField(true);
        txtMatKhau = new JPasswordField();
        UITheme.styleTextField(txtMatKhau, true);
        txtMatKhau.setPreferredSize(new Dimension(200, 32));
        txtMatKhau.setToolTipText("Chỉ dùng khi tạo tài khoản mới; muốn đổi mật khẩu tài khoản có sẵn dùng nút Đặt Lại MK");

        cbVaiTro = new JComboBox<>(new String[]{"THU_THU", "ADMIN"});
        UITheme.styleComboBox(cbVaiTro);

        UITheme.addFormCell(panelForm, gbc, "Mã Người Dùng:", txtMaND, 0, 0);
        UITheme.addFormCell(panelForm, gbc, "Tên Đăng Nhập:", txtTenDangNhap, 2, 0);
        UITheme.addFormCell(panelForm, gbc, "Họ và Tên:", txtHoTen, 0, 1);
        UITheme.addFormCell(panelForm, gbc, "Vai Trò:", cbVaiTro, 2, 1);
        UITheme.addFormCell(panelForm, gbc, "Mật Khẩu (Tạo mới):", txtMatKhau, 0, 2);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        panelButtons.setOpaque(false);

        btnThem = UITheme.createButton("Thêm Tài Khoản", UITheme.SUCCESS, 145, 36);
        btnSua = UITheme.createButton("Cập Nhật", UITheme.PRIMARY, 120, 36);
        btnResetPass = UITheme.createButton("Đặt Lại MK", UITheme.WARNING, 125, 36);
        btnKhoaMo = UITheme.createButton("Khóa / Mở TK", new Color(124, 58, 237), 140, 36);
        btnLamMoi = UITheme.createButton("Làm Mới", UITheme.MUTED, 115, 36);

        panelButtons.add(btnThem);
        panelButtons.add(btnSua);
        panelButtons.add(btnResetPass);
        panelButtons.add(btnKhoaMo);
        panelButtons.add(btnLamMoi);

        JPanel panelTop = new JPanel(new BorderLayout(8, 8));
        panelTop.setOpaque(false);
        panelTop.add(lblTitle, BorderLayout.NORTH);
        panelTop.add(panelForm, BorderLayout.CENTER);
        panelTop.add(panelButtons, BorderLayout.SOUTH);

        panelMain.add(panelTop, BorderLayout.NORTH);

        String[] columns = {"Mã ND", "Tên Đăng Nhập", "Họ và Tên", "Vai Trò", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        UITheme.styleTable(table, scrollPane, 0, 3);
        table.getColumnModel().getColumn(4).setCellRenderer(UITheme.createStatusRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);

        panelMain.add(scrollPane, BorderLayout.CENTER);
        add(panelMain);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < currentList.size()) {
                    NguoiDung nd = currentList.get(row);
                    txtMaND.setText(String.valueOf(nd.getMaNguoiDung()));
                    txtTenDangNhap.setText(nd.getTenDangNhap());
                    txtTenDangNhap.setEditable(false);
                    txtTenDangNhap.setBackground(UITheme.BG_INPUT_READONLY);
                    txtHoTen.setText(nd.getHoTen());
                    cbVaiTro.setSelectedItem(nd.getVaiTro());
                    txtMatKhau.setText("");
                    btnKhoaMo.setText(nd.isTrangThai() ? "Khóa Tài Khoản" : "Mở Tài Khoản");
                }
            }
        });

        btnLamMoi.addActionListener(e -> clearForm());
        btnThem.addActionListener(e -> addAccount());
        btnSua.addActionListener(e -> updateAccount());
        btnResetPass.addActionListener(e -> resetPassword());
        btnKhoaMo.addActionListener(e -> toggleStatus());
    }

    private void loadDataSafe() {
        tableModel.setRowCount(0);
        try {
            currentList = nguoiDungDAO.getAllNguoiDung();
            for (NguoiDung nd : currentList) {
                tableModel.addRow(new Object[]{
                    nd.getMaNguoiDung(),
                    nd.getTenDangNhap(),
                    nd.getHoTen(),
                    nd.getVaiTro(),
                    nd.isTrangThai() ? "Hoạt động" : "Khóa"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi tải danh sách tài khoản"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtMaND.setText("");
        txtTenDangNhap.setText("");
        txtTenDangNhap.setEditable(true);
        txtTenDangNhap.setBackground(Color.WHITE);
        txtHoTen.setText("");
        txtMatKhau.setText("");
        cbVaiTro.setSelectedIndex(0);
        btnKhoaMo.setText("Khóa / Mở TK");
        table.clearSelection();
        loadDataSafe();
    }

    private void addAccount() {
        String username = txtTenDangNhap.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String pass = new String(txtMatKhau.getPassword()).trim();
        String vaiTro = String.valueOf(cbVaiTro.getSelectedItem());

        if (username.isEmpty() || hoTen.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên đăng nhập, Họ tên và Mật khẩu!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 4 ký tự!", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        NguoiDung nd = new NguoiDung(0, username, pass, hoTen, vaiTro, true);
        try {
            if (nguoiDungDAO.insertNguoiDung(nd)) {
                JOptionPane.showMessageDialog(this, "Tạo tài khoản thành công!");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Tạo tài khoản thất bại!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi tạo tài khoản"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAccount() {
        if (txtMaND.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 tài khoản trên bảng để cập nhật!");
            return;
        }
        String hoTen = txtHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được để trống!");
            return;
        }
        int maND = Integer.parseInt(txtMaND.getText().trim());
        String vaiTro = String.valueOf(cbVaiTro.getSelectedItem());
        NguoiDung nd = new NguoiDung(maND, txtTenDangNhap.getText().trim(), "", hoTen, vaiTro, true);

        try {
            if (nguoiDungDAO.updateNguoiDung(nd)) {
                JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thành công!");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi cập nhật tài khoản"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetPassword() {
        if (txtMaND.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 tài khoản trên bảng để đặt lại mật khẩu!");
            return;
        }
        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(this, pf,
                "Nhập mật khẩu mới cho " + txtTenDangNhap.getText() + ":",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        String newPass = new String(pf.getPassword()).trim();
        if (newPass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 4 ký tự!", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maND = Integer.parseInt(txtMaND.getText().trim());
        try {
            if (nguoiDungDAO.resetMatKhau(maND, newPass)) {
                JOptionPane.showMessageDialog(this, "Đã đặt lại mật khẩu thành công!");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Đặt lại mật khẩu thất bại!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi đặt lại mật khẩu"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleStatus() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 tài khoản trên bảng!");
            return;
        }
        NguoiDung selected = currentList.get(row);
        if (currentUser != null && selected.getMaNguoiDung() == currentUser.getMaNguoiDung()) {
            JOptionPane.showMessageDialog(this, "Không thể tự khóa tài khoản đang đăng nhập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean newStatus = !selected.isTrangThai();
        String action = newStatus ? "mở khóa" : "khóa";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận " + action + " tài khoản '" + selected.getTenDangNhap() + "'?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (nguoiDungDAO.capNhatTrangThai(selected.getMaNguoiDung(), newStatus)) {
                    JOptionPane.showMessageDialog(this, "Đã " + action + " tài khoản thành công!");
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Thao tác thất bại!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        ValidationUtils.friendlySqlError(ex, "Lỗi cập nhật trạng thái tài khoản"),
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

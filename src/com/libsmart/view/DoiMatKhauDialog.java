package com.libsmart.view;

import com.libsmart.dao.NguoiDungDAO;
import com.libsmart.model.NguoiDung;
import com.libsmart.utils.UITheme;
import com.libsmart.utils.ValidationUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Hộp thoại đổi mật khẩu cho tài khoản thủ thư / quản trị viên đang đăng nhập.
 */
public class DoiMatKhauDialog extends JDialog {

    private final NguoiDung currentUser;
    private final NguoiDungDAO nguoiDungDAO;
    private JPasswordField txtOldPass, txtNewPass, txtConfirmPass;

    public DoiMatKhauDialog(Frame owner, NguoiDung currentUser) {
        super(owner, "LIB-SMART | Đổi mật khẩu", true);
        this.currentUser = currentUser;
        this.nguoiDungDAO = new NguoiDungDAO();
        initComponents();
    }

    private void initComponents() {
        setSize(440, 290);
        setResizable(false);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(UITheme.BG_APP);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel form = UITheme.createTitledCard("Đổi Mật Khẩu (" + currentUser.getTenDangNhap() + ")");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtOldPass = new JPasswordField();
        txtNewPass = new JPasswordField();
        txtConfirmPass = new JPasswordField();
        UITheme.styleTextField(txtOldPass, true);
        UITheme.styleTextField(txtNewPass, true);
        UITheme.styleTextField(txtConfirmPass, true);
        txtOldPass.setPreferredSize(new Dimension(200, 32));
        txtNewPass.setPreferredSize(new Dimension(200, 32));
        txtConfirmPass.setPreferredSize(new Dimension(200, 32));

        UITheme.addFormCell(form, gbc, "Mật khẩu hiện tại:", txtOldPass, 0, 0);
        UITheme.addFormCell(form, gbc, "Mật khẩu mới:", txtNewPass, 0, 1);
        UITheme.addFormCell(form, gbc, "Xác nhận mật khẩu:", txtConfirmPass, 0, 2);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton btnLuu = UITheme.createButton("Lưu Mật Khẩu", UITheme.PRIMARY, 140, 34);
        JButton btnHuy = UITheme.createButton("Hủy", UITheme.MUTED, 95, 34);
        buttons.add(btnHuy);
        buttons.add(btnLuu);

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> handleChangePassword());
        getRootPane().setDefaultButton(btnLuu);

        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        add(root);
    }

    private void handleChangePassword() {
        String oldPass = new String(txtOldPass.getPassword()).trim();
        String newPass = new String(txtNewPass.getPassword()).trim();
        String confirmPass = new String(txtConfirmPass.getPassword()).trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường mật khẩu!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newPass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 4 ký tự!", "Mật khẩu yếu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Xác nhận mật khẩu mới không khớp!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (nguoiDungDAO.doiMatKhau(currentUser.getMaNguoiDung(), oldPass, newPass)) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không đúng!", "Thất bại", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ValidationUtils.friendlySqlError(ex, "Lỗi đổi mật khẩu"),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }
}

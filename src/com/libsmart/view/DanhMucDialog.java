package com.libsmart.view;

import com.libsmart.dao.SachDAO;
import com.libsmart.utils.UITheme;
import com.libsmart.utils.ValidationUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Hộp thoại quản lý danh mục Tác giả và Thể loại.
 */
public class DanhMucDialog extends JDialog {

    @FunctionalInterface
    private interface MapLoader {
        Map<Integer, String> load() throws SQLException;
    }

    @FunctionalInterface
    private interface ItemInserter {
        int insert(String name) throws SQLException;
    }

    @FunctionalInterface
    private interface ItemUpdater {
        boolean update(int id, String name) throws SQLException;
    }

    @FunctionalInterface
    private interface ItemDeleter {
        boolean delete(int id) throws SQLException;
    }

    private final SachDAO sachDAO;
    private int lastAddedTacGiaId = -1;
    private int lastAddedTheLoaiId = -1;
    private boolean dataChanged = false;

    public DanhMucDialog(Frame owner, SachDAO sachDAO, int initialTabIndex) {
        super(owner, "LIB-SMART | Quản lý Tác giả & Thể loại", true);
        this.sachDAO = sachDAO;
        initComponents(initialTabIndex);
    }

    public boolean isDataChanged() {
        return dataChanged;
    }

    public int getLastAddedTacGiaId() {
        return lastAddedTacGiaId;
    }

    public int getLastAddedTheLoaiId() {
        return lastAddedTheLoaiId;
    }

    private void initComponents(int initialTabIndex) {
        setSize(640, 480);
        setMinimumSize(new Dimension(560, 420));
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_SECTION);

        JPanel tabTacGia = createCatalogTab(
                "Tác Giả",
                sachDAO::getMapTacGia,
                name -> {
                    int id = sachDAO.insertTacGia(name);
                    if (id > 0) {
                        lastAddedTacGiaId = id;
                    }
                    return id;
                },
                sachDAO::updateTacGia,
                sachDAO::deleteTacGia
        );

        JPanel tabTheLoai = createCatalogTab(
                "Thể Loại",
                sachDAO::getMapTheLoai,
                name -> {
                    int id = sachDAO.insertTheLoai(name);
                    if (id > 0) {
                        lastAddedTheLoaiId = id;
                    }
                    return id;
                },
                sachDAO::updateTheLoai,
                sachDAO::deleteTheLoai
        );

        tabbedPane.addTab("Tác Giả", tabTacGia);
        tabbedPane.addTab("Thể Loại", tabTheLoai);
        if (initialTabIndex >= 0 && initialTabIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(initialTabIndex);
        }

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UITheme.BG_APP);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        root.add(tabbedPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        JButton btnDong = UITheme.createButton("Đóng", UITheme.MUTED, 110, 34);
        btnDong.addActionListener(e -> dispose());
        bottom.add(btnDong);
        root.add(bottom, BorderLayout.SOUTH);

        add(root);
    }

    private JPanel createCatalogTab(
            String entityLabel,
            MapLoader loader,
            ItemInserter inserter,
            ItemUpdater updater,
            ItemDeleter deleter) {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_APP);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = UITheme.createTitledCard("Thông Tin " + entityLabel);
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = UITheme.createTextField(false);
        txtId.setPreferredSize(new Dimension(90, 32));
        JTextField txtTen = UITheme.createTextField(true);
        txtTen.setPreferredSize(new Dimension(260, 32));

        UITheme.addFormCell(form, gbc, "Mã:", txtId, 0, 0);
        UITheme.addFormCell(form, gbc, "Tên " + entityLabel + ":", txtTen, 2, 0);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        btnPanel.setOpaque(false);
        JButton btnThem = UITheme.createButton("Thêm", UITheme.SUCCESS, 100, 32);
        JButton btnSua = UITheme.createButton("Sửa", UITheme.PRIMARY, 100, 32);
        JButton btnXoa = UITheme.createButton("Xóa", UITheme.DANGER, 100, 32);
        JButton btnMoi = UITheme.createButton("Làm Mới", UITheme.MUTED, 100, 32);
        btnPanel.add(btnThem);
        btnPanel.add(btnSua);
        btnPanel.add(btnMoi);
        btnPanel.add(btnXoa);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);
        top.add(form, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"Mã", "Tên " + entityLabel};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        UITheme.styleTable(table, scroll, 0);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(420);
        panel.add(scroll, BorderLayout.CENTER);

        Runnable reload = () -> {
            model.setRowCount(0);
            try {
                Map<Integer, String> data = new TreeMap<>(loader.load());
                for (Map.Entry<Integer, String> entry : data.entrySet()) {
                    model.addRow(new Object[]{entry.getKey(), entry.getValue()});
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        ValidationUtils.friendlySqlError(ex, "Lỗi tải danh sách " + entityLabel),
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        };

        Runnable clear = () -> {
            txtId.setText("");
            txtTen.setText("");
            table.clearSelection();
            reload.run();
        };

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtId.setText(String.valueOf(model.getValueAt(row, 0)));
                    txtTen.setText(String.valueOf(model.getValueAt(row, 1)));
                }
            }
        });

        btnMoi.addActionListener(e -> clear.run());

        btnThem.addActionListener(e -> {
            String ten = txtTen.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên " + entityLabel.toLowerCase() + "!");
                return;
            }
            try {
                if (inserter.insert(ten) > 0) {
                    dataChanged = true;
                    JOptionPane.showMessageDialog(this, "Thêm " + entityLabel.toLowerCase() + " thành công!");
                    clear.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm thất bại!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        ValidationUtils.friendlySqlError(ex, "Lỗi thêm " + entityLabel.toLowerCase()),
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnSua.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chọn 1 dòng trên bảng để sửa!");
                return;
            }
            String ten = txtTen.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên " + entityLabel.toLowerCase() + "!");
                return;
            }
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                if (updater.update(id, ten)) {
                    dataChanged = true;
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    clear.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        ValidationUtils.friendlySqlError(ex, "Lỗi cập nhật " + entityLabel.toLowerCase()),
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnXoa.addActionListener(e -> {
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chọn 1 dòng trên bảng để xóa!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa " + entityLabel.toLowerCase() + " này?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int id = Integer.parseInt(txtId.getText().trim());
                    if (deleter.delete(id)) {
                        dataChanged = true;
                        JOptionPane.showMessageDialog(this, "Xóa thành công!");
                        clear.run();
                    } else {
                        JOptionPane.showMessageDialog(this, "Xóa thất bại!");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            ValidationUtils.friendlySqlError(ex, "Không thể xóa " + entityLabel.toLowerCase()),
                            "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        reload.run();
        return panel;
    }
}

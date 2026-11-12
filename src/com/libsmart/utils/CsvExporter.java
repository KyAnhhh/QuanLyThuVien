package com.libsmart.utils;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

/**
 * Tiện ích xuất dữ liệu JTable ra file CSV chuẩn UTF-8 BOM (mở đúng tiếng Việt trên Excel).
 */
public final class CsvExporter {

    private CsvExporter() {}

    public static String escapeCsvCell(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        boolean needsQuotes = text.contains(",") || text.contains("\"")
                || text.contains("\n") || text.contains("\r");
        if (!needsQuotes) {
            return text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    public static void writeTableToFile(JTable table, File file) throws IOException {
        TableModel model = table.getModel();
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {
            // UTF-8 BOM để Excel nhận diện đúng Unicode tiếng Việt
            writer.write('\uFEFF');

            int colCount = model.getColumnCount();
            for (int c = 0; c < colCount; c++) {
                if (c > 0) {
                    writer.write(',');
                }
                writer.write(escapeCsvCell(model.getColumnName(c)));
            }
            writer.newLine();

            int rowCount = model.getRowCount();
            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < colCount; c++) {
                    if (c > 0) {
                        writer.write(',');
                    }
                    writer.write(escapeCsvCell(model.getValueAt(r, c)));
                }
                writer.newLine();
            }
        }
    }

    public static void exportTableToCsv(Component parent, JTable table, String defaultFileName) {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent, "Bảng hiện không có dữ liệu để xuất!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn nơi lưu file CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV UTF-8 (*.csv)", "csv"));
        chooser.setSelectedFile(new File(defaultFileName));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(parent,
                    "File '" + file.getName() + "' đã tồn tại. Bạn có muốn ghi đè?",
                    "Xác nhận ghi đè", JOptionPane.YES_NO_OPTION);
            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            writeTableToFile(table, file);
            JOptionPane.showMessageDialog(parent,
                    "Xuất file CSV thành công!\nĐường dẫn: " + file.getAbsolutePath(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent,
                    "Không thể ghi file CSV:\n" + ex.getMessage(),
                    "Lỗi xuất file", JOptionPane.ERROR_MESSAGE);
        }
    }
}

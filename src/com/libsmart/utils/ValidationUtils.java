package com.libsmart.utils;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Tiện ích kiểm tra dữ liệu đầu vào và chuẩn hóa thông báo lỗi SQL.
 */
public final class ValidationUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{8,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    private ValidationUtils() {}

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Chuyển chuỗi yyyy-MM-dd thành java.sql.Date; ném IllegalArgumentException nếu sai định dạng.
     */
    public static Date parseSqlDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Date.valueOf(LocalDate.parse(text.trim()));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày không đúng định dạng yyyy-MM-dd (ví dụ: 2027-12-31)!");
        }
    }

    public static String friendlySqlError(SQLException ex, String defaultMessage) {
        if (ex == null) {
            return defaultMessage;
        }
        int code = ex.getErrorCode();
        String state = ex.getSQLState();
        if (code == 1062 || ("23000".equals(state) && ex.getMessage() != null && ex.getMessage().toLowerCase().contains("duplicate"))) {
            return "Dữ liệu bị trùng lặp (ví dụ: Số điện thoại hoặc tên đã tồn tại trong hệ thống)!";
        }
        if (code == 1451 || ("23000".equals(state) && ex.getMessage() != null && ex.getMessage().toLowerCase().contains("foreign key"))) {
            return "Không thể xóa vì dữ liệu đang được tham chiếu ở bảng khác (sách hoặc phiếu mượn)!";
        }
        return defaultMessage + "\nChi tiết: " + ex.getMessage();
    }
}

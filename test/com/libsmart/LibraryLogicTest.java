package com.libsmart;

import com.libsmart.dao.MuonTraDAO;
import com.libsmart.utils.CsvExporter;
import com.libsmart.utils.PasswordUtils;
import com.libsmart.utils.ValidationUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Bộ kiểm thử đơn vị cho các nghiệp vụ cốt lõi (không phụ thuộc kết nối DB).
 */
public class LibraryLogicTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.err.println("[FAIL] " + name);
        }
    }

    private static void assertEquals(String name, Object expected, Object actual) {
        boolean eq = (expected == null) ? (actual == null) : expected.equals(actual);
        if (eq) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.err.println("[FAIL] " + name + " | expected=" + expected + ", actual=" + actual);
        }
    }

    private static void testValidationUtils() {
        assertTrue("Phone 9 digits valid", ValidationUtils.isValidPhone("012345678"));
        assertTrue("Phone 10 digits valid", ValidationUtils.isValidPhone("0912345678"));
        assertTrue("Phone with letters invalid", !ValidationUtils.isValidPhone("09123abc78"));
        assertTrue("Phone too short invalid", !ValidationUtils.isValidPhone("1234567"));

        assertTrue("Valid email", ValidationUtils.isValidEmail("thu.thu@libsmart.vn"));
        assertTrue("Invalid email missing @", !ValidationUtils.isValidEmail("thuthu.libsmart.vn"));

        assertEquals("Empty date returns null", null, ValidationUtils.parseSqlDate("  "));
        assertEquals("Valid yyyy-MM-dd parsed", Date.valueOf("2027-12-31"), ValidationUtils.parseSqlDate("2027-12-31"));

        boolean threwBadDate = false;
        try {
            ValidationUtils.parseSqlDate("31/12/2027");
        } catch (IllegalArgumentException ex) {
            threwBadDate = true;
        }
        assertTrue("Invalid date format throws IllegalArgumentException", threwBadDate);

        SQLException dupEx = new SQLException("Duplicate entry", "23000", 1062);
        assertTrue("Friendly duplicate error", ValidationUtils.friendlySqlError(dupEx, "Err").contains("trùng lặp"));

        SQLException fkEx = new SQLException("Cannot delete parent row", "23000", 1451);
        assertTrue("Friendly FK error", ValidationUtils.friendlySqlError(fkEx, "Err").contains("tham chiếu"));
    }

    private static void testPasswordUtils() {
        String hash = PasswordUtils.hashPassword("secret123");
        assertTrue("Hash starts with pbkdf2$", hash.startsWith("pbkdf2$"));
        assertTrue("Verify correct hashed password", PasswordUtils.verifyPassword("secret123", hash));
        assertTrue("Reject wrong hashed password", !PasswordUtils.verifyPassword("wrong", hash));
        assertTrue("Hashed password does not need upgrade", !PasswordUtils.needsUpgrade(hash));

        assertTrue("Legacy plaintext compat verify", PasswordUtils.verifyPassword("admin", "admin"));
        assertTrue("Legacy plaintext flagged for upgrade", PasswordUtils.needsUpgrade("admin"));
    }

    private static void testReaderCardAndFines() {
        LocalDate today = LocalDate.of(2026, 11, 12);

        assertEquals("Active card valid today", null,
                MuonTraDAO.kiemTraTrangThaiTheHopLe("HOAT_DONG", today, today));
        assertEquals("Active card valid future", null,
                MuonTraDAO.kiemTraTrangThaiTheHopLe("HOAT_DONG", today.plusDays(30), today));
        assertTrue("Expired card blocked",
                MuonTraDAO.kiemTraTrangThaiTheHopLe("HOAT_DONG", today.minusDays(1), today) != null);
        assertTrue("Locked card blocked",
                MuonTraDAO.kiemTraTrangThaiTheHopLe("KHOA", today.plusDays(30), today) != null);

        assertEquals("Return on due date = 0 late days", 0L,
                MuonTraDAO.tinhSoNgayTre(today, today));
        assertEquals("Return early = 0 late days", 0L,
                MuonTraDAO.tinhSoNgayTre(today.plusDays(2), today));
        assertEquals("Return 4 days late = 4", 4L,
                MuonTraDAO.tinhSoNgayTre(today.minusDays(4), today));

        assertEquals("0 days late fine = 0", BigDecimal.ZERO, MuonTraDAO.tinhTienPhat(0));
        assertEquals("4 days late fine = 20000", new BigDecimal("20000"), MuonTraDAO.tinhTienPhat(4));
    }

    private static void testCsvExporter() throws IOException {
        assertEquals("Plain text unchanged", "Java", CsvExporter.escapeCsvCell("Java"));
        assertEquals("Comma quoted", "\"A, B\"", CsvExporter.escapeCsvCell("A, B"));
        assertEquals("Quotes doubled", "\"Sách \"\"Hay\"\"\"", CsvExporter.escapeCsvCell("Sách \"Hay\""));
        assertEquals("Null becomes empty", "", CsvExporter.escapeCsvCell(null));

        DefaultTableModel model = new DefaultTableModel(new String[]{"Mã", "Tên Sách"}, 0);
        model.addRow(new Object[]{1, "Lập Trình Java, Cơ Bản"});
        JTable table = new JTable(model);

        Path tempCsv = Files.createTempFile("libsmart_test_", ".csv");
        try {
            CsvExporter.writeTableToFile(table, tempCsv.toFile());
            byte[] bytes = Files.readAllBytes(tempCsv);
            assertTrue("UTF-8 BOM present", bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xEF
                    && (bytes[1] & 0xFF) == 0xBB
                    && (bytes[2] & 0xFF) == 0xBF);
            String content = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
            assertTrue("CSV contains header and escaped row",
                    content.contains("Mã,Tên Sách") && content.contains("1,\"Lập Trình Java, Cơ Bản\""));
        } finally {
            Files.deleteIfExists(tempCsv);
        }
    }

    public static void main(String[] args) throws Exception {
        testValidationUtils();
        testPasswordUtils();
        testReaderCardAndFines();
        testCsvExporter();

        System.out.println("=================================");
        System.out.println("Tests passed: " + passed + ", failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}

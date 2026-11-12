package com.libsmart.dao;

import com.libsmart.model.DocGia;
import com.libsmart.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocGiaDAO {

    private static final Logger LOGGER = Logger.getLogger(DocGiaDAO.class.getName());

    private DocGia mapDocGia(ResultSet rs) throws SQLException {
        return new DocGia(
            rs.getInt("MaDocGia"),
            rs.getString("HoTen"),
            rs.getString("SoDienThoai"),
            rs.getString("Email"),
            rs.getDate("NgayTaoThe"),
            rs.getDate("NgayHetHan"),
            rs.getString("TrangThai")
        );
    }

    // Lấy toàn bộ danh sách Độc Giả từ bảng DocGia
    public List<DocGia> getAllDocGia() throws SQLException {
        List<DocGia> list = new ArrayList<>();
        String sql = "SELECT MaDocGia, HoTen, SoDienThoai, Email, NgayTaoThe, NgayHetHan, TrangThai FROM DocGia ORDER BY MaDocGia";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapDocGia(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách độc giả", e);
            throw e;
        }
        return list;
    }

    // Tìm kiếm độc giả theo tên
    public List<DocGia> searchDocGiaByTen(String keyword) throws SQLException {
        List<DocGia> list = new ArrayList<>();
        String sql = "SELECT MaDocGia, HoTen, SoDienThoai, Email, NgayTaoThe, NgayHetHan, TrangThai FROM DocGia WHERE HoTen LIKE ? ORDER BY MaDocGia";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDocGia(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm kiếm độc giả: " + keyword, e);
            throw e;
        }
        return list;
    }

    // Thêm độc giả mới (Gán mặc định NgayHetHan là 1 năm sau và TrangThai HOAT_DONG nếu chưa truyền)
    public boolean insertDocGia(DocGia dg) throws SQLException {
        boolean hasHetHan = dg.getNgayHetHan() != null;
        String trangThai = (dg.getTrangThai() != null && !dg.getTrangThai().isBlank())
                ? dg.getTrangThai() : "HOAT_DONG";
        String sql = hasHetHan
                ? "INSERT INTO DocGia (HoTen, SoDienThoai, Email, NgayHetHan, TrangThai) VALUES (?, ?, ?, ?, ?)"
                : "INSERT INTO DocGia (HoTen, SoDienThoai, Email, NgayHetHan, TrangThai) VALUES (?, ?, ?, DATE_ADD(CURRENT_DATE, INTERVAL 1 YEAR), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dg.getHoTen());
            ps.setString(2, dg.getSoDienThoai());
            ps.setString(3, dg.getEmail());
            if (hasHetHan) {
                ps.setDate(4, dg.getNgayHetHan());
                ps.setString(5, trangThai);
            } else {
                ps.setString(4, trangThai);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm độc giả: " + dg.getHoTen(), e);
            throw e;
        }
    }

    // Cập nhật thông tin độc giả (giữ nguyên NgayHetHan/TrangThai hiện có nếu model không truyền)
    public boolean updateDocGia(DocGia dg) throws SQLException {
        String sql = "UPDATE DocGia SET HoTen=?, SoDienThoai=?, Email=?, "
                + "NgayHetHan=COALESCE(?, NgayHetHan), TrangThai=COALESCE(?, TrangThai) "
                + "WHERE MaDocGia=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dg.getHoTen());
            ps.setString(2, dg.getSoDienThoai());
            ps.setString(3, dg.getEmail());
            if (dg.getNgayHetHan() != null) {
                ps.setDate(4, dg.getNgayHetHan());
            } else {
                ps.setNull(4, Types.DATE);
            }
            if (dg.getTrangThai() != null && !dg.getTrangThai().isBlank()) {
                ps.setString(5, dg.getTrangThai());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            ps.setInt(6, dg.getMaDocGia());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật độc giả mã: " + dg.getMaDocGia(), e);
            throw e;
        }
    }

    // Gia hạn thẻ độc giả thêm soNam năm tính từ ngày lớn hơn giữa NgayHetHan hiện tại và hôm nay
    public boolean giaHanThe(int maDocGia, int soNam) throws SQLException {
        if (maDocGia <= 0 || soNam <= 0) {
            return false;
        }
        String sql = "UPDATE DocGia SET NgayHetHan = DATE_ADD(GREATEST(COALESCE(NgayHetHan, CURRENT_DATE), CURRENT_DATE), INTERVAL ? YEAR), "
                + "TrangThai = 'HOAT_DONG' WHERE MaDocGia = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soNam);
            ps.setInt(2, maDocGia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi gia hạn thẻ độc giả mã: " + maDocGia, e);
            throw e;
        }
    }

    // Cập nhật trạng thái thẻ độc giả (HOAT_DONG / KHOA)
    public boolean capNhatTrangThai(int maDocGia, String trangThai) throws SQLException {
        String sql = "UPDATE DocGia SET TrangThai = ? WHERE MaDocGia = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, maDocGia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật trạng thái độc giả mã: " + maDocGia, e);
            throw e;
        }
    }

    // Xóa độc giả theo Mã Độc Giả
    public boolean deleteDocGia(int maDocGia) throws SQLException {
        String sql = "DELETE FROM DocGia WHERE MaDocGia=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDocGia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa độc giả mã: " + maDocGia, e);
            throw e;
        }
    }

    // Đếm số độc giả đang hoạt động
    public int countActiveDocGia() throws SQLException {
        String sql = "SELECT COUNT(*) FROM DocGia WHERE TrangThai = 'HOAT_DONG'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm độc giả hoạt động", e);
            throw e;
        }
        return 0;
    }
}
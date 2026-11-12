package com.libsmart.dao;

import com.libsmart.model.DocGia;
import com.libsmart.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocGiaDAO {

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
    public List<DocGia> getAllDocGia() {
        List<DocGia> list = new ArrayList<>();
        String sql = "SELECT MaDocGia, HoTen, SoDienThoai, Email, NgayTaoThe, NgayHetHan, TrangThai FROM DocGia";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapDocGia(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Tìm kiếm độc giả theo tên
    public List<DocGia> searchDocGiaByTen(String keyword) {
        List<DocGia> list = new ArrayList<>();
        String sql = "SELECT MaDocGia, HoTen, SoDienThoai, Email, NgayTaoThe, NgayHetHan, TrangThai FROM DocGia WHERE HoTen LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDocGia(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Thêm độc giả mới (Gán mặc định NgayHetHan là 1 năm sau và TrangThai HOAT_DONG nếu chưa truyền)
    public boolean insertDocGia(DocGia dg) {
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
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thông tin độc giả (giữ nguyên NgayHetHan/TrangThai hiện có nếu model không truyền)
    public boolean updateDocGia(DocGia dg) {
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
            e.printStackTrace();
        }
        return false;
    }

    // Xóa độc giả theo Mã Độc Giả
    public boolean deleteDocGia(int maDocGia) {
        String sql = "DELETE FROM DocGia WHERE MaDocGia=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDocGia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Đếm số độc giả đang hoạt động
    public int countActiveDocGia() {
        String sql = "SELECT COUNT(*) FROM DocGia WHERE TrangThai = 'HOAT_DONG'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
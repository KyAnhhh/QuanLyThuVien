package com.libsmart.dao;

import com.libsmart.model.Sach;
import com.libsmart.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SachDAO {

    private static final Logger LOGGER = Logger.getLogger(SachDAO.class.getName());

    private Sach mapSach(ResultSet rs) throws SQLException {
        return new Sach(
            rs.getInt("MaSach"),
            rs.getString("TenSach"),
            rs.getInt("MaTacGia"),
            rs.getInt("MaTheLoai"),
            rs.getInt("NamXuatBan"),
            rs.getInt("SoLuongTong"),
            rs.getInt("SoLuongCon"),
            rs.getBigDecimal("GiaBia")
        );
    }

    public List<Sach> getAllSach() throws SQLException {
        List<Sach> list = new ArrayList<>();
        String sql = "SELECT MaSach, TenSach, MaTacGia, MaTheLoai, NamXuatBan, SoLuongTong, SoLuongCon, GiaBia FROM Sach";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapSach(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách sách", e);
            throw e;
        }
        return list;
    }

    // Tìm kiếm sách theo tên
    public List<Sach> searchSachByTen(String keyword) throws SQLException {
        List<Sach> list = new ArrayList<>();
        String sql = "SELECT MaSach, TenSach, MaTacGia, MaTheLoai, NamXuatBan, SoLuongTong, SoLuongCon, GiaBia FROM Sach WHERE TenSach LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSach(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm kiếm sách theo tên: " + keyword, e);
            throw e;
        }
        return list;
    }

    // Lấy danh sách Tác giả (Mã -> Tên)
    public Map<Integer, String> getMapTacGia() throws SQLException {
        Map<Integer, String> map = new HashMap<>();
        String sql = "SELECT MaTacGia, TenTacGia FROM TacGia ORDER BY MaTacGia";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("MaTacGia"), rs.getString("TenTacGia"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách tác giả", e);
            throw e;
        }
        return map;
    }

    // Lấy danh sách Thể loại (Mã -> Tên)
    public Map<Integer, String> getMapTheLoai() throws SQLException {
        Map<Integer, String> map = new HashMap<>();
        String sql = "SELECT MaTheLoai, TenTheLoai FROM TheLoai ORDER BY MaTheLoai";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("MaTheLoai"), rs.getString("TenTheLoai"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách thể loại", e);
            throw e;
        }
        return map;
    }

    public boolean insertSach(Sach s) throws SQLException {
        String sql = "INSERT INTO Sach (TenSach, MaTacGia, MaTheLoai, NamXuatBan, SoLuongTong, SoLuongCon, GiaBia) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getTenSach());
            ps.setInt(2, s.getMaTacGia());
            ps.setInt(3, s.getMaTheLoai());
            ps.setInt(4, s.getNamXuatBan());
            ps.setInt(5, s.getSoLuongTong());
            ps.setInt(6, s.getSoLuongCon());
            ps.setBigDecimal(7, s.getGiaBia());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm sách: " + s.getTenSach(), e);
            throw e;
        }
    }

    public boolean updateSach(Sach s) throws SQLException {
        String sql = "UPDATE Sach SET TenSach=?, MaTacGia=?, MaTheLoai=?, NamXuatBan=?, SoLuongTong=?, SoLuongCon=?, GiaBia=? WHERE MaSach=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getTenSach());
            ps.setInt(2, s.getMaTacGia());
            ps.setInt(3, s.getMaTheLoai());
            ps.setInt(4, s.getNamXuatBan());
            ps.setInt(5, s.getSoLuongTong());
            ps.setInt(6, s.getSoLuongCon());
            ps.setBigDecimal(7, s.getGiaBia());
            ps.setInt(8, s.getMaSach());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật sách mã: " + s.getMaSach(), e);
            throw e;
        }
    }

    public boolean deleteSach(int maSach) throws SQLException {
        String sql = "DELETE FROM Sach WHERE MaSach=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maSach);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa sách mã: " + maSach, e);
            throw e;
        }
    }

    private int insertLookup(String sql, String ten, String logLabel) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ten.trim());
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm " + logLabel + ": " + ten, e);
            throw e;
        }
        return -1;
    }

    private boolean updateLookup(String sql, int id, String ten, String logLabel) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ten.trim());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật " + logLabel + " mã: " + id, e);
            throw e;
        }
    }

    private boolean deleteLookup(String sql, int id, String logLabel) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa " + logLabel + " mã: " + id, e);
            throw e;
        }
    }

    public int insertTacGia(String tenTacGia) throws SQLException {
        return insertLookup("INSERT INTO TacGia (TenTacGia) VALUES (?)", tenTacGia, "tác giả");
    }

    public boolean updateTacGia(int maTacGia, String tenTacGia) throws SQLException {
        return updateLookup("UPDATE TacGia SET TenTacGia=? WHERE MaTacGia=?", maTacGia, tenTacGia, "tác giả");
    }

    public boolean deleteTacGia(int maTacGia) throws SQLException {
        return deleteLookup("DELETE FROM TacGia WHERE MaTacGia=?", maTacGia, "tác giả");
    }

    public int insertTheLoai(String tenTheLoai) throws SQLException {
        return insertLookup("INSERT INTO TheLoai (TenTheLoai) VALUES (?)", tenTheLoai, "thể loại");
    }

    public boolean updateTheLoai(int maTheLoai, String tenTheLoai) throws SQLException {
        return updateLookup("UPDATE TheLoai SET TenTheLoai=? WHERE MaTheLoai=?", maTheLoai, tenTheLoai, "thể loại");
    }

    public boolean deleteTheLoai(int maTheLoai) throws SQLException {
        return deleteLookup("DELETE FROM TheLoai WHERE MaTheLoai=?", maTheLoai, "thể loại");
    }

    // Đếm tổng số đầu sách
    public int countTongDauSach() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Sach";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm tổng đầu sách", e);
            throw e;
        }
        return 0;
    }

    // Tính tổng số bản sách (SoLuongTong)
    public int sumTongSoLuongSach() throws SQLException {
        String sql = "SELECT COALESCE(SUM(SoLuongTong), 0) FROM Sach";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tính tổng số lượng sách", e);
            throw e;
        }
        return 0;
    }
}


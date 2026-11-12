package com.libsmart.dao;

import com.libsmart.model.NguoiDung;
import com.libsmart.utils.DBConnection;
import com.libsmart.utils.PasswordUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NguoiDungDAO {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungDAO.class.getName());

    private NguoiDung mapNguoiDung(ResultSet rs) throws SQLException {
        return new NguoiDung(
            rs.getInt("MaNguoiDung"),
            rs.getString("TenDangNhap"),
            "",
            rs.getString("HoTen"),
            rs.getString("VaiTro"),
            rs.getBoolean("TrangThai")
        );
    }

    public NguoiDung checkLogin(String username, String password) throws SQLException {
        String sql = "SELECT MaNguoiDung, TenDangNhap, MatKhau, HoTen, VaiTro, TrangThai "
                + "FROM NguoiDung WHERE TenDangNhap = ? AND TrangThai = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("MatKhau");
                    if (!PasswordUtils.verifyPassword(password, storedHash)) {
                        return null;
                    }
                    NguoiDung user = mapNguoiDung(rs);
                    if (PasswordUtils.needsUpgrade(storedHash)) {
                        upgradePasswordHash(conn, user.getMaNguoiDung(), password);
                    }
                    return user;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra đăng nhập cho tài khoản: " + username, e);
            throw e;
        }

        return null;
    }

    private void upgradePasswordHash(Connection conn, int maNguoiDung, String plainPassword) {
        String sql = "UPDATE NguoiDung SET MatKhau = ? WHERE MaNguoiDung = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtils.hashPassword(plainPassword));
            ps.setInt(2, maNguoiDung);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không thể nâng cấp hash mật khẩu cho user #" + maNguoiDung, e);
        }
    }

    public List<NguoiDung> getAllNguoiDung() throws SQLException {
        List<NguoiDung> list = new ArrayList<>();
        String sql = "SELECT MaNguoiDung, TenDangNhap, HoTen, VaiTro, TrangThai FROM NguoiDung ORDER BY MaNguoiDung";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapNguoiDung(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách người dùng", e);
            throw e;
        }
        return list;
    }

    public boolean insertNguoiDung(NguoiDung nd) throws SQLException {
        String sql = "INSERT INTO NguoiDung (TenDangNhap, MatKhau, HoTen, VaiTro, TrangThai) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nd.getTenDangNhap().trim());
            ps.setString(2, PasswordUtils.hashPassword(nd.getMatKhau()));
            ps.setString(3, nd.getHoTen().trim());
            ps.setString(4, nd.getVaiTro());
            ps.setBoolean(5, nd.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm người dùng: " + nd.getTenDangNhap(), e);
            throw e;
        }
    }

    public boolean updateNguoiDung(NguoiDung nd) throws SQLException {
        String sql = "UPDATE NguoiDung SET HoTen = ?, VaiTro = ? WHERE MaNguoiDung = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nd.getHoTen().trim());
            ps.setString(2, nd.getVaiTro());
            ps.setInt(3, nd.getMaNguoiDung());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật người dùng #" + nd.getMaNguoiDung(), e);
            throw e;
        }
    }

    public boolean capNhatTrangThai(int maNguoiDung, boolean trangThai) throws SQLException {
        String sql = "UPDATE NguoiDung SET TrangThai = ? WHERE MaNguoiDung = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, trangThai);
            ps.setInt(2, maNguoiDung);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật trạng thái người dùng #" + maNguoiDung, e);
            throw e;
        }
    }

    public boolean resetMatKhau(int maNguoiDung, String newPlainPassword) throws SQLException {
        String sql = "UPDATE NguoiDung SET MatKhau = ? WHERE MaNguoiDung = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtils.hashPassword(newPlainPassword));
            ps.setInt(2, maNguoiDung);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đặt lại mật khẩu người dùng #" + maNguoiDung, e);
            throw e;
        }
    }

    public boolean doiMatKhau(int maNguoiDung, String oldPlainPassword, String newPlainPassword) throws SQLException {
        String sqlSelect = "SELECT MatKhau FROM NguoiDung WHERE MaNguoiDung = ? AND TrangThai = TRUE";
        String sqlUpdate = "UPDATE NguoiDung SET MatKhau = ? WHERE MaNguoiDung = ?";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlSelect)) {
                ps.setInt(1, maNguoiDung);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    String storedHash = rs.getString("MatKhau");
                    if (!PasswordUtils.verifyPassword(oldPlainPassword, storedHash)) {
                        return false;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, PasswordUtils.hashPassword(newPlainPassword));
                ps.setInt(2, maNguoiDung);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đổi mật khẩu người dùng #" + maNguoiDung, e);
            throw e;
        }
    }
}
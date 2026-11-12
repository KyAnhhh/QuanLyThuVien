package com.libsmart.dao;

import com.libsmart.model.NguoiDung;
import com.libsmart.utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NguoiDungDAO {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungDAO.class.getName());

    public NguoiDung checkLogin(String username, String password) throws SQLException {
        String sql = "SELECT MaNguoiDung, TenDangNhap, MatKhau, HoTen, VaiTro "
                + "FROM NguoiDung WHERE TenDangNhap = ? AND MatKhau = ? AND TrangThai = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new NguoiDung(
                        rs.getInt("MaNguoiDung"),
                        rs.getString("TenDangNhap"),
                        rs.getString("MatKhau"),
                        rs.getString("HoTen"),
                        rs.getString("VaiTro")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra đăng nhập cho tài khoản: " + username, e);
            throw e;
        }

        return null;
    }
}
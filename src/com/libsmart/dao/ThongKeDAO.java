package com.libsmart.dao;

import com.libsmart.utils.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO phục vụ các truy vấn thống kê & báo cáo (quá hạn, tiền phạt, top sách mượn nhiều).
 */
public class ThongKeDAO {

    private static final Logger LOGGER = Logger.getLogger(ThongKeDAO.class.getName());

    public List<Object[]> getDanhSachQuaHan() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT pm.MaPhieuMuon, dg.HoTen, COALESCE(dg.SoDienThoai, '') AS SoDienThoai, " +
            "COALESCE(dg.Email, '') AS Email, s.TenSach, pm.NgayMuon, pm.NgayHenTra, " +
            "DATEDIFF(CURRENT_DATE, pm.NgayHenTra) AS SoNgayTre " +
            "FROM PhieuMuon pm " +
            "JOIN ChiTietPhieuMuon ct ON pm.MaPhieuMuon = ct.MaPhieuMuon " +
            "JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia " +
            "JOIN Sach s ON ct.MaSach = s.MaSach " +
            "WHERE pm.TrangThai = 'DANG_MUON' AND ct.NgayTraThucTe IS NULL " +
            "AND pm.NgayHenTra < CURRENT_DATE " +
            "ORDER BY SoNgayTre DESC, pm.NgayHenTra ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long soNgayTre = Math.max(0, rs.getLong("SoNgayTre"));
                BigDecimal phatTamTinh = MuonTraDAO.tinhTienPhat(soNgayTre);
                list.add(new Object[]{
                    rs.getInt("MaPhieuMuon"),
                    rs.getString("HoTen"),
                    rs.getString("SoDienThoai"),
                    rs.getString("Email"),
                    rs.getString("TenSach"),
                    rs.getTimestamp("NgayMuon"),
                    rs.getDate("NgayHenTra"),
                    soNgayTre,
                    phatTamTinh
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thống kê danh sách quá hạn", e);
            throw e;
        }
        return list;
    }

    public List<Integer> getDanhSachNamPhat() throws SQLException {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(NgayTraThucTe) AS Nam "
                + "FROM ChiTietPhieuMuon WHERE NgayTraThucTe IS NOT NULL "
                + "ORDER BY Nam DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                years.add(rs.getInt("Nam"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách năm thống kê tiền phạt", e);
            throw e;
        }
        return years;
    }

    public List<Object[]> getThongKeTienPhatTheoThang(Integer nam) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT YEAR(NgayTraThucTe) AS Nam, MONTH(NgayTraThucTe) AS Thang, " +
            "COUNT(*) AS SoLuotPhat, COALESCE(SUM(TienPhat), 0) AS TongTienPhat " +
            "FROM ChiTietPhieuMuon " +
            "WHERE NgayTraThucTe IS NOT NULL AND TienPhat > 0 " +
            (nam != null ? "AND YEAR(NgayTraThucTe) = ? " : "") +
            "GROUP BY YEAR(NgayTraThucTe), MONTH(NgayTraThucTe) " +
            "ORDER BY Nam DESC, Thang DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (nam != null) {
                ps.setInt(1, nam);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("Nam"),
                        rs.getInt("Thang"),
                        rs.getInt("SoLuotPhat"),
                        rs.getBigDecimal("TongTienPhat")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thống kê tiền phạt theo tháng", e);
            throw e;
        }
        return list;
    }

    public BigDecimal getTongTienPhat(Integer nam) throws SQLException {
        String sql =
            "SELECT COALESCE(SUM(TienPhat), 0) AS Tong " +
            "FROM ChiTietPhieuMuon WHERE NgayTraThucTe IS NOT NULL " +
            (nam != null ? "AND YEAR(NgayTraThucTe) = ?" : "");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (nam != null) {
                ps.setInt(1, nam);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("Tong");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tính tổng tiền phạt", e);
            throw e;
        }
        return BigDecimal.ZERO;
    }

    public List<Object[]> getTopSachMuonNhieuNhat(int limit) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT s.MaSach, s.TenSach, COALESCE(tg.TenTacGia, 'N/A') AS TenTacGia, " +
            "COALESCE(tl.TenTheLoai, 'N/A') AS TenTheLoai, " +
            "COUNT(ct.MaChiTiet) AS SoLuotMuon, " +
            "COALESCE(SUM(CASE WHEN ct.MaChiTiet IS NOT NULL AND ct.NgayTraThucTe IS NULL THEN 1 ELSE 0 END), 0) AS DangMuon, " +
            "s.SoLuongCon " +
            "FROM Sach s " +
            "LEFT JOIN TacGia tg ON s.MaTacGia = tg.MaTacGia " +
            "LEFT JOIN TheLoai tl ON s.MaTheLoai = tl.MaTheLoai " +
            "LEFT JOIN ChiTietPhieuMuon ct ON s.MaSach = ct.MaSach " +
            "GROUP BY s.MaSach, s.TenSach, tg.TenTacGia, tl.TenTheLoai, s.SoLuongCon " +
            "ORDER BY SoLuotMuon DESC, s.MaSach ASC " +
            "LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    list.add(new Object[]{
                        rank++,
                        rs.getInt("MaSach"),
                        rs.getString("TenSach"),
                        rs.getString("TenTacGia"),
                        rs.getString("TenTheLoai"),
                        rs.getInt("SoLuotMuon"),
                        rs.getInt("DangMuon"),
                        rs.getInt("SoLuongCon")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thống kê top sách mượn nhiều nhất", e);
            throw e;
        }
        return list;
    }
}

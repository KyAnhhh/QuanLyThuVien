
package com.libsmart.dao;

import com.libsmart.utils.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class MuonTraDAO {

    public static final BigDecimal MUC_PHAT_MOI_NGAY = new BigDecimal("5000");

    public static class KetQuaMuon {
        private final boolean thanhCong;
        private final String thongBao;

        public KetQuaMuon(boolean thanhCong, String thongBao) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() { return thanhCong; }
        public String getThongBao() { return thongBao; }
    }

    public static class KetQuaTra {
        private final boolean thanhCong;
        private final String thongBao;
        private final long soNgayTre;
        private final BigDecimal tienPhat;

        public KetQuaTra(boolean thanhCong, String thongBao, long soNgayTre, BigDecimal tienPhat) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
            this.soNgayTre = soNgayTre;
            this.tienPhat = tienPhat;
        }

        public boolean isThanhCong() { return thanhCong; }
        public String getThongBao() { return thongBao; }
        public long getSoNgayTre() { return soNgayTre; }
        public BigDecimal getTienPhat() { return tienPhat; }
    }

    // Lấy danh sách phiếu mượn
    public List<Object[]> getAllPhieuMuon() {

        List<Object[]> list = new ArrayList<>();

        String sql =
            "SELECT pm.MaPhieuMuon, dg.HoTen AS TenDocGia, " +
            "s.TenSach, pm.NgayMuon, pm.NgayHenTra, pm.TrangThai, " +
            "COALESCE(ct.TienPhat, 0) AS TienPhat " +
            "FROM PhieuMuon pm " +
            "LEFT JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia " +
            "LEFT JOIN ChiTietPhieuMuon ct " +
            "ON pm.MaPhieuMuon = ct.MaPhieuMuon " +
            "LEFT JOIN Sach s ON ct.MaSach = s.MaSach " +
            "ORDER BY pm.MaPhieuMuon DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                list.add(new Object[]{
                    rs.getInt("MaPhieuMuon"),
                    rs.getString("TenDocGia") != null
                        ? rs.getString("TenDocGia") : "N/A",
                    rs.getString("TenSach") != null
                        ? rs.getString("TenSach") : "N/A",
                    rs.getTimestamp("NgayMuon"),
                    rs.getDate("NgayHenTra"),
                    rs.getString("TrangThai"),
                    rs.getBigDecimal("TienPhat")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private LocalDate getDbCurrentDate(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT CURRENT_DATE");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDate(1).toLocalDate();
            }
        }
        return LocalDate.now();
    }

    // Kiểm tra người dùng tồn tại và đang hoạt động
    private boolean isNguoiDungHopLe(Connection conn, int maNguoiDung) throws SQLException {
        String sql = "SELECT 1 FROM NguoiDung WHERE MaNguoiDung = ? AND TrangThai = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Kiểm tra trạng thái & hạn thẻ của độc giả trong transaction
    private String kiemTraTheDocGia(Connection conn, int maDocGia) throws SQLException {
        String sql = "SELECT TrangThai, NgayHetHan FROM DocGia WHERE MaDocGia = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDocGia);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "Không tìm thấy thông tin độc giả!";
                }
                String trangThai = rs.getString("TrangThai");
                Date ngayHetHan = rs.getDate("NgayHetHan");
                if (!"HOAT_DONG".equalsIgnoreCase(trangThai)) {
                    return "Thẻ độc giả đang bị khóa hoặc không ở trạng thái hoạt động (" + trangThai + ")!";
                }
                LocalDate today = getDbCurrentDate(conn);
                if (ngayHetHan == null || ngayHetHan.toLocalDate().isBefore(today)) {
                    return "Thẻ độc giả đã hết hạn (" + ngayHetHan + "). Vui lòng gia hạn thẻ trước khi mượn!";
                }
                return null;
            }
        }
    }

    public boolean taoPhieuMuon(int maDocGia, int maSach, int soNgayMuon, int maNguoiDung) {
        return taoPhieuMuonChiTiet(maDocGia, maSach, soNgayMuon, maNguoiDung).isThanhCong();
    }

    // Lập phiếu mượn sách theo đúng thủ thư đang đăng nhập và kiểm tra điều kiện thẻ độc giả
    public KetQuaMuon taoPhieuMuonChiTiet(
        int maDocGia, int maSach, int soNgayMuon, int maNguoiDung
    ) {

        if (maDocGia <= 0 || maSach <= 0 || soNgayMuon <= 0 || maNguoiDung <= 0) {
            return new KetQuaMuon(false, "Thông tin lập phiếu mượn không hợp lệ!");
        }

        String sqlPhieu =
            "INSERT INTO PhieuMuon " +
            "(MaDocGia, MaNguoiDung, NgayHenTra, TrangThai) " +
            "VALUES (?, ?, DATE_ADD(CURRENT_DATE, INTERVAL ? DAY), 'DANG_MUON')";

        String sqlChiTiet =
            "INSERT INTO ChiTietPhieuMuon (MaPhieuMuon, MaSach) " +
            "VALUES (?, ?)";

        String sqlGiamSach =
            "UPDATE Sach SET SoLuongCon = SoLuongCon - 1 " +
            "WHERE MaSach = ? AND SoLuongCon > 0";

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                // Xác nhận tài khoản thủ thư tạo phiếu hợp lệ
                if (!isNguoiDungHopLe(conn, maNguoiDung)) {
                    conn.rollback();
                    return new KetQuaMuon(false, "Tài khoản thủ thư không hợp lệ hoặc đã bị khóa!");
                }

                // Kiểm tra trạng thái & hạn thẻ độc giả
                String loiThe = kiemTraTheDocGia(conn, maDocGia);
                if (loiThe != null) {
                    conn.rollback();
                    return new KetQuaMuon(false, loiThe);
                }

                // Kiểm tra và giảm số lượng sách
                try (PreparedStatement ps =
                         conn.prepareStatement(sqlGiamSach)) {

                    ps.setInt(1, maSach);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return new KetQuaMuon(false, "Sách đã hết số lượng trong kho!");
                    }
                }

                // Tạo phiếu mượn
                int maPhieuMuon;

                try (PreparedStatement ps = conn.prepareStatement(
                        sqlPhieu, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, maDocGia);
                    ps.setInt(2, maNguoiDung);
                    ps.setInt(3, soNgayMuon);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return new KetQuaMuon(false, "Không thể tạo phiếu mượn!");
                    }

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            conn.rollback();
                            return new KetQuaMuon(false, "Không lấy được mã phiếu mượn mới!");
                        }

                        maPhieuMuon = rs.getInt(1);
                    }
                }

                // Thêm chi tiết phiếu mượn
                try (PreparedStatement ps =
                         conn.prepareStatement(sqlChiTiet)) {

                    ps.setInt(1, maPhieuMuon);
                    ps.setInt(2, maSach);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return new KetQuaMuon(false, "Không thể lưu chi tiết phiếu mượn!");
                    }
                }

                conn.commit();
                return new KetQuaMuon(true, "Lập phiếu mượn sách thành công (Mã phiếu: " + maPhieuMuon + ")!");

            } catch (SQLException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new KetQuaMuon(false, "Lỗi cơ sở dữ liệu: " + e.getMessage());
        }
    }

    // Trả sách an toàn, không cho trả hai lần
    public boolean traSach(int maPhieuMuon) {
        return traSachChiTiet(maPhieuMuon).isThanhCong();
    }

    public KetQuaTra traSachChiTiet(int maPhieuMuon) {

        if (maPhieuMuon <= 0) {
            return new KetQuaTra(false, "Mã phiếu mượn không hợp lệ!", 0, BigDecimal.ZERO);
        }

        String sqlKiemTra =
            "SELECT TrangThai, NgayHenTra FROM PhieuMuon " +
            "WHERE MaPhieuMuon = ? FOR UPDATE";

        String sqlChiTiet =
            "SELECT MaSach FROM ChiTietPhieuMuon " +
            "WHERE MaPhieuMuon = ? AND NgayTraThucTe IS NULL " +
            "FOR UPDATE";

        String sqlCapNhatChiTiet =
            "UPDATE ChiTietPhieuMuon " +
            "SET NgayTraThucTe = CURRENT_TIMESTAMP, TienPhat = ?, GhiChu = ? " +
            "WHERE MaPhieuMuon = ? AND NgayTraThucTe IS NULL";

        String sqlTangSach =
            "UPDATE Sach SET SoLuongCon = SoLuongCon + 1 " +
            "WHERE MaSach = ?";

        String sqlCapNhatPhieu =
            "UPDATE PhieuMuon SET TrangThai = 'DA_TRA' " +
            "WHERE MaPhieuMuon = ? AND TrangThai = 'DANG_MUON'";

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                // Bước 1: Khóa và kiểm tra phiếu mượn
                String trangThai;
                Date ngayHenTra;

                try (PreparedStatement ps =
                         conn.prepareStatement(sqlKiemTra)) {

                    ps.setInt(1, maPhieuMuon);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            conn.rollback();
                            return new KetQuaTra(false, "Không tìm thấy phiếu mượn #" + maPhieuMuon + "!", 0, BigDecimal.ZERO);
                        }

                        trangThai = rs.getString("TrangThai");
                        ngayHenTra = rs.getDate("NgayHenTra");
                    }
                }

                // Không cho trả nếu phiếu đã trả
                if (!"DANG_MUON".equalsIgnoreCase(trangThai)) {
                    conn.rollback();
                    return new KetQuaTra(false, "Phiếu mượn #" + maPhieuMuon + " đã được trả trước đó!", 0, BigDecimal.ZERO);
                }

                // Bước 2: Lấy các sách chưa trả
                List<Integer> danhSachMaSach = new ArrayList<>();

                try (PreparedStatement ps =
                         conn.prepareStatement(sqlChiTiet)) {

                    ps.setInt(1, maPhieuMuon);

                    try (ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                            danhSachMaSach.add(
                                rs.getInt("MaSach")
                            );
                        }
                    }
                }

                if (danhSachMaSach.isEmpty()) {
                    conn.rollback();
                    return new KetQuaTra(false, "Không có chi tiết sách cần trả cho phiếu #" + maPhieuMuon + "!", 0, BigDecimal.ZERO);
                }

                // Tính số ngày trễ và tiền phạt
                LocalDate today = getDbCurrentDate(conn);
                long soNgayTre = 0;
                if (ngayHenTra != null) {
                    soNgayTre = Math.max(0, ChronoUnit.DAYS.between(ngayHenTra.toLocalDate(), today));
                }
                BigDecimal tienPhat = soNgayTre > 0
                        ? MUC_PHAT_MOI_NGAY.multiply(BigDecimal.valueOf(soNgayTre))
                        : BigDecimal.ZERO;
                String ghiChu = soNgayTre > 0
                        ? "Trả quá hạn " + soNgayTre + " ngày"
                        : "Trả đúng hạn";

                // Bước 3: Cập nhật ngày trả, tiền phạt, ghi chú
                try (PreparedStatement ps =
                         conn.prepareStatement(sqlCapNhatChiTiet)) {

                    ps.setBigDecimal(1, tienPhat);
                    ps.setString(2, ghiChu);
                    ps.setInt(3, maPhieuMuon);

                    if (ps.executeUpdate() != danhSachMaSach.size()) {
                        conn.rollback();
                        return new KetQuaTra(false, "Không thể cập nhật chi tiết trả sách!", 0, BigDecimal.ZERO);
                    }
                }

                // Bước 4: Cộng lại số lượng từng sách
                try (PreparedStatement ps =
                         conn.prepareStatement(sqlTangSach)) {

                    for (int maSach : danhSachMaSach) {

                        ps.setInt(1, maSach);

                        if (ps.executeUpdate() != 1) {
                            conn.rollback();
                            return new KetQuaTra(false, "Không thể cập nhật tồn kho sách!", 0, BigDecimal.ZERO);
                        }
                    }
                }

                // Bước 5: Cập nhật trạng thái phiếu
                try (PreparedStatement ps =
                         conn.prepareStatement(sqlCapNhatPhieu)) {

                    ps.setInt(1, maPhieuMuon);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return new KetQuaTra(false, "Không thể cập nhật trạng thái phiếu mượn!", 0, BigDecimal.ZERO);
                    }
                }

                // Hoàn tất giao dịch
                conn.commit();
                String msg = soNgayTre > 0
                        ? String.format("Trả sách thành công!\nPhiếu quá hạn %d ngày.\nTiền phạt: %,.0f VNĐ", soNgayTre, tienPhat)
                        : "Trả sách đúng hạn thành công!";
                return new KetQuaTra(true, msg, soNgayTre, tienPhat);

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return new KetQuaTra(false, "Lỗi cơ sở dữ liệu: " + e.getMessage(), 0, BigDecimal.ZERO);
        }
    }

    // Đếm số phiếu đang cho mượn
    public int countPhieuDangMuon() {
        String sql = "SELECT COUNT(*) FROM PhieuMuon WHERE TrangThai = 'DANG_MUON'";
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

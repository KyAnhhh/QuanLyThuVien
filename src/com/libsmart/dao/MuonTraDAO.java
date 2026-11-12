
package com.libsmart.dao;

import com.libsmart.utils.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MuonTraDAO {

    private static final Logger LOGGER = Logger.getLogger(MuonTraDAO.class.getName());
    public static final BigDecimal MUC_PHAT_MOI_NGAY = new BigDecimal("5000");
    public static final int MAX_SACH_DANG_MUON = 5;

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

    // Lấy danh sách chi tiết phiếu mượn
    public List<Object[]> getAllPhieuMuon() {
        List<Object[]> list = new ArrayList<>();

        String sql =
            "SELECT pm.MaPhieuMuon, ct.MaChiTiet, dg.HoTen AS TenDocGia, " +
            "s.TenSach, nd.HoTen AS TenThuThu, pm.NgayMuon, pm.NgayHenTra, " +
            "CASE WHEN ct.NgayTraThucTe IS NOT NULL THEN 'DA_TRA' ELSE pm.TrangThai END AS TrangThaiChiTiet, " +
            "COALESCE(ct.TienPhat, 0) AS TienPhat, COALESCE(ct.GhiChu, '') AS GhiChu " +
            "FROM PhieuMuon pm " +
            "LEFT JOIN DocGia dg ON pm.MaDocGia = dg.MaDocGia " +
            "LEFT JOIN NguoiDung nd ON pm.MaNguoiDung = nd.MaNguoiDung " +
            "LEFT JOIN ChiTietPhieuMuon ct ON pm.MaPhieuMuon = ct.MaPhieuMuon " +
            "LEFT JOIN Sach s ON ct.MaSach = s.MaSach " +
            "ORDER BY pm.MaPhieuMuon DESC, ct.MaChiTiet ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("MaPhieuMuon"),
                    rs.getInt("MaChiTiet"),
                    rs.getString("TenDocGia") != null ? rs.getString("TenDocGia") : "N/A",
                    rs.getString("TenSach") != null ? rs.getString("TenSach") : "N/A",
                    rs.getString("TenThuThu") != null ? rs.getString("TenThuThu") : "N/A",
                    rs.getTimestamp("NgayMuon"),
                    rs.getDate("NgayHenTra"),
                    rs.getString("TrangThaiChiTiet"),
                    rs.getBigDecimal("TienPhat"),
                    rs.getString("GhiChu")
                });
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách phiếu mượn", e);
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

    private boolean isNguoiDungHopLe(Connection conn, int maNguoiDung) throws SQLException {
        String sql = "SELECT 1 FROM NguoiDung WHERE MaNguoiDung = ? AND TrangThai = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static String kiemTraTrangThaiTheHopLe(String trangThai, LocalDate ngayHetHan, LocalDate today) {
        if (!"HOAT_DONG".equalsIgnoreCase(trangThai)) {
            return "Thẻ độc giả đang bị khóa hoặc không ở trạng thái hoạt động (" + trangThai + ")!";
        }
        if (ngayHetHan == null || ngayHetHan.isBefore(today)) {
            return "Thẻ độc giả đã hết hạn (" + ngayHetHan + "). Vui lòng gia hạn thẻ trước khi mượn!";
        }
        return null;
    }

    public static long tinhSoNgayTre(LocalDate ngayHenTra, LocalDate ngayThucTe) {
        if (ngayHenTra == null || ngayThucTe == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(ngayHenTra, ngayThucTe));
    }

    public static BigDecimal tinhTienPhat(long soNgayTre) {
        if (soNgayTre <= 0) {
            return BigDecimal.ZERO;
        }
        return MUC_PHAT_MOI_NGAY.multiply(BigDecimal.valueOf(soNgayTre));
    }

    private String kiemTraTheDocGia(Connection conn, int maDocGia, LocalDate today) throws SQLException {
        String sql = "SELECT TrangThai, NgayHetHan FROM DocGia WHERE MaDocGia = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDocGia);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "Không tìm thấy thông tin độc giả!";
                }
                String trangThai = rs.getString("TrangThai");
                Date ngayHetHan = rs.getDate("NgayHetHan");
                return kiemTraTrangThaiTheHopLe(
                        trangThai,
                        ngayHetHan != null ? ngayHetHan.toLocalDate() : null,
                        today);
            }
        }
    }

    private int countPhieuQuaHanCuaDocGia(Connection conn, int maDocGia, LocalDate today) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT pm.MaPhieuMuon) "
                + "FROM PhieuMuon pm "
                + "JOIN ChiTietPhieuMuon ct ON pm.MaPhieuMuon = ct.MaPhieuMuon "
                + "WHERE pm.MaDocGia = ? AND pm.TrangThai = 'DANG_MUON' "
                + "AND ct.NgayTraThucTe IS NULL AND pm.NgayHenTra < ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDocGia);
            ps.setDate(2, Date.valueOf(today));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int countSachDangMuonCuaDocGia(Connection conn, int maDocGia) throws SQLException {
        String sql = "SELECT COUNT(*) "
                + "FROM ChiTietPhieuMuon ct "
                + "JOIN PhieuMuon pm ON ct.MaPhieuMuon = pm.MaPhieuMuon "
                + "WHERE pm.MaDocGia = ? AND pm.TrangThai = 'DANG_MUON' AND ct.NgayTraThucTe IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDocGia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean taoPhieuMuon(int maDocGia, int maSach, int soNgayMuon, int maNguoiDung) {
        return taoPhieuMuonChiTiet(maDocGia, maSach, soNgayMuon, maNguoiDung).isThanhCong();
    }

    public KetQuaMuon taoPhieuMuonChiTiet(
        int maDocGia, int maSach, int soNgayMuon, int maNguoiDung
    ) {
        return taoPhieuMuonNhieuSach(maDocGia, Collections.singletonList(maSach), soNgayMuon, maNguoiDung);
    }

    // Lập phiếu mượn gồm nhiều sách trong cùng 1 transaction, có kiểm tra hạn thẻ, quá hạn & hạn mức
    public KetQuaMuon taoPhieuMuonNhieuSach(
        int maDocGia, List<Integer> danhSachMaSach, int soNgayMuon, int maNguoiDung
    ) {
        if (maDocGia <= 0 || soNgayMuon <= 0 || maNguoiDung <= 0 || danhSachMaSach == null || danhSachMaSach.isEmpty()) {
            return new KetQuaMuon(false, "Thông tin lập phiếu mượn không hợp lệ!");
        }

        Set<Integer> uniqueBookIds = new LinkedHashSet<>();
        for (Integer maSach : danhSachMaSach) {
            if (maSach == null || maSach <= 0) {
                return new KetQuaMuon(false, "Danh sách sách mượn có mã không hợp lệ!");
            }
            if (!uniqueBookIds.add(maSach)) {
                return new KetQuaMuon(false, "Không được mượn trùng cùng một đầu sách trong một phiếu!");
            }
        }

        String sqlPhieu =
            "INSERT INTO PhieuMuon (MaDocGia, MaNguoiDung, NgayHenTra, TrangThai) " +
            "VALUES (?, ?, DATE_ADD(CURRENT_DATE, INTERVAL ? DAY), 'DANG_MUON')";
        String sqlChiTiet =
            "INSERT INTO ChiTietPhieuMuon (MaPhieuMuon, MaSach) VALUES (?, ?)";
        String sqlGiamSach =
            "UPDATE Sach SET SoLuongCon = SoLuongCon - 1 WHERE MaSach = ? AND SoLuongCon > 0";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!isNguoiDungHopLe(conn, maNguoiDung)) {
                    conn.rollback();
                    return new KetQuaMuon(false, "Tài khoản thủ thư không hợp lệ hoặc đã bị khóa!");
                }

                LocalDate today = getDbCurrentDate(conn);

                String loiThe = kiemTraTheDocGia(conn, maDocGia, today);
                if (loiThe != null) {
                    conn.rollback();
                    return new KetQuaMuon(false, loiThe);
                }

                if (countPhieuQuaHanCuaDocGia(conn, maDocGia, today) > 0) {
                    conn.rollback();
                    return new KetQuaMuon(false, "Độc giả đang có sách quá hạn chưa trả! Vui lòng hoàn trả sách quá hạn trước khi mượn mới.");
                }

                int dangMuon = countSachDangMuonCuaDocGia(conn, maDocGia);
                if (dangMuon + uniqueBookIds.size() > MAX_SACH_DANG_MUON) {
                    conn.rollback();
                    int conLai = Math.max(0, MAX_SACH_DANG_MUON - dangMuon);
                    return new KetQuaMuon(false,
                            "Độc giả đang mượn " + dangMuon + " cuốn. Hạn mức tối đa là "
                            + MAX_SACH_DANG_MUON + " cuốn (chỉ có thể mượn thêm " + conLai + " cuốn)!");
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlGiamSach)) {
                    for (int maSach : uniqueBookIds) {
                        ps.setInt(1, maSach);
                        if (ps.executeUpdate() != 1) {
                            conn.rollback();
                            return new KetQuaMuon(false, "Sách mã " + maSach + " đã hết số lượng trong kho!");
                        }
                    }
                }

                int maPhieuMuon;
                try (PreparedStatement ps = conn.prepareStatement(sqlPhieu, Statement.RETURN_GENERATED_KEYS)) {
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

                try (PreparedStatement ps = conn.prepareStatement(sqlChiTiet)) {
                    for (int maSach : uniqueBookIds) {
                        ps.setInt(1, maPhieuMuon);
                        ps.setInt(2, maSach);
                        if (ps.executeUpdate() != 1) {
                            conn.rollback();
                            return new KetQuaMuon(false, "Không thể lưu chi tiết phiếu mượn!");
                        }
                    }
                }

                conn.commit();
                return new KetQuaMuon(true,
                        "Lập phiếu mượn #" + maPhieuMuon + " thành công (" + uniqueBookIds.size() + " cuốn)!");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lập phiếu mượn", e);
            return new KetQuaMuon(false, "Lỗi cơ sở dữ liệu: " + e.getMessage());
        }
    }

    public boolean traSach(int maPhieuMuon) {
        return traSachChiTiet(maPhieuMuon).isThanhCong();
    }

    public KetQuaTra traSachChiTiet(int maPhieuMuon) {
        if (maPhieuMuon <= 0) {
            return new KetQuaTra(false, "Mã phiếu mượn không hợp lệ!", 0, BigDecimal.ZERO);
        }
        return thucHienTraSach(maPhieuMuon, null);
    }

    public KetQuaTra traTheoChiTiet(int maChiTiet) {
        if (maChiTiet <= 0) {
            return new KetQuaTra(false, "Mã chi tiết phiếu mượn không hợp lệ!", 0, BigDecimal.ZERO);
        }
        return thucHienTraSach(null, maChiTiet);
    }

    private KetQuaTra thucHienTraSach(Integer maPhieuMuonParam, Integer maChiTietParam) {
        String sqlLookupByDetail =
            "SELECT ct.MaPhieuMuon, ct.MaSach, ct.NgayTraThucTe, pm.TrangThai, pm.NgayHenTra " +
            "FROM ChiTietPhieuMuon ct " +
            "JOIN PhieuMuon pm ON ct.MaPhieuMuon = pm.MaPhieuMuon " +
            "WHERE ct.MaChiTiet = ? FOR UPDATE";

        String sqlKiemTraPhieu =
            "SELECT TrangThai, NgayHenTra FROM PhieuMuon WHERE MaPhieuMuon = ? FOR UPDATE";

        String sqlChiTietChuaTra =
            "SELECT MaChiTiet, MaSach FROM ChiTietPhieuMuon " +
            "WHERE MaPhieuMuon = ? AND NgayTraThucTe IS NULL FOR UPDATE";

        String sqlCapNhatChiTiet =
            "UPDATE ChiTietPhieuMuon " +
            "SET NgayTraThucTe = CURRENT_TIMESTAMP, TienPhat = ?, GhiChu = ? " +
            "WHERE MaChiTiet = ? AND NgayTraThucTe IS NULL";

        String sqlTangSach =
            "UPDATE Sach SET SoLuongCon = SoLuongCon + 1 WHERE MaSach = ?";

        String sqlDemConLai =
            "SELECT COUNT(*) FROM ChiTietPhieuMuon WHERE MaPhieuMuon = ? AND NgayTraThucTe IS NULL";

        String sqlCapNhatPhieu =
            "UPDATE PhieuMuon SET TrangThai = 'DA_TRA' WHERE MaPhieuMuon = ? AND TrangThai = 'DANG_MUON'";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int maPhieuMuon;
                Date ngayHenTra;
                List<int[]> itemsToReturn = new ArrayList<>(); // [maChiTiet, maSach]

                if (maChiTietParam != null) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlLookupByDetail)) {
                        ps.setInt(1, maChiTietParam);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                conn.rollback();
                                return new KetQuaTra(false, "Không tìm thấy chi tiết mượn #" + maChiTietParam + "!", 0, BigDecimal.ZERO);
                            }
                            if (rs.getTimestamp("NgayTraThucTe") != null || !"DANG_MUON".equalsIgnoreCase(rs.getString("TrangThai"))) {
                                conn.rollback();
                                return new KetQuaTra(false, "Cuốn sách này đã được trả trước đó!", 0, BigDecimal.ZERO);
                            }
                            maPhieuMuon = rs.getInt("MaPhieuMuon");
                            ngayHenTra = rs.getDate("NgayHenTra");
                            itemsToReturn.add(new int[]{maChiTietParam, rs.getInt("MaSach")});
                        }
                    }
                } else {
                    maPhieuMuon = maPhieuMuonParam;
                    try (PreparedStatement ps = conn.prepareStatement(sqlKiemTraPhieu)) {
                        ps.setInt(1, maPhieuMuon);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                conn.rollback();
                                return new KetQuaTra(false, "Không tìm thấy phiếu mượn #" + maPhieuMuon + "!", 0, BigDecimal.ZERO);
                            }
                            if (!"DANG_MUON".equalsIgnoreCase(rs.getString("TrangThai"))) {
                                conn.rollback();
                                return new KetQuaTra(false, "Phiếu mượn #" + maPhieuMuon + " đã được trả trước đó!", 0, BigDecimal.ZERO);
                            }
                            ngayHenTra = rs.getDate("NgayHenTra");
                        }
                    }

                    try (PreparedStatement ps = conn.prepareStatement(sqlChiTietChuaTra)) {
                        ps.setInt(1, maPhieuMuon);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                itemsToReturn.add(new int[]{rs.getInt("MaChiTiet"), rs.getInt("MaSach")});
                            }
                        }
                    }

                    if (itemsToReturn.isEmpty()) {
                        conn.rollback();
                        return new KetQuaTra(false, "Không còn sách chưa trả trong phiếu #" + maPhieuMuon + "!", 0, BigDecimal.ZERO);
                    }
                }

                LocalDate today = getDbCurrentDate(conn);
                long soNgayTre = tinhSoNgayTre(ngayHenTra != null ? ngayHenTra.toLocalDate() : null, today);
                BigDecimal phatMoiCuon = tinhTienPhat(soNgayTre);
                String ghiChu = soNgayTre > 0
                        ? "Trả quá hạn " + soNgayTre + " ngày"
                        : "Trả đúng hạn";

                try (PreparedStatement psUpdate = conn.prepareStatement(sqlCapNhatChiTiet);
                     PreparedStatement psTang = conn.prepareStatement(sqlTangSach)) {
                    for (int[] item : itemsToReturn) {
                        psUpdate.setBigDecimal(1, phatMoiCuon);
                        psUpdate.setString(2, ghiChu);
                        psUpdate.setInt(3, item[0]);
                        if (psUpdate.executeUpdate() != 1) {
                            conn.rollback();
                            return new KetQuaTra(false, "Không thể cập nhật chi tiết trả sách!", 0, BigDecimal.ZERO);
                        }

                        psTang.setInt(1, item[1]);
                        if (psTang.executeUpdate() != 1) {
                            conn.rollback();
                            return new KetQuaTra(false, "Không thể cập nhật tồn kho sách!", 0, BigDecimal.ZERO);
                        }
                    }
                }

                int soCuonConChuaTra = 0;
                try (PreparedStatement ps = conn.prepareStatement(sqlDemConLai)) {
                    ps.setInt(1, maPhieuMuon);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            soCuonConChuaTra = rs.getInt(1);
                        }
                    }
                }

                if (soCuonConChuaTra == 0) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlCapNhatPhieu)) {
                        ps.setInt(1, maPhieuMuon);
                        if (ps.executeUpdate() != 1) {
                            conn.rollback();
                            return new KetQuaTra(false, "Không thể cập nhật trạng thái phiếu mượn!", 0, BigDecimal.ZERO);
                        }
                    }
                }

                conn.commit();
                BigDecimal tongTienPhat = phatMoiCuon.multiply(BigDecimal.valueOf(itemsToReturn.size()));
                String msg = soNgayTre > 0
                        ? String.format("Đã trả %d cuốn sách!\nQuá hạn %d ngày. Tổng tiền phạt: %,.0f VNĐ",
                                itemsToReturn.size(), soNgayTre, tongTienPhat)
                        : String.format("Đã trả %d cuốn sách đúng hạn thành công!", itemsToReturn.size());
                if (soCuonConChuaTra > 0) {
                    msg += "\n(Phiếu #" + maPhieuMuon + " còn " + soCuonConChuaTra + " cuốn chưa trả)";
                }
                return new KetQuaTra(true, msg, soNgayTre, tongTienPhat);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi trả sách", e);
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
            LOGGER.log(Level.SEVERE, "Lỗi đếm phiếu đang mượn", e);
        }
        return 0;
    }
}

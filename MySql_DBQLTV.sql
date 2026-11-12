CREATE DATABASE IF NOT EXISTS QuanLyThuVien 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE QuanLyThuVien;

CREATE TABLE IF NOT EXISTS NguoiDung (
    MaNguoiDung INT AUTO_INCREMENT PRIMARY KEY,
    TenDangNhap VARCHAR(50) NOT NULL UNIQUE,
    MatKhau VARCHAR(255) NOT NULL,
    HoTen VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL,
    VaiTro VARCHAR(20) NOT NULL DEFAULT 'THU_THU',
    TrangThai BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS DocGia (
    MaDocGia INT AUTO_INCREMENT PRIMARY KEY,
    HoTen VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL,
    SoDienThoai VARCHAR(15) UNIQUE,
    Email VARCHAR(100),
    NgayTaoThe DATE DEFAULT (CURRENT_DATE),
    NgayHetHan DATE NOT NULL,
    TrangThai VARCHAR(20) DEFAULT 'HOAT_DONG'
);

CREATE TABLE IF NOT EXISTS TacGia (
    MaTacGia INT AUTO_INCREMENT PRIMARY KEY,
    TenTacGia VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL
);

CREATE TABLE IF NOT EXISTS TheLoai (
    MaTheLoai INT AUTO_INCREMENT PRIMARY KEY,
    TenTheLoai VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL
);

CREATE TABLE IF NOT EXISTS Sach (
    MaSach INT AUTO_INCREMENT PRIMARY KEY,
    TenSach VARCHAR(200) CHARACTER SET utf8mb4 NOT NULL,
    MaTacGia INT,
    MaTheLoai INT,
    NamXuatBan INT,
    SoLuongTong INT NOT NULL DEFAULT 0,
    SoLuongCon INT NOT NULL DEFAULT 0,
    GiaBia DECIMAL(10,2),
    FOREIGN KEY (MaTacGia) REFERENCES TacGia(MaTacGia),
    FOREIGN KEY (MaTheLoai) REFERENCES TheLoai(MaTheLoai)
);

CREATE TABLE IF NOT EXISTS PhieuMuon (
    MaPhieuMuon INT AUTO_INCREMENT PRIMARY KEY,
    MaDocGia INT NOT NULL,
    MaNguoiDung INT NOT NULL,
    NgayMuon DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayHenTra DATE NOT NULL,
    TrangThai VARCHAR(20) DEFAULT 'DANG_MUON',
    FOREIGN KEY (MaDocGia) REFERENCES DocGia(MaDocGia),
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
);

CREATE TABLE IF NOT EXISTS ChiTietPhieuMuon (
    MaChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    MaPhieuMuon INT NOT NULL,
    MaSach INT NOT NULL,
    NgayTraThucTe DATETIME NULL,
    TienPhat DECIMAL(10,2) DEFAULT 0,
    GhiChu VARCHAR(255) CHARACTER SET utf8mb4,
    FOREIGN KEY (MaPhieuMuon) REFERENCES PhieuMuon(MaPhieuMuon),
    FOREIGN KEY (MaSach) REFERENCES Sach(MaSach)
);


USE QuanLyThuVien;

-- 1. Thêm dữ liệu mẫu vào bảng Tác Giả (Để cấp ID 1, 2, 3)
INSERT INTO TacGia (TenTacGia) VALUES 
('Nguyễn Văn A'),
('Trần Thị B'),
('Nguyễn Nhật Ánh');

-- 2. Thêm dữ liệu mẫu vào bảng Thể Loại (Để cấp ID 1, 2, 3)
INSERT INTO TheLoai (TenTheLoai) VALUES 
('Lập Trình & Công Nghệ'),
('Cơ Sở Dữ Liệu'),
('Văn Học & Tiểu Thuyết');

-- 3. Thêm dữ liệu mẫu vào bảng Độc Giả
INSERT INTO DocGia (HoTen, SoDienThoai, Email, NgayHetHan) VALUES 
('Phước Toàn', '012345678', 'toan68@gmail.com', '2027-12-31'),
('Văn A', '23456789', 'vana12@gmail.com', '2027-12-31');

-- 4. Chèn Sách sau khi đã có MaTacGia và MaTheLoai tương ứng
INSERT INTO Sach (TenSach, MaTacGia, MaTheLoai, NamXuatBan, SoLuongTong, SoLuongCon, GiaBia) 
VALUES 
('Lập Trình Java Cơ Bản', 1, 1, 2023, 10, 8, 120000.00),
('Nhập Môn CSDL MySQL', 1, 2, 2022, 5, 5, 85000.00),
('Cấu Trúc Dữ Liệu & Giải Thuật', 2, 1, 2024, 15, 12, 150000.00);

-- 5. Thêm tài khoản Đăng nhập
INSERT INTO NguoiDung (TenDangNhap, MatKhau, HoTen, VaiTro) 
VALUES ('admin', 'admin', 'Thủ Thư Admin', 'THU_THU')
ON DUPLICATE KEY UPDATE TenDangNhap=TenDangNhap;




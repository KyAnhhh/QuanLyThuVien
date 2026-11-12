package com.libsmart.model;

import java.sql.Date;

public class DocGia {
    private int maDocGia;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private Date ngayTaoThe;
    private Date ngayHetHan;
    private String trangThai;

    public DocGia() {}

    public DocGia(int maDocGia, String hoTen, String soDienThoai, String email, Date ngayTaoThe, Date ngayHetHan, String trangThai) {
        this.maDocGia = maDocGia;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.ngayTaoThe = ngayTaoThe;
        this.ngayHetHan = ngayHetHan;
        this.trangThai = trangThai;
    }

    public int getMaDocGia() { return maDocGia; }
    public void setMaDocGia(int maDocGia) { this.maDocGia = maDocGia; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getNgayTaoThe() { return ngayTaoThe; }
    public void setNgayTaoThe(Date ngayTaoThe) { this.ngayTaoThe = ngayTaoThe; }

    public Date getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(Date ngayHetHan) { this.ngayHetHan = ngayHetHan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
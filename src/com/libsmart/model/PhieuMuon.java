package com.libsmart.model;

import java.sql.Date;
import java.sql.Timestamp;

public class PhieuMuon {
    private int maPhieuMuon;
    private int maDocGia;
    private int maNguoiDung;
    private Timestamp ngayMuon;
    private Date ngayHenTra;
    private String trangThai;

    public PhieuMuon() {}

    public PhieuMuon(int maPhieuMuon, int maDocGia, int maNguoiDung, Timestamp ngayMuon, Date ngayHenTra, String trangThai) {
        this.maPhieuMuon = maPhieuMuon;
        this.maDocGia = maDocGia;
        this.maNguoiDung = maNguoiDung;
        this.ngayMuon = ngayMuon;
        this.ngayHenTra = ngayHenTra;
        this.trangThai = trangThai;
    }

    public int getMaPhieuMuon() { return maPhieuMuon; }
    public void setMaPhieuMuon(int maPhieuMuon) { this.maPhieuMuon = maPhieuMuon; }

    public int getMaDocGia() { return maDocGia; }
    public void setMaDocGia(int maDocGia) { this.maDocGia = maDocGia; }

    public int getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(int maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public Timestamp getNgayMuon() { return ngayMuon; }
    public void setNgayMuon(Timestamp ngayMuon) { this.ngayMuon = ngayMuon; }

    public Date getNgayHenTra() { return ngayHenTra; }
    public void setNgayHenTra(Date ngayHenTra) { this.ngayHenTra = ngayHenTra; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
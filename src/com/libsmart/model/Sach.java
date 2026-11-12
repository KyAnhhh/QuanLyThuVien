package com.libsmart.model;

import java.math.BigDecimal;

public class Sach {
    private int maSach;
    private String tenSach;
    private int maTacGia;
    private int maTheLoai;
    private int namXuatBan;
    private int soLuongTong;
    private int soLuongCon;
    private BigDecimal giaBia;

    public Sach() {}

    public Sach(int maSach, String tenSach, int maTacGia, int maTheLoai, int namXuatBan, int soLuongTong, int soLuongCon, BigDecimal giaBia) {
        this.maSach = maSach;
        this.tenSach = tenSach;
        this.maTacGia = maTacGia;
        this.maTheLoai = maTheLoai;
        this.namXuatBan = namXuatBan;
        this.soLuongTong = soLuongTong;
        this.soLuongCon = soLuongCon;
        this.giaBia = giaBia;
    }

    public int getMaSach() { return maSach; }
    public void setMaSach(int maSach) { this.maSach = maSach; }
    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }
    public int getMaTacGia() { return maTacGia; }
    public void setMaTacGia(int maTacGia) { this.maTacGia = maTacGia; }
    public int getMaTheLoai() { return maTheLoai; }
    public void setMaTheLoai(int maTheLoai) { this.maTheLoai = maTheLoai; }
    public int getNamXuatBan() { return namXuatBan; }
    public void setNamXuatBan(int namXuatBan) { this.namXuatBan = namXuatBan; }
    public int getSoLuongTong() { return soLuongTong; }
    public void setSoLuongTong(int soLuongTong) { this.soLuongTong = soLuongTong; }
    public int getSoLuongCon() { return soLuongCon; }
    public void setSoLuongCon(int soLuongCon) { this.soLuongCon = soLuongCon; }
    public BigDecimal getGiaBia() { return giaBia; }
    public void setGiaBia(BigDecimal giaBia) { this.giaBia = giaBia; }
}
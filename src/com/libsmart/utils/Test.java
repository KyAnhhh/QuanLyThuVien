package com.libsmart.utils;

import com.libsmart.dao.SachDAO;
import com.libsmart.model.Sach;
import java.sql.SQLException;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        SachDAO dao = new SachDAO();
        try {
            List<Sach> list = dao.getAllSach();

            System.out.println("=== DANH SÁCH SÁCH TRONG CSDL ===");
            for (Sach s : list) {
                System.out.println("Mã: " + s.getMaSach() + " | Tên: " + s.getTenSach() + " | Còn: " + s.getSoLuongCon());
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
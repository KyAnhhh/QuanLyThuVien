package com.libsmart.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
private static final String URL = "jdbc:mysql://localhost:3306/QuanLyThuVien?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";    
private static final String USER = "root"; // Đổi thành user MySQL của bạn
   private static final String PASSWORD = "kyanh3305"; // Đổi thành mật khẩu MySQL của bạn

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
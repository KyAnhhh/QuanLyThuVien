package com.libsmart.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/QuanLyThuVien?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "123456";

    private static String url;
    private static String user;
    private static String password;

    static {
        reloadConfig();
    }

    public static synchronized void reloadConfig() {
        Properties props = new Properties();
        Path externalConfig = Paths.get("config.properties");

        if (Files.isRegularFile(externalConfig)) {
            try (InputStream in = Files.newInputStream(externalConfig)) {
                props.load(in);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Không đọc được file config.properties, dùng cấu hình mặc định.", e);
            }
        } else {
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (in != null) {
                    props.load(in);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Không đọc được config.properties từ classpath, dùng cấu hình mặc định.", e);
            }
        }

        url = props.getProperty("db.url", DEFAULT_URL).trim();
        user = props.getProperty("db.user", DEFAULT_USER).trim();
        password = props.getProperty("db.password", DEFAULT_PASSWORD);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

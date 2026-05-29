package com.bmgf.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class MySQLService {
    private final String mysqlUrl = "jdbc:mysql://47.117.70.79:3306/?useSSL=false&serverTimezone=UTC";
    private final String rootUser = "root";
    private final String rootPass = "123456";
    public void createDatabaseIfNotExists(String dbName) {
        String sql = "CREATE DATABASE IF NOT EXISTS `" + dbName + "`";
        try (Connection conn = DriverManager.getConnection(mysqlUrl, rootUser, rootPass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("创建数据库失败: " + e.getMessage());
        }
    }

    public void dropDatabaseIfExists(String dbName) {
        String sql = "DROP DATABASE IF EXISTS `" + dbName + "`";
        try (Connection conn = DriverManager.getConnection(mysqlUrl, rootUser, rootPass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("删除数据库失败: " + e.getMessage());
        }
    }
}
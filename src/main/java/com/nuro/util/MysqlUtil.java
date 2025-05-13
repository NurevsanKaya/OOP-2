package com.nuro.util;

import com.nuro.ConfigReader;

import java.sql.Connection;
import java.sql.DriverManager;

public class MysqlUtil {

    public static Connection getConnection() {
        try {
            String host = ConfigReader.get("mysql.host");
            String port = ConfigReader.get("mysql.port");
            String db = ConfigReader.get("mysql.database");
            String user = ConfigReader.get("mysql.username");
            String pass = ConfigReader.get("mysql.password");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&serverTimezone=UTC";
            return DriverManager.getConnection(url, user, pass);

        } catch (Exception e) {
            System.out.println("MySQL bağlantı hatası:");
            e.printStackTrace();
            return null;
        }
    }
}

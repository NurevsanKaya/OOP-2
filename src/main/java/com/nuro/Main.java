package com.nuro;
import com.nuro.ConfigReader;
import com.nuro.arayuz.GirisEkrani;
import com.nuro.util.MongoDBUtil;
import com.nuro.util.MysqlUtil;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
       new GirisEkrani();

        String mod = ConfigReader.get("db.mode");

        if ("mongodb".equalsIgnoreCase(mod)) {
            System.out.println("MongoDB seçildi.");
            System.out.println("DB adı: " + MongoDBUtil.getDatabase().getName());
        } else {
            System.out.println("MySQL seçildi.");
            try (Connection conn = MysqlUtil.getConnection()) {
                System.out.println("Bağlantı başarılı: " + !conn.isClosed());
            } catch (Exception e) {
                System.out.println("MySQL bağlantısı başarısız.");
            }
        }



    }
}
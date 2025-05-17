package com.nuro.arayuz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// MongoDB için eklemen gerekenler:
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

// config.properties dosyasını okumak için:
import java.io.FileInputStream;
import java.util.Properties;

public class KullaniciYonetimPaneli extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private Properties config;

    public KullaniciYonetimPaneli() {
        // Config dosyasını yükle
        try {
            config = new Properties();
            config.load(new FileInputStream("src/main/resources/config.properties"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Config dosyası yüklenemedi!");
            return;
        }

        setTitle("Kullanıcı Yönetimi");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // === Tablo Modeli ===
        String[] kolonlar = {"ID", "Ad", "Email", "Role"};
        model = new DefaultTableModel(kolonlar, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // === Geri Butonu ===
        JButton btnGeri = new JButton("⬅ Geri");
        btnGeri.addActionListener(e -> dispose());

        JPanel altPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        altPanel.add(btnGeri);

        add(scrollPane, BorderLayout.CENTER);
        add(altPanel, BorderLayout.SOUTH);

        // === Verileri Yükle ===
        kullanicilariYukle();
    }

    private void kullanicilariYukle() {
        String dbMode = config.getProperty("db.mode");

        if (dbMode.equalsIgnoreCase("mongodb")) {
            // === MongoDB'den veri çek
            try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                MongoDatabase database = mongoClient.getDatabase("proje_yonetim");
                MongoCollection<Document> collection = database.getCollection("users");

                for (Document doc : collection.find()) {
                    String id = doc.getObjectId("_id").toHexString();
                    String ad = doc.getString("username");
                    String email = doc.getString("email");
                    String role = doc.getString("role");

                    model.addRow(new Object[]{id, ad, email, role});
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "MongoDB bağlantı hatası: " + e.getMessage());
            }

        } else {
            // === MySQL'den veri çek
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                String sql = "SELECT id, username, email, role FROM users";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String ad = rs.getString("username");
                    String email = rs.getString("email");
                    String role = rs.getString("role");

                    model.addRow(new Object[]{id, ad, email, role});
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "MySQL hata: " + e.getMessage());
            }
        }
    }
}


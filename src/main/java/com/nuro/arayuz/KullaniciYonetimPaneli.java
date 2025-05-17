package com.nuro.arayuz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;


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

        // === Butonlar ===
        JButton btnGeri = new JButton("⬅ Geri");
        JButton btnEkle = new JButton("➕ Kullanıcı Ekle");
        JButton btnSil = new JButton("🗑 Kullanıcı Sil");
        JButton btnGuncelle = new JButton("✏ Kullanıcı Güncelle");

// === Aksiyonlar ===
        btnGeri.addActionListener(e -> dispose());
        btnEkle.addActionListener(e -> kullaniciEkle());
        btnSil.addActionListener(e -> kullaniciSil());
        btnGuncelle.addActionListener(e -> kullaniciGuncelle());



// === Alt Panel (Buton Grubu) ===
        JPanel altPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        altPanel.add(btnGeri);
        altPanel.add(btnEkle);
        altPanel.add(btnSil);
        altPanel.add(btnGuncelle);

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
    private void kullaniciEkle() {
        JTextField tfAd = new JTextField();
        JTextField tfEmail = new JTextField();
        JTextField tfRole = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Ad:"));
        panel.add(tfAd);
        panel.add(new JLabel("Email:"));
        panel.add(tfEmail);
        panel.add(new JLabel("Rol:"));
        panel.add(tfRole);

        int result = JOptionPane.showConfirmDialog(this, panel, "Yeni Kullanıcı Ekle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String ad = tfAd.getText();
            String email = tfEmail.getText();
            String rol = tfRole.getText();

            if (config.getProperty("db.mode").equalsIgnoreCase("mongodb")) {
                try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                    MongoDatabase database = mongoClient.getDatabase("proje_yonetim");
                    MongoCollection<Document> collection = database.getCollection("users");
                    Document yeni = new Document("username", ad)
                            .append("email", email)
                            .append("role", rol);
                    collection.insertOne(yeni);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Mongo ekleme hatası: " + e.getMessage());
                }
            } else {
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                    String sql = "INSERT INTO users (username, email, role) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, ad);
                    stmt.setString(2, email);
                    stmt.setString(3, rol);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "MySQL ekleme hatası: " + e.getMessage());
                }
            }

            model.setRowCount(0); // tabloyu temizle
            kullanicilariYukle(); // tekrar yükle
        }
    }
    private void kullaniciSil() {
        int seciliSatir = table.getSelectedRow();
        if (seciliSatir == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silmek için bir kullanıcı seçin.");
            return;
        }

        String id = model.getValueAt(seciliSatir, 0).toString();

        int onay = JOptionPane.showConfirmDialog(this, "Seçili kullanıcı silinsin mi?", "Onay", JOptionPane.YES_NO_OPTION);
        if (onay != JOptionPane.YES_OPTION) return;

        if (config.getProperty("db.mode").equalsIgnoreCase("mongodb")) {
            try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                MongoDatabase database = mongoClient.getDatabase("proje_yonetim");
                MongoCollection<Document> collection = database.getCollection("users");
                collection.deleteOne(new Document("_id", new org.bson.types.ObjectId(id)));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Mongo silme hatası: " + e.getMessage());
            }
        } else {
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                String sql = "DELETE FROM users WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(id));
                stmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "MySQL silme hatası: " + e.getMessage());
            }
        }

        model.removeRow(seciliSatir); // tablodan kaldır
    }

    private void kullaniciGuncelle() {
        int seciliSatir = table.getSelectedRow();
        if (seciliSatir == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellemek için bir kullanıcı seçin.");
            return;
        }

        String id = model.getValueAt(seciliSatir, 0).toString();
        String mevcutAd = model.getValueAt(seciliSatir, 1).toString();
        String mevcutEmail = model.getValueAt(seciliSatir, 2).toString();
        String mevcutRol = model.getValueAt(seciliSatir, 3).toString();

        JTextField tfAd = new JTextField(mevcutAd);
        JTextField tfEmail = new JTextField(mevcutEmail);
        JTextField tfRole = new JTextField(mevcutRol);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Ad:"));
        panel.add(tfAd);
        panel.add(new JLabel("Email:"));
        panel.add(tfEmail);
        panel.add(new JLabel("Rol:"));
        panel.add(tfRole);

        int result = JOptionPane.showConfirmDialog(this, panel, "Kullanıcı Güncelle", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String yeniAd = tfAd.getText();
            String yeniEmail = tfEmail.getText();
            String yeniRol = tfRole.getText();

            if (config.getProperty("db.mode").equalsIgnoreCase("mongodb")) {
                try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                    MongoDatabase database = mongoClient.getDatabase("proje_yonetim");
                    MongoCollection<Document> collection = database.getCollection("users");

                    Document filtre = new Document("_id", new org.bson.types.ObjectId(id));
                    Document yeniVeriler = new Document("username", yeniAd)
                            .append("email", yeniEmail)
                            .append("role", yeniRol);

                    collection.updateOne(filtre, new Document("$set", yeniVeriler));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Mongo güncelleme hatası: " + e.getMessage());
                }
            } else {
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                    String sql = "UPDATE users SET username=?, email=?, role=? WHERE id=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, yeniAd);
                    stmt.setString(2, yeniEmail);
                    stmt.setString(3, yeniRol);
                    stmt.setInt(4, Integer.parseInt(id));
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "MySQL güncelleme hatası: " + e.getMessage());
                }
            }

            model.setRowCount(0); // tabloyu temizle
            kullanicilariYukle(); // tekrar yükle
        }
    }

}


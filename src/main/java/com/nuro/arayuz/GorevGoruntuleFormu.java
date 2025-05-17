package com.nuro.arayuz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

public class GorevGoruntuleFormu extends JFrame {
    private String kullaniciAdi;
    private Properties config;
    private JTable table;
    private DefaultTableModel model;

    public GorevGoruntuleFormu(String kullaniciAdi, Properties config) {
        this.kullaniciAdi = kullaniciAdi;
        this.config = config;

        setTitle("Görevlerim");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Tablo modeli
        String[] kolonlar = {"Proje Adı", "Görev", "Başlangıç Tarihi", "Bitiş Tarihi"};
        model = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabloyu düzenlenemez yap
            }
        };
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // Butonlar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnDuzenle = new JButton("Düzenle");
        JButton btnSil = new JButton("Sil");
        JButton btnKapat = new JButton("Kapat");

        btnDuzenle.addActionListener(e -> gorevDuzenle());
        btnSil.addActionListener(e -> gorevSil());
        btnKapat.addActionListener(e -> dispose());

        buttonPanel.add(btnDuzenle);
        buttonPanel.add(btnSil);
        buttonPanel.add(btnKapat);

        // Görevleri yükle
        gorevleriYukle();

        // Panel düzeni
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private void gorevleriYukle() {
        model.setRowCount(0); // Tabloyu temizle
        String dbMode = config.getProperty("db.mode");
        if ("mongodb".equalsIgnoreCase(dbMode)) {
            try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                MongoDatabase db = mongoClient.getDatabase("proje_yonetim");
                MongoCollection<Document> collection = db.getCollection("projects");

                for (Document doc : collection.find()) {
                    for (Document responsible : doc.getList("responsibles", Document.class)) {
                        if (responsible.getString("username").equals(kullaniciAdi)) {
                            model.addRow(new Object[]{
                                doc.getString("name"),
                                responsible.getString("task"),
                                doc.getString("start_date"),
                                doc.getString("end_date")
                            });
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Görevler yüklenirken hata oluştu: " + e.getMessage());
            }
        } else {
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                String sql = "SELECT p.name, pu.task, p.start_date, p.end_date " +
                           "FROM projects p " +
                           "JOIN project_users pu ON p.id = pu.project_id " +
                           "WHERE pu.username = ?";
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, kullaniciAdi);
                java.sql.ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("task"),
                        rs.getString("start_date"),
                        rs.getString("end_date")
                    });
                }
            } catch (java.sql.SQLException e) {
                JOptionPane.showMessageDialog(this, "Görevler yüklenirken hata oluştu: " + e.getMessage());
            }
        }
    }

    private void gorevDuzenle() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen düzenlemek için bir görev seçin.");
            return;
        }

        String projeAdi = (String) model.getValueAt(selectedRow, 0);
        String gorev = (String) model.getValueAt(selectedRow, 1);
        String baslangic = (String) model.getValueAt(selectedRow, 2);
        String bitis = (String) model.getValueAt(selectedRow, 3);

        // Düzenleme formu
        JTextField tfGorev = new JTextField(gorev);
        JTextField tfBaslangic = new JTextField(baslangic);
        JTextField tfBitis = new JTextField(bitis);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Görev:"));
        panel.add(tfGorev);
        panel.add(new JLabel("Başlangıç Tarihi (yyyy-MM-dd):"));
        panel.add(tfBaslangic);
        panel.add(new JLabel("Bitiş Tarihi (yyyy-MM-dd):"));
        panel.add(tfBitis);

        int result = JOptionPane.showConfirmDialog(this, panel, "Görev Düzenle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String dbMode = config.getProperty("db.mode");
            if ("mongodb".equalsIgnoreCase(dbMode)) {
                try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                    MongoDatabase db = mongoClient.getDatabase("proje_yonetim");
                    MongoCollection<Document> collection = db.getCollection("projects");

                    Document query = new Document("name", projeAdi);
                    Document update = new Document("$set", new Document("responsibles.$[elem].task", tfGorev.getText())
                            .append("start_date", tfBaslangic.getText())
                            .append("end_date", tfBitis.getText()));

                    collection.updateOne(query, update);
                    JOptionPane.showMessageDialog(this, "Görev başarıyla güncellendi.");
                    gorevleriYukle();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Güncelleme hatası: " + e.getMessage());
                }
            } else {
                try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                    String sql = "UPDATE project_users SET task = ? WHERE project_id = (SELECT id FROM projects WHERE name = ?) AND username = ?";
                    java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, tfGorev.getText());
                    stmt.setString(2, projeAdi);
                    stmt.setString(3, kullaniciAdi);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Görev başarıyla güncellendi.");
                    gorevleriYukle();
                } catch (java.sql.SQLException e) {
                    JOptionPane.showMessageDialog(this, "Güncelleme hatası: " + e.getMessage());
                }
            }
        }
    }

    private void gorevSil() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silmek için bir görev seçin.");
            return;
        }

        String projeAdi = (String) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bu görevi silmek istediğinizden emin misiniz?",
                "Görev Silme",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String dbMode = config.getProperty("db.mode");
            if ("mongodb".equalsIgnoreCase(dbMode)) {
                try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                    MongoDatabase db = mongoClient.getDatabase("proje_yonetim");
                    MongoCollection<Document> collection = db.getCollection("projects");

                    Document query = new Document("name", projeAdi);
                    Document update = new Document("$pull", new Document("responsibles", 
                            new Document("username", kullaniciAdi)));
                    collection.updateOne(query, update);
                    JOptionPane.showMessageDialog(this, "Görev başarıyla silindi.");
                    gorevleriYukle();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Silme hatası: " + e.getMessage());
                }
            } else {
                try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                    String sql = "DELETE FROM project_users WHERE project_id = (SELECT id FROM projects WHERE name = ?) AND username = ?";
                    java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, projeAdi);
                    stmt.setString(2, kullaniciAdi);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Görev başarıyla silindi.");
                    gorevleriYukle();
                } catch (java.sql.SQLException e) {
                    JOptionPane.showMessageDialog(this, "Silme hatası: " + e.getMessage());
                }
            }
        }
    }
} 
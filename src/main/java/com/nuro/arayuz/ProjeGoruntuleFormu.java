package com.nuro.arayuz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

public class ProjeGoruntuleFormu extends JFrame {
    private String kullaniciAdi;
    private Properties config;
    private JTable table;
    private DefaultTableModel model;

    public ProjeGoruntuleFormu(String kullaniciAdi, Properties config) {
        this.kullaniciAdi = kullaniciAdi;
        this.config = config;

        setTitle("Projelerim");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Tablo modeli
        String[] kolonlar = {"Proje Adı", "Açıklama", "Başlangıç Tarihi", "Bitiş Tarihi", "Görevim"};
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

        btnDuzenle.addActionListener(e -> projeDuzenle());
        btnSil.addActionListener(e -> projeSil());
        btnKapat.addActionListener(e -> dispose());

        buttonPanel.add(btnDuzenle);
        buttonPanel.add(btnSil);
        buttonPanel.add(btnKapat);

        // Projeleri yükle
        projeleriYukle();

        // Panel düzeni
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private void projeleriYukle() {
        model.setRowCount(0); // Tabloyu temizle
        String dbMode = config.getProperty("db.mode");
        if ("mongodb".equalsIgnoreCase(dbMode)) {
            try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                MongoDatabase db = mongoClient.getDatabase("proje_yonetim");
                MongoCollection<Document> collection = db.getCollection("projects");

                for (Document doc : collection.find()) {
                    // Kullanıcının görevli olduğu projeleri bul
                    for (Document responsible : doc.getList("responsibles", Document.class)) {
                        if (responsible.getString("username").equals(kullaniciAdi)) {
                            model.addRow(new Object[]{
                                doc.getString("name"),
                                doc.getString("description"),
                                doc.getString("start_date"),
                                doc.getString("end_date"),
                                responsible.getString("task")
                            });
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Projeler yüklenirken hata oluştu: " + e.getMessage());
            }
        } else {
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                String sql = "SELECT p.id, p.name, p.description, p.start_date, p.end_date, pu.task " +
                           "FROM projects p " +
                           "JOIN project_users pu ON p.id = pu.project_id " +
                           "WHERE pu.username = ?";
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, kullaniciAdi);
                java.sql.ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getString("task")
                    });
                }
            } catch (java.sql.SQLException e) {
                JOptionPane.showMessageDialog(this, "Projeler yüklenirken hata oluştu: " + e.getMessage());
            }
        }
    }

    private void projeDuzenle() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen düzenlemek için bir proje seçin.");
            return;
        }

        String projeAdi = (String) model.getValueAt(selectedRow, 0);
        String aciklama = (String) model.getValueAt(selectedRow, 1);
        String baslangic = (String) model.getValueAt(selectedRow, 2);
        String bitis = (String) model.getValueAt(selectedRow, 3);
        String gorev = (String) model.getValueAt(selectedRow, 4);

        // Düzenleme formu
        JTextField tfProjeAdi = new JTextField(projeAdi);
        JTextArea taAciklama = new JTextArea(aciklama, 3, 20);
        JTextField tfBaslangic = new JTextField(baslangic);
        JTextField tfBitis = new JTextField(bitis);
        JTextField tfGorev = new JTextField(gorev);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Proje Adı:"));
        panel.add(tfProjeAdi);
        panel.add(new JLabel("Açıklama:"));
        panel.add(new JScrollPane(taAciklama));
        panel.add(new JLabel("Başlangıç Tarihi (yyyy-MM-dd):"));
        panel.add(tfBaslangic);
        panel.add(new JLabel("Bitiş Tarihi (yyyy-MM-dd):"));
        panel.add(tfBitis);
        panel.add(new JLabel("Görev:"));
        panel.add(tfGorev);

        int result = JOptionPane.showConfirmDialog(this, panel, "Proje Düzenle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String dbMode = config.getProperty("db.mode");
            if ("mongodb".equalsIgnoreCase(dbMode)) {
                try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                    MongoDatabase db = mongoClient.getDatabase("proje_yonetim");
                    MongoCollection<Document> collection = db.getCollection("projects");

                    Document query = new Document("name", projeAdi);
                    Document update = new Document("$set", new Document()
                            .append("name", tfProjeAdi.getText())
                            .append("description", taAciklama.getText())
                            .append("start_date", tfBaslangic.getText())
                            .append("end_date", tfBitis.getText()));

                    collection.updateOne(query, update);
                    JOptionPane.showMessageDialog(this, "Proje başarıyla güncellendi.");
                    projeleriYukle();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Güncelleme hatası: " + e.getMessage());
                }
            } else {
                try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                    String sql = "UPDATE projects SET name = ?, description = ?, start_date = ?, end_date = ? WHERE name = ?";
                    java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, tfProjeAdi.getText());
                    stmt.setString(2, taAciklama.getText());
                    stmt.setString(3, tfBaslangic.getText());
                    stmt.setString(4, tfBitis.getText());
                    stmt.setString(5, projeAdi);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Proje başarıyla güncellendi.");
                    projeleriYukle();
                } catch (java.sql.SQLException e) {
                    JOptionPane.showMessageDialog(this, "Güncelleme hatası: " + e.getMessage());
                }
            }
        }
    }

    private void projeSil() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silmek için bir proje seçin.");
            return;
        }

        String projeAdi = (String) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bu projeyi silmek istediğinizden emin misiniz?",
                "Proje Silme",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String dbMode = config.getProperty("db.mode");
            if ("mongodb".equalsIgnoreCase(dbMode)) {
                try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                    MongoDatabase db = mongoClient.getDatabase("proje_yonetim");
                    MongoCollection<Document> collection = db.getCollection("projects");

                    Document query = new Document("name", projeAdi);
                    collection.deleteOne(query);
                    JOptionPane.showMessageDialog(this, "Proje başarıyla silindi.");
                    projeleriYukle();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Silme hatası: " + e.getMessage());
                }
            } else {
                try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                    String sql = "DELETE FROM projects WHERE name = ?";
                    java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, projeAdi);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Proje başarıyla silindi.");
                    projeleriYukle();
                } catch (java.sql.SQLException e) {
                    JOptionPane.showMessageDialog(this, "Silme hatası: " + e.getMessage());
                }
            }
        }
    }
} 
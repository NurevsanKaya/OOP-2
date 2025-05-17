package com.nuro.arayuz;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.List;
import java.util.Properties;

public class ProjeEkleFormu extends JFrame {
    private Properties config;

    public ProjeEkleFormu(Properties config) {
        this.config = config;

        setTitle("Proje Ekle");
        setSize(450, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTextField tfAd = new JTextField();
        JTextArea taAciklama = new JTextArea(3, 20);
        JTextField tfBaslangic = new JTextField("2025-05-17");
        JTextField tfBitis = new JTextField();

        DefaultListModel<String> userModel = new DefaultListModel<>();
        JList<String> listUsers = new JList<>(userModel);
        listUsers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Kullanıcıları yükle
        String dbMode = config.getProperty("db.mode");
        if ("mongodb".equalsIgnoreCase(dbMode)) {
            try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                MongoDatabase database = mongoClient.getDatabase("proje_yonetim");
                MongoCollection<Document> collection = database.getCollection("users");
                
                for (Document doc : collection.find()) {
                    userModel.addElement(doc.getString("username"));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "MongoDB: Kullanıcılar yüklenemedi: " + e.getMessage());
            }
        } else {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT username FROM users");
                while (rs.next()) {
                    userModel.addElement(rs.getString("username"));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "MySQL: Kullanıcılar yüklenemedi: " + e.getMessage());
            }
        }

        JButton btnEkle = new JButton("Kaydet");

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.add(new JLabel("Proje Adı:"));
        panel.add(tfAd);
        panel.add(new JLabel("Açıklama:"));
        panel.add(new JScrollPane(taAciklama));
        panel.add(new JLabel("Başlangıç Tarihi (yyyy-mm-dd):"));
        panel.add(tfBaslangic);
        panel.add(new JLabel("Bitiş Tarihi (yyyy-mm-dd):"));
        panel.add(tfBitis);
        panel.add(new JLabel("Görevli Kişiler:"));
        panel.add(new JScrollPane(listUsers));

        add(panel, BorderLayout.CENTER);
        add(btnEkle, BorderLayout.SOUTH);

        btnEkle.addActionListener(e -> {
            String ad = tfAd.getText().trim();
            String aciklama = taAciklama.getText().trim();
            String baslangic = tfBaslangic.getText().trim();
            String bitis = tfBitis.getText().trim();
            List<String> secilenKullanicilar = listUsers.getSelectedValuesList();

            if (ad.isEmpty() || baslangic.isEmpty() || secilenKullanicilar.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen tüm alanları ve en az bir görevliyi seçin.");
                return;
            }

            List<Map<String, String>> responsibleList = new ArrayList<>();

            for (String user : secilenKullanicilar) {
                String task = JOptionPane.showInputDialog(this, user + " için görev:");
                if (task != null && !task.trim().isEmpty()) {
                    Map<String, String> g = new HashMap<>();
                    g.put("username", user);
                    g.put("task", task);
                    responsibleList.add(g);
                }
            }

            if ("mongodb".equalsIgnoreCase(dbMode)) {
                try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                    MongoDatabase db = mongoClient.getDatabase("proje_yonetim");
                    MongoCollection<Document> collection = db.getCollection("projects");

                    List<Document> responsibles = new ArrayList<>();
                    for (Map<String, String> map : responsibleList) {
                        responsibles.add(new Document(map));
                    }

                    Document doc = new Document("name", ad)
                            .append("description", aciklama)
                            .append("start_date", baslangic)
                            .append("end_date", bitis)
                            .append("responsibles", responsibles);

                    collection.insertOne(doc);
                    JOptionPane.showMessageDialog(this, "MongoDB: Proje eklendi.");
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Mongo hata: " + ex.getMessage());
                }

            } else {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                    String insertProject = "INSERT INTO projects (name, description, start_date, end_date) VALUES (?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(insertProject, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, ad);
                    ps.setString(2, aciklama);
                    ps.setString(3, baslangic);
                    ps.setString(4, bitis);
                    ps.executeUpdate();

                    ResultSet keys = ps.getGeneratedKeys();
                    int projectId = -1;
                    if (keys.next()) {
                        projectId = keys.getInt(1);
                    }

                    // Görevli kişileri ekle
                    for (Map<String, String> map : responsibleList) {
                        String insertUser = "INSERT INTO project_users (project_id, username, task) VALUES (?, ?, ?)";
                        PreparedStatement ups = conn.prepareStatement(insertUser);
                        ups.setInt(1, projectId);
                        ups.setString(2, map.get("username"));
                        ups.setString(3, map.get("task"));
                        ups.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(this, "MySQL: Proje ve görevli kişiler eklendi.");
                    dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "MySQL hata: " + ex.getMessage());
                }
            }
        });
    }
}

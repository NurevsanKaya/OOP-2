package com.nuro.arayuz;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import com.mongodb.client.*;
import org.bson.Document;
import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

public class ProjeEkleFormu extends JFrame {
    private Properties config;
    private String kullaniciAdi;
    private static final int MIN_WIDTH = 600;
    private static final int MIN_HEIGHT = 700;

    public ProjeEkleFormu(Properties config, String kullaniciAdi) {
        this.config = config;
        this.kullaniciAdi = kullaniciAdi;

        setTitle("Proje Ekle");
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTextField tfAd = new JTextField();
        JTextArea taAciklama = new JTextArea(3, 20);
        JDateChooser dateBaslangic = new JDateChooser();
        JDateChooser dateBitis = new JDateChooser();
        
        // Tarih formatını ayarla
        dateBaslangic.setDate(new Date());
        dateBaslangic.setDateFormatString("yyyy-MM-dd");
        dateBitis.setDateFormatString("yyyy-MM-dd");

        DefaultListModel<String> userModel = new DefaultListModel<>();
        JList<String> listUsers = new JList<>(userModel);
        listUsers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listUsers.setVisibleRowCount(5); // Görünür satır sayısı

        // Kullanıcıları yükle
        String dbMode = config.getProperty("db.mode");
        if ("mongodb".equalsIgnoreCase(dbMode)) {
            try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                MongoDatabase database = mongoClient.getDatabase("proje_yonetim");
                MongoCollection<Document> collection = database.getCollection("users");

                for (Document doc : collection.find()) {
                    String username = doc.getString("username");
                    String email = doc.getString("email");
                    userModel.addElement(username + " (" + email + ")");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "MongoDB: Kullanıcılar yüklenemedi: " + e.getMessage());
            }
        } else {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT username, email FROM users");
                while (rs.next()) {
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    userModel.addElement(username + " (" + email + ")");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "MySQL: Kullanıcılar yüklenemedi: " + e.getMessage());
            }
        }

        JButton btnEkle = new JButton("Kaydet");
        btnEkle.setBackground(new Color(70, 130, 180));
        btnEkle.setForeground(Color.WHITE);
        btnEkle.setFocusPainted(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Proje Adı
        JPanel adPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        adPanel.add(new JLabel("Proje Adı:"));
        tfAd.setPreferredSize(new Dimension(300, 30));
        adPanel.add(tfAd);
        panel.add(adPanel);
        panel.add(Box.createVerticalStrut(15));

        // Açıklama
        JPanel aciklamaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aciklamaPanel.add(new JLabel("Açıklama:"));
        taAciklama.setPreferredSize(new Dimension(300, 100));
        aciklamaPanel.add(new JScrollPane(taAciklama));
        panel.add(aciklamaPanel);
        panel.add(Box.createVerticalStrut(15));

        // Başlangıç Tarihi
        JPanel baslangicPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        baslangicPanel.add(new JLabel("Başlangıç Tarihi:"));
        dateBaslangic.setPreferredSize(new Dimension(200, 30));
        baslangicPanel.add(dateBaslangic);
        panel.add(baslangicPanel);
        panel.add(Box.createVerticalStrut(15));

        // Bitiş Tarihi
        JPanel bitisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bitisPanel.add(new JLabel("Bitiş Tarihi:"));
        dateBitis.setPreferredSize(new Dimension(200, 30));
        bitisPanel.add(dateBitis);
        panel.add(bitisPanel);
        panel.add(Box.createVerticalStrut(15));

        // Görevli Kişiler
        JPanel kullanicilarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        kullanicilarPanel.add(new JLabel("Görevli Kişiler (Birden fazla seçim için CTRL tuşuna basılı tutun):"));
        JScrollPane scrollPane = new JScrollPane(listUsers);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        kullanicilarPanel.add(scrollPane);
        panel.add(kullanicilarPanel);

        add(panel, BorderLayout.CENTER);
        add(btnEkle, BorderLayout.SOUTH);

        btnEkle.addActionListener(e -> {
            String ad = tfAd.getText().trim();
            String aciklama = taAciklama.getText().trim();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String baslangic = dateBaslangic.getDate() != null ? sdf.format(dateBaslangic.getDate()) : "";
            String bitis = dateBitis.getDate() != null ? sdf.format(dateBitis.getDate()) : "";
            List<String> secilenKullanicilar = listUsers.getSelectedValuesList();

            if (ad.isEmpty() || baslangic.isEmpty() || secilenKullanicilar.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen tüm alanları ve en az bir görevliyi seçin.");
                return;
            }

            List<Map<String, String>> responsibleList = new ArrayList<>();

            for (String userWithEmail : secilenKullanicilar) {
                // Email parantezini kaldır ve sadece kullanıcı adını al
                String username = userWithEmail.substring(0, userWithEmail.indexOf(" ("));
                String task = JOptionPane.showInputDialog(this, username + " için görev:");
                if (task != null && !task.trim().isEmpty()) {
                    Map<String, String> g = new HashMap<>();
                    g.put("username", username);
                    g.put("task", task);
                    responsibleList.add(g);
                }
            }

            if ("mongodb".equalsIgnoreCase(dbMode)) {
                try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                    MongoDatabase database = mongoClient.getDatabase("proje_yonetim");
                    MongoCollection<Document> collection = database.getCollection("projects");

                    List<Document> responsibles = new ArrayList<>();
                    for (Map<String, String> map : responsibleList) {
                        responsibles.add(new Document(map));
                    }

                    Document doc = new Document("name", ad)
                            .append("description", aciklama)
                            .append("start_date", baslangic)
                            .append("end_date", bitis)
                            .append("responsibles", responsibles)
                            .append("created_by", kullaniciAdi);

                    collection.insertOne(doc);
                    JOptionPane.showMessageDialog(this, "Proje başarıyla eklendi.");
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
                }
            } else {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/proje_yonetim", "root", "")) {
                    conn.setAutoCommit(false);
                    try {
                        String insertProject = "INSERT INTO projects (name, description, start_date, end_date, created_by) VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement ps = conn.prepareStatement(insertProject, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, ad);
                        ps.setString(2, aciklama);
                        ps.setString(3, baslangic);
                        ps.setString(4, bitis);
                        ps.setString(5, kullaniciAdi);
                        ps.executeUpdate();

                        ResultSet keys = ps.getGeneratedKeys();
                        int projectId = -1;
                        if (keys.next()) {
                            projectId = keys.getInt(1);
                        }

                        for (Map<String, String> map : responsibleList) {
                            String insertUser = "INSERT INTO project_users (project_id, username, task) VALUES (?, ?, ?)";
                            PreparedStatement ups = conn.prepareStatement(insertUser);
                            ups.setInt(1, projectId);
                            ups.setString(2, map.get("username"));
                            ups.setString(3, map.get("task"));
                            ups.executeUpdate();
                        }

                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Proje başarıyla eklendi.");
                        dispose();
                    } catch (Exception ex) {
                        conn.rollback();
                        throw ex;
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
                }
            }
        });
    }
}

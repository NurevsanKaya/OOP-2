package com.nuro.arayuz;

import com.nuro.util.MongoDBUtil;
import com.nuro.util.SifrelemeUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;

public class KayitEkrani extends JFrame {
    private JTextField kullaniciAdiAlani;
    private JPasswordField sifreAlani;
    private JComboBox<String> rolSecimi;
    private JButton kayitButonu;

    public KayitEkrani() {
        setTitle("Kayıt Ol");
        setSize(350, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel anaPanel = new JPanel();
        anaPanel.setLayout(new BoxLayout(anaPanel, BoxLayout.Y_AXIS));
        anaPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        anaPanel.setBackground(new Color(245, 245, 245));

        JLabel baslik = new JLabel("Yeni Kullanıcı Kaydı");
        baslik.setFont(new Font("Arial", Font.BOLD, 20));
        baslik.setAlignmentX(Component.CENTER_ALIGNMENT);
        anaPanel.add(baslik);
        anaPanel.add(Box.createVerticalStrut(20));

        JLabel kullaniciAdiEtiketi = new JLabel("Kullanıcı Adı:");
        kullaniciAdiEtiketi.setAlignmentX(Component.CENTER_ALIGNMENT);
        anaPanel.add(kullaniciAdiEtiketi);
        kullaniciAdiAlani = new JTextField(20);
        kullaniciAdiAlani.setMaximumSize(new Dimension(200, 30));
        kullaniciAdiAlani.setAlignmentX(Component.CENTER_ALIGNMENT);
        anaPanel.add(kullaniciAdiAlani);
        anaPanel.add(Box.createVerticalStrut(10));

        JLabel sifreEtiketi = new JLabel("Şifre:");
        sifreEtiketi.setAlignmentX(Component.CENTER_ALIGNMENT);
        anaPanel.add(sifreEtiketi);
        sifreAlani = new JPasswordField(20);
        sifreAlani.setMaximumSize(new Dimension(200, 30));
        sifreAlani.setAlignmentX(Component.CENTER_ALIGNMENT);
        anaPanel.add(sifreAlani);
        anaPanel.add(Box.createVerticalStrut(10));

        JLabel rolEtiketi = new JLabel("Rol:");
        rolEtiketi.setAlignmentX(Component.CENTER_ALIGNMENT);
        anaPanel.add(rolEtiketi);
        rolSecimi = new JComboBox<>(new String[]{"kullanıcı", "ADMIN"});
        rolSecimi.setMaximumSize(new Dimension(200, 30));
        rolSecimi.setAlignmentX(Component.CENTER_ALIGNMENT);
        anaPanel.add(rolSecimi);
        anaPanel.add(Box.createVerticalStrut(20));

        kayitButonu = new JButton("Kayıt Ol");
        kayitButonu.setAlignmentX(Component.CENTER_ALIGNMENT);
        kayitButonu.setBackground(new Color(70, 130, 180));
        kayitButonu.setForeground(Color.WHITE);
        kayitButonu.setFocusPainted(false);
        kayitButonu.addActionListener(e -> kayitOl());
        anaPanel.add(kayitButonu);

        add(anaPanel);
        setVisible(true);
    }

    private void kayitOl() {
        String kullaniciAdi = kullaniciAdiAlani.getText().trim();
        String sifre = new String(sifreAlani.getPassword());
        String rol = (String) rolSecimi.getSelectedItem();

        if (kullaniciAdi.isEmpty() || sifre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MongoDatabase veritabani = MongoDBUtil.getDatabase();
        MongoCollection<Document> kullanicilar = veritabani.getCollection("users");

        Document mevcutKullanici = kullanicilar.find(new Document("username", kullaniciAdi)).first();
        if (mevcutKullanici != null) {
            JOptionPane.showMessageDialog(this, "Bu kullanıcı adı zaten alınmış!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sifreli = SifrelemeUtil.hashSifre(sifre);
        Document yeniKullanici = new Document("username", kullaniciAdi)
                .append("password", sifreli)
                .append("role", rol);
        kullanicilar.insertOne(yeniKullanici);
        JOptionPane.showMessageDialog(this, "Kayıt başarılı! Artık giriş yapabilirsiniz.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
} 
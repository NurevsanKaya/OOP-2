package com.nuro.arayuz;
import com.nuro.util.MongoDBUtil;
import com.nuro.util.SifrelemeUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
public class GirisEkrani extends JFrame //Java Swing kütüphanesinde bir pencere (window) oluşturmamıza yarayan sınıftır.
{
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnGiris;
    private JButton btnYeniKullanici;

    public GirisEkrani() {
        setTitle("Proje Yönetim Paneli - Giriş");
        setSize(450, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Ana panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 30, 50));
        panel.setBackground(new Color(240, 240, 240));

        // Başlık
        JLabel lblBaslik = new JLabel("Proje Yönetim Paneli");
        lblBaslik.setFont(new Font("Arial", Font.BOLD, 24));
        lblBaslik.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBaslik.setForeground(new Color(51, 51, 51));

        // Giriş paneli
        JPanel girisPanel = new JPanel();
        girisPanel.setLayout(new BoxLayout(girisPanel, BoxLayout.Y_AXIS));
        girisPanel.setBackground(Color.WHITE);
        girisPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        // Kullanıcı adı
        JLabel lblUsername = new JLabel("Kullanıcı Adı:");
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        lblUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtUsername = new JTextField(20);
        txtUsername.setMaximumSize(new Dimension(300, 30));
        txtUsername.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Şifre
        JLabel lblPassword = new JLabel("Şifre:");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtPassword = new JPasswordField(20);
        txtPassword.setMaximumSize(new Dimension(300, 30));
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Butonlar
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setMaximumSize(new Dimension(300, 100));
        
        btnGiris = new JButton("Giriş Yap");
        btnGiris.setPreferredSize(new Dimension(250, 40));
        btnGiris.setMaximumSize(new Dimension(250, 40));
        btnGiris.setBackground(new Color(70, 130, 180));
        btnGiris.setForeground(Color.WHITE);
        btnGiris.setFont(new Font("Arial", Font.BOLD, 14));
        btnGiris.setFocusPainted(false);
        btnGiris.setBorderPainted(false);
        btnGiris.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnYeniKullanici = new JButton("Yeni Kullanıcı Oluştur");
        btnYeniKullanici.setPreferredSize(new Dimension(250, 40));
        btnYeniKullanici.setMaximumSize(new Dimension(250, 40));
        btnYeniKullanici.setBackground(Color.WHITE);
        btnYeniKullanici.setForeground(new Color(70, 130, 180));
        btnYeniKullanici.setFont(new Font("Arial", Font.PLAIN, 14));
        btnYeniKullanici.setFocusPainted(false);
        btnYeniKullanici.setBorderPainted(false);
        btnYeniKullanici.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Butonları ortala
        btnGiris.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnYeniKullanici.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(btnGiris);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(btnYeniKullanici);

        // Action Listeners
        btnGiris.addActionListener(e -> girisYap());
        btnYeniKullanici.addActionListener(e -> new KayitEkrani());

        // Ekleme
        girisPanel.add(lblUsername);
        girisPanel.add(Box.createVerticalStrut(5));
        girisPanel.add(txtUsername);
        girisPanel.add(Box.createVerticalStrut(15));
        girisPanel.add(lblPassword);
        girisPanel.add(Box.createVerticalStrut(5));
        girisPanel.add(txtPassword);
        girisPanel.add(Box.createVerticalStrut(30));
        girisPanel.add(buttonPanel);

        panel.add(lblBaslik);
        panel.add(Box.createVerticalStrut(30));
        panel.add(girisPanel);

        add(panel);
        setVisible(true);
    }

    private void girisYap() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Lütfen tüm alanları doldurun!", 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        MongoDatabase db = MongoDBUtil.getDatabase();
        MongoCollection<Document> users = db.getCollection("users");

        Document user = users.find(new Document("username", username)).first();

        if (user != null) {
            String hashliSifre = user.getString("password");
            if (SifrelemeUtil.dogrula(password, hashliSifre)) {
                JOptionPane.showMessageDialog(this, 
                    "Giriş başarılı! Hoş geldiniz, " + username, 
                    "Başarılı", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                String rol = user.getString("role");
                new AnaPanel(username, rol);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Şifre yanlış!", 
                    "Hata", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Kullanıcı bulunamadı!", 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void yeniKullanici() {
        // TODO: Yeni kullanıcı kayıt ekranını aç
        JOptionPane.showMessageDialog(this, 
            "Yeni kullanıcı kaydı yakında eklenecek!", 
            "Bilgi", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}

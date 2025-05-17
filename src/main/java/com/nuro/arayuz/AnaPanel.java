package com.nuro.arayuz;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.*;

public class AnaPanel extends JFrame {
    private String kullaniciAdi; 
    private String rol;

    public AnaPanel(String kullaniciAdi, String rol) {
        try {
            System.out.println("AnaPanel açılıyor: " + kullaniciAdi );
            this.kullaniciAdi = kullaniciAdi;
            this.rol = rol;
            setTitle("Proje Yönetim Paneli - Ana Sayfa");
            setSize(500, 400);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(false);

            JPanel anaPanel = new JPanel();
            anaPanel.setLayout(new BoxLayout(anaPanel, BoxLayout.Y_AXIS));
            anaPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            anaPanel.setBackground(new Color(245, 245, 245));

            JLabel hosgeldin = new JLabel("Hoş geldin, " + kullaniciAdi );
            hosgeldin.setFont(new Font("Arial", Font.BOLD, 18));
            hosgeldin.setAlignmentX(Component.CENTER_ALIGNMENT);
            anaPanel.add(hosgeldin);
            anaPanel.add(Box.createVerticalStrut(30));

            JButton projeGoruntule = new JButton("Projelerimi Görüntüle");
            projeGoruntule.setAlignmentX(Component.CENTER_ALIGNMENT);
            anaPanel.add(projeGoruntule);
            anaPanel.add(Box.createVerticalStrut(10));

            JButton gorevlerim = new JButton("Görevlerimi Görüntüle");
            gorevlerim.setAlignmentX(Component.CENTER_ALIGNMENT);
            anaPanel.add(gorevlerim);
            anaPanel.add(Box.createVerticalStrut(10));

            // Eğer admin ise ekstra butonlar
            if (rol != null && rol.equalsIgnoreCase("admin")) {//büyük küçük harf duyarsız yaptık çünkü ADMIN yazıyor veritabanında
                System.out.println("Admin butonları ekleniyor!");
                JButton projeEkle = new JButton("Proje Ekle");
                projeEkle.setAlignmentX(Component.CENTER_ALIGNMENT);
                anaPanel.add(projeEkle);
                anaPanel.add(Box.createVerticalStrut(10));

                JButton kullaniciYonet = new JButton("Kullanıcıları Yönet");
                kullaniciYonet.setAlignmentX(Component.CENTER_ALIGNMENT);
                kullaniciYonet.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                KullaniciYonetimPaneli pencere = new KullaniciYonetimPaneli();
                pencere.setVisible(true);
                }
            });
                anaPanel.add(kullaniciYonet);
                anaPanel.add(Box.createVerticalStrut(10));
            }

            add(anaPanel);
            setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Bir hata oluştu: " + e.getMessage());
        }
    }
}

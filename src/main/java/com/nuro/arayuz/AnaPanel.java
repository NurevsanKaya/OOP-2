package com.nuro.arayuz;

import javax.swing.*;
import java.awt.*;

public class AnaPanel extends JFrame {

    public AnaPanel(String kullaniciAdi) {
        setTitle("Yönetim Paneli");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Ana panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel lblHosgeldin = new JLabel("Hoş geldin, " + kullaniciAdi);
        lblHosgeldin.setFont(new Font("Arial", Font.BOLD, 16));
        lblHosgeldin.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnProjeler = new JButton("Projeleri Görüntüle");
        JButton btnGorevler = new JButton("Görevleri Görüntüle");
        JButton btnCikis = new JButton("Çıkış");

        btnProjeler.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGorevler.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCikis.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buton işlevi
        btnCikis.addActionListener(e -> {
            dispose();
            new GirisEkrani(); // Giriş ekranına geri dön
        });

        panel.add(lblHosgeldin);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnProjeler);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnGorevler);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnCikis);

        add(panel);
        setVisible(true);
    }
}

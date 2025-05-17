package com.nuro.arayuz;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.*;
import java.util.Properties;
import java.io.FileInputStream;

public class AnaPanel extends JFrame {
    private String kullaniciAdi; 
    private String rol;
    private Properties config;
    private static final int MIN_WIDTH = 600;
    private static final int MIN_HEIGHT = 500;
    private static final int BUTTON_WIDTH = 250;
    private static final int BUTTON_HEIGHT = 40;

    public AnaPanel(String kullaniciAdi, String rol) {
        try {
            // Config dosyasını yükle
            config = new Properties();
            config.load(new FileInputStream("src/main/resources/config.properties"));
            
            System.out.println("AnaPanel açılıyor: " + kullaniciAdi);
            this.kullaniciAdi = kullaniciAdi;
            this.rol = rol;
            setTitle("Proje Yönetim Paneli - Ana Sayfa");
            setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
            setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(true);

            JPanel anaPanel = new JPanel();
            anaPanel.setLayout(new BoxLayout(anaPanel, BoxLayout.Y_AXIS));
            anaPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
            anaPanel.setBackground(new Color(245, 245, 245));

            JLabel hosgeldin = new JLabel("Hoş geldin, " + kullaniciAdi);
            hosgeldin.setFont(new Font("Arial", Font.BOLD, 24));
            hosgeldin.setAlignmentX(Component.CENTER_ALIGNMENT);
            anaPanel.add(hosgeldin);
            anaPanel.add(Box.createVerticalStrut(40));

            JButton projeGoruntule = createStyledButton("Projelerimi Görüntüle");
            projeGoruntule.addActionListener(e -> {
                new ProjeGoruntuleFormu(kullaniciAdi, config).setVisible(true);
            });
            anaPanel.add(projeGoruntule);
            anaPanel.add(Box.createVerticalStrut(15));

            JButton gorevlerim = createStyledButton("Görevlerimi Görüntüle");
            gorevlerim.addActionListener(e -> {
                new GorevGoruntuleFormu(kullaniciAdi, config).setVisible(true);
            });
            anaPanel.add(gorevlerim);
            anaPanel.add(Box.createVerticalStrut(15));

            // Eğer admin ise ekstra butonlar
            if (rol != null && rol.equalsIgnoreCase("admin")) {
                System.out.println("Admin butonları ekleniyor!");
                JButton projeEkle = createStyledButton("Proje Ekle");
                projeEkle.addActionListener(e -> {
                    new ProjeEkleFormu(config, kullaniciAdi).setVisible(true);

                });
                anaPanel.add(projeEkle);
                anaPanel.add(Box.createVerticalStrut(15));

                JButton kullaniciYonet = createStyledButton("Kullanıcıları Yönet");
                kullaniciYonet.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        KullaniciYonetimPaneli pencere = new KullaniciYonetimPaneli();
                        pencere.setVisible(true);
                    }
                });
                anaPanel.add(kullaniciYonet);
                anaPanel.add(Box.createVerticalStrut(15));
            }

            // Alt kısımda boşluk bırak
            anaPanel.add(Box.createVerticalGlue());

            add(anaPanel);
            pack(); // Pencereyi içeriğe göre boyutlandır
            setLocationRelativeTo(null); // Ekranın ortasında göster
            setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Bir hata oluştu: " + e.getMessage());
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}

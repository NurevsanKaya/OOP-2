package com.nuro.arayuz;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class GorevAtamaTest {

    // görev atanmış mı diye kontrol eden yardımcı fonksiyon
    private boolean gorevVarMi(List<Map<String, String>> liste) {
        for (Map<String, String> g : liste) {
            String task = g.get("task");
            if (task == null || task.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Test
    void bosGorevOlmamali() {
        // Kullanıcıya boş görev atadık
        List<Map<String, String>> liste = new ArrayList<>();
        Map<String, String> g = new HashMap<>();
        g.put("username", "test_user");
        g.put("task", "");  // boş görev

        liste.add(g);

        // Test: Görev boş olmamalı
        assertFalse(gorevVarMi(liste), "Boş görev atanamaz");
    }

    @Test
    void gecerliGorevOlmali() {
        // Kullanıcıya geçerli görev atadık
        List<Map<String, String>> liste = new ArrayList<>();
        Map<String, String> g = new HashMap<>();
        g.put("username", "test_user");
        g.put("task", "Backend yaz");

        liste.add(g);

        // Test: Görev atanmış olmalı
        assertTrue(gorevVarMi(liste), "Geçerli görev atanmalı");
    }

    @Test
    void birdenFazlaGorevAtanabilir() {
        // İki farklı kişiye görev atanıyor
        List<Map<String, String>> liste = new ArrayList<>();

        Map<String, String> g1 = new HashMap<>();
        g1.put("username", "user1");
        g1.put("task", "Tasarım yap");

        Map<String, String> g2 = new HashMap<>();
        g2.put("username", "user2");
        g2.put("task", "Kodla");

        liste.add(g1);
        liste.add(g2);

        // Test: Liste 2 kişi içermeli
        assertEquals(2, liste.size(), "İki kullanıcıya görev atanmalı");
    }
}

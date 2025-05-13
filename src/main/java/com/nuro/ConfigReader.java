package com.nuro;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties props = new Properties();

    static {
        try {
            InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties");
            props.load(input);

        } catch (IOException e) {
            System.out.println("Ayar dosyası okunamadı.");
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}

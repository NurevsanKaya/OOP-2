package com.nuro;
import com.mongodb.client.*;
import org.bson.Document;
import com.nuro.util.SifrelemeUtil;
import com.nuro.util.MongoDBUtil;  // Burada artık bağlantıyı buradan alıyoruz
public class KullaniciEkle {
    public static void main(String[] args) {
        MongoDatabase database = MongoDBUtil.getDatabase();
        MongoCollection<Document> users = database.getCollection("users");

        String girilenSifre = "123456";
        String hashSifre = SifrelemeUtil.hashSifre(girilenSifre);

        Document user = new Document("username", "gulderen")
                .append("email", "gulderen@example.com")
                .append("password", hashSifre)
                .append("role", "ADMIN")
                .append("createdAt", new java.util.Date());

        users.insertOne(user);
        System.out.println("Kullanıcı başarıyla eklendi.");
    }
}

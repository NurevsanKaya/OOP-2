package com.nuro.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.nuro.ConfigReader;

public class MongoDBUtil {

    private static final String HOST = ConfigReader.get("mongo.host");
    private static final String PORT = ConfigReader.get("mongo.port");
    private static final String DATABASE_NAME = ConfigReader.get("mongo.database");

    private static final String CONNECTION_STRING = "mongodb://" + HOST + ":" + PORT;

    private static MongoClient mongoClient = null;

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_STRING);
        }
        return mongoClient.getDatabase(DATABASE_NAME);
    }
}

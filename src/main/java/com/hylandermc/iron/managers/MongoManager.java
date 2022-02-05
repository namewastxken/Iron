package com.hylandermc.iron.managers;

import com.hylandermc.iron.Iron;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MongoManager {

    private Iron plugin;
    private FileConfiguration config;
    @Getter private MongoClient mongoClient;
    @Getter private MongoDatabase database;
    @Getter private Map<Player, String> rankCache;
    @Getter private Map<Player, Integer> prestigeCache;

    public MongoManager(Iron instance) {
        this.plugin = instance;
        this.config = plugin.getConfig();
        this.rankCache = new ConcurrentHashMap<>();
        this.prestigeCache = new ConcurrentHashMap<>();
    }

    public void connect() {
        try {
            final MongoCredential credential = MongoCredential.createCredential(
                    config.getString("username"),
                    config.getString("database"),
                    config.getString("password").toCharArray()
            );
            mongoClient = new MongoClient(new ServerAddress(config.getString("host"), config.getInt("port")), Collections.singletonList(credential));
            database = mongoClient.getDatabase(config.getString("database"));
            Bukkit.getConsoleSender().sendMessage("§b§lIRON | §aConnected into the db");
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§c§lCRITICAL ERROR: §bIron cannot connect to the mongo database.");
            e.printStackTrace();
        }
    }
}

package dev.akatriggered.database;

import dev.akatriggered.G1axStats;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    
    private final G1axStats plugin;
    private Connection connection;
    private final ConcurrentHashMap<String, Integer> cache = new ConcurrentHashMap<>();

    public DatabaseManager(G1axStats plugin) {
        this.plugin = plugin;
        initDatabase();
        startCacheFlushTask();
    }

    private void initDatabase() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "stats.db");
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS player_kills (" +
                "killer_uuid TEXT NOT NULL, " +
                "victim_uuid TEXT NOT NULL, " +
                "kills INTEGER DEFAULT 0, " +
                "PRIMARY KEY (killer_uuid, victim_uuid))"
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    public CompletableFuture<Integer> getKills(String killerUuid, String victimUuid) {
        String key = killerUuid + ":" + victimUuid;
        Integer cached = cache.get(key);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT kills FROM player_kills WHERE killer_uuid = ? AND victim_uuid = ?")) {
                stmt.setString(1, killerUuid);
                stmt.setString(2, victimUuid);
                ResultSet rs = stmt.executeQuery();
                int kills = rs.next() ? rs.getInt("kills") : 0;
                cache.put(key, kills);
                return kills;
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to get kills: " + e.getMessage());
                return 0;
            }
        });
    }

    public void incrementKills(String killerUuid, String victimUuid) {
        String key = killerUuid + ":" + victimUuid;
        cache.merge(key, 1, Integer::sum);
    }

    private void startCacheFlushTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                flushCache();
            }
        }.runTaskTimerAsynchronously(plugin, 1200L, 1200L); // Every minute
    }

    private void flushCache() {
        if (cache.isEmpty()) return;
        
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT OR REPLACE INTO player_kills (killer_uuid, victim_uuid, kills) VALUES (?, ?, ?)")) {
            
            for (var entry : cache.entrySet()) {
                String[] parts = entry.getKey().split(":");
                stmt.setString(1, parts[0]);
                stmt.setString(2, parts[1]);
                stmt.setInt(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to flush cache: " + e.getMessage());
        }
    }

    public void close() {
        flushCache();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to close database: " + e.getMessage());
        }
    }
}

package com.turtle.gmachivement.storage;

import com.turtle.gmachivement.api.AchievementStatus;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class AchievementStorage {

    private final JavaPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private Connection connection;

    public AchievementStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data"); // Keep folder logic just in case it's used elsewhere
    }

    public void initialize() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        try {
            Class.forName("org.sqlite.JDBC");
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS player_achievements (" +
                        "uuid VARCHAR(36) NOT NULL," +
                        "achievement_id VARCHAR(64) NOT NULL," +
                        "progress INT NOT NULL," +
                        "status VARCHAR(20) NOT NULL," +
                        "PRIMARY KEY (uuid, achievement_id)" +
                        ")");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite database", e);
        }
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (cache.containsKey(uuid)) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData data = new PlayerData(player.getName(), uuid);
            if (connection != null) {
                String query = "SELECT achievement_id, progress, status FROM player_achievements WHERE uuid = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, uuid.toString());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            String id = rs.getString("achievement_id");
                            int progress = rs.getInt("progress");
                            String statusStr = rs.getString("status");
                            AchievementStatus status = AchievementStatus.IN_PROGRESS;
                            try {
                                status = AchievementStatus.valueOf(statusStr);
                            } catch (IllegalArgumentException ignored) {}
                            data.setProgress(id, progress);
                            data.setStatus(id, status);
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to load player data from SQLite: " + player.getName(), e);
                }
            }
            cache.put(uuid, data);
        });
    }

    public void savePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = cache.get(uuid);
        if (data == null || connection == null) return;
        
        Map<String, Integer> progressSnapshot = new HashMap<>(data.getAllProgress());
        Map<String, AchievementStatus> statusSnapshot = new HashMap<>(data.getAllStatus());
        String pName = player.getName();
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            saveDataInternal(uuid.toString(), pName, progressSnapshot, statusSnapshot);
        });
    }

    private synchronized void saveDataInternal(String uuid, String playerName, Map<String, Integer> progressSnapshot, Map<String, AchievementStatus> statusSnapshot) {
        if (connection == null || progressSnapshot.isEmpty()) return;
        String query = "INSERT OR REPLACE INTO player_achievements (uuid, achievement_id, progress, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Integer> entry : progressSnapshot.entrySet()) {
                pstmt.setString(1, uuid);
                pstmt.setString(2, entry.getKey());
                pstmt.setInt(3, entry.getValue());
                AchievementStatus status = statusSnapshot.getOrDefault(entry.getKey(), AchievementStatus.IN_PROGRESS);
                pstmt.setString(4, status.name());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data to SQLite: " + playerName, e);
        }
    }

    public void saveAll() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            savePlayer(p);
        }
    }

    public void unloadPlayer(Player player) {
        savePlayer(player);
        cache.remove(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    public void saveAllSync() {
        for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
            saveDataInternal(entry.getKey().toString(), entry.getValue().name, new HashMap<>(entry.getValue().getAllProgress()), new HashMap<>(entry.getValue().getAllStatus()));
        }
    }

    public void close() {
        saveAllSync();
        cache.clear();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close SQLite connection", e);
            }
        }
    }

    public static class PlayerData {
        private final String name;
        private final UUID uuid;
        private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();
        private final Map<String, AchievementStatus> statusMap = new ConcurrentHashMap<>();

        public PlayerData(String name, UUID uuid) {
            this.name = name;
            this.uuid = uuid;
        }

        public int getProgress(String achievementId) {
            return progressMap.getOrDefault(achievementId, 0);
        }

        public void setProgress(String achievementId, int progress) {
            progressMap.put(achievementId, progress);
        }

        public AchievementStatus getStatus(String achievementId) {
            return statusMap.getOrDefault(achievementId, AchievementStatus.IN_PROGRESS);
        }

        public void setStatus(String achievementId, AchievementStatus status) {
            statusMap.put(achievementId, status);
        }

        public Map<String, Integer> getAllProgress() {
            return progressMap;
        }

        public Map<String, AchievementStatus> getAllStatus() {
            return statusMap;
        }
    }
}

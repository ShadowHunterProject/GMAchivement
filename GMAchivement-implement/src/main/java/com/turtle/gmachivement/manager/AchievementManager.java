package com.turtle.gmachivement.manager;

import com.turtle.gmachivement.api.AchievementStatus;
import com.turtle.gmachivement.api.AchievementType;
import com.turtle.gmachivement.storage.AchievementStorage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class AchievementManager {

    private final JavaPlugin plugin;
    private final AchievementStorage storage;
    private final com.turtle.gmachivement.config.MessageManager messageManager;

    private final Map<String, AchievementDefinition> achievements = new LinkedHashMap<>();
    private final Map<String, CategoryDefinition> categories = new LinkedHashMap<>();

    public AchievementManager(JavaPlugin plugin, AchievementStorage storage, com.turtle.gmachivement.config.MessageManager messageManager) {
        this.plugin = plugin;
        this.storage = storage;
        this.messageManager = messageManager;
    }

    public JavaPlugin getPlugin() { return plugin; }

    public void load() {
        achievements.clear();
        categories.clear();

        loadCategories();
        loadAchievements();
        
        plugin.getLogger().info("Loaded " + categories.size() + " categories and " + achievements.size() + " achievements.");
    }

    private void loadCategories() {
        File file = new File(plugin.getDataFolder(), "categories.yml");
        if (!file.exists()) {
            plugin.saveResource("categories.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = config.getConfigurationSection("categories");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            String displayName = sec.getString(key + ".display-name", key);
            Material icon = Material.valueOf(sec.getString(key + ".icon", "BOOK"));
            int slot = sec.getInt(key + ".slot", 0);
            List<String> lore = sec.getStringList(key + ".lore");
            
            categories.put(key, new CategoryDefinition(key, displayName, icon, slot, lore));
        }
    }

    private void loadAchievements() {
        File folder = new File(plugin.getDataFolder(), "achievements");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String[] defaultFiles = {"player_kill.yml", "play_time.yml", "crate_open.yml", "points_earn.yml", "points_spend.yml", "player_join.yml", "chat_message.yml", "command_execute.yml", "boss_kill.yml", "gate_e.yml", "gate_d.yml", "gate_c.yml", "gate_b.yml", "gate_a.yml", "solo_level.yml", "solo_rank.yml"};
        for (String file : defaultFiles) {
            File targetFile = new File(folder, file);
            if (!targetFile.exists()) {
                try {
                    plugin.saveResource("achievements/" + file, false);
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not save default achievement file: " + file);
                }
            }
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) return;

        for (File file : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection sec = config.getConfigurationSection("achievements");
            if (sec == null) continue;

            for (String key : sec.getKeys(false)) {
                String category = sec.getString(key + ".category", "default");
                String displayName = sec.getString(key + ".display-name", key);
                List<String> lore = sec.getStringList(key + ".lore");
                
                AchievementType type = AchievementType.CUSTOM;
                try {
                    type = AchievementType.valueOf(sec.getString(key + ".type", "CUSTOM"));
                } catch (Exception ignored) {}
                
                String filter = sec.getString(key + ".target-filter", "");
                long target = sec.getLong(key + ".target", 1);
                List<String> rewards = sec.getStringList(key + ".rewards");

                achievements.put(key, new AchievementDefinition(key, category, displayName, lore, type, filter, target, rewards));
            }
        }
    }

    public Collection<CategoryDefinition> getCategories() {
        return categories.values();
    }

    public List<AchievementDefinition> getAchievementsByCategory(String categoryId) {
        List<AchievementDefinition> list = new ArrayList<>();
        for (AchievementDefinition def : achievements.values()) {
            if (def.getCategoryId().equalsIgnoreCase(categoryId)) {
                list.add(def);
            }
        }
        return list;
    }

    public AchievementDefinition getAchievement(String id) {
        return achievements.get(id);
    }

    public List<AchievementDefinition> getActiveAchievementsByType(AchievementType type) {
        List<AchievementDefinition> list = new ArrayList<>();
        for (AchievementDefinition def : achievements.values()) {
            if (def.getType() == type) {
                list.add(def);
            }
        }
        return list;
    }

    public AchievementStorage.PlayerData getPlayerData(UUID uuid) {
        return storage.getPlayerData(uuid);
    }

    public void loadPlayer(Player player) {
        storage.loadPlayer(player);
    }

    public void unloadPlayer(Player player) {
        storage.unloadPlayer(player);
    }

    public void addProgress(Player player, AchievementType type, String targetContext, int amount) {
        AchievementStorage.PlayerData data = storage.getPlayerData(player.getUniqueId());
        if (data == null) return;

        List<AchievementDefinition> active = getActiveAchievementsByType(type);
        boolean updated = false;

        for (AchievementDefinition def : active) {
            if (data.getStatus(def.getId()) != AchievementStatus.IN_PROGRESS) {
                continue; // Already claimable or completed
            }

            // Check filter
            if (def.getParsedTargetFilter() != null && !def.getParsedTargetFilter().isEmpty()) {
                if (targetContext == null) {
                    continue; // Filter doesn't match
                }
                if (!def.getParsedTargetFilter().contains(targetContext.toLowerCase())) {
                    continue; // Filter doesn't match
                }
            }

            int current = data.getProgress(def.getId());
            current += amount;
            if (current >= def.getTarget()) {
                current = (int) def.getTarget();
                data.setStatus(def.getId(), AchievementStatus.CLAIMABLE);
                
                if (plugin.getConfig().getBoolean("settings.message-on-complete", true)) {
                    String completeMsg = messageManager.getMessage("notifications.achievement_complete")
                            .replace("%achievement%", messageManager.colorize(def.getDisplayName()));
                    player.sendMessage(completeMsg);
                }
                
                playSound(player, "sounds.on-complete", "ENTITY_PLAYER_LEVELUP");
                
                if (plugin.getConfig().getBoolean("settings.broadcast-on-complete", false)) {
                    String broadcastMsg = messageManager.getMessage("notifications.achievement_broadcast")
                            .replace("%player%", player.getName())
                            .replace("%achievement%", messageManager.colorize(def.getDisplayName()));
                    plugin.getServer().broadcastMessage(broadcastMsg);
                }
            }
            data.setProgress(def.getId(), current);
            updated = true;
        }

        if (updated) {
            // auto-save task handles periodic saving, saving instantly on progress is too heavy
        }
    }

    public void setProgress(Player player, AchievementType type, String targetContext, int newProgress) {
        AchievementStorage.PlayerData data = storage.getPlayerData(player.getUniqueId());
        if (data == null) return;

        List<AchievementDefinition> active = getActiveAchievementsByType(type);
        boolean updated = false;

        for (AchievementDefinition def : active) {
            if (data.getStatus(def.getId()) != AchievementStatus.IN_PROGRESS) {
                continue;
            }

            if (def.getParsedTargetFilter() != null && !def.getParsedTargetFilter().isEmpty()) {
                if (targetContext == null) continue;
                if (!def.getParsedTargetFilter().contains(targetContext.toLowerCase())) {
                    continue;
                }
            }

            int current = data.getProgress(def.getId());
            if (newProgress <= current) continue; // Only increase progress
            
            int setVal = newProgress;
            if (setVal >= def.getTarget()) {
                setVal = (int) def.getTarget();
                data.setStatus(def.getId(), AchievementStatus.CLAIMABLE);
                
                if (plugin.getConfig().getBoolean("settings.message-on-complete", true)) {
                    String completeMsg = messageManager.getMessage("notifications.achievement_complete")
                            .replace("%achievement%", messageManager.colorize(def.getDisplayName()));
                    player.sendMessage(completeMsg);
                }
                
                playSound(player, "sounds.on-complete", "ENTITY_PLAYER_LEVELUP");
                
                if (plugin.getConfig().getBoolean("settings.broadcast-on-complete", false)) {
                    String broadcastMsg = messageManager.getMessage("notifications.achievement_broadcast")
                            .replace("%player%", player.getName())
                            .replace("%achievement%", messageManager.colorize(def.getDisplayName()));
                    plugin.getServer().broadcastMessage(broadcastMsg);
                }
            }
            data.setProgress(def.getId(), setVal);
            updated = true;
        }
    }

    private void playSound(Player player, String configPath, String defSound) {
        if (!plugin.getConfig().getBoolean("sounds.enable", true)) return;
        String soundName = plugin.getConfig().getString(configPath, defSound);
        if (soundName == null || soundName.isEmpty()) return;
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }

    public boolean claimReward(Player player, String achievementId) {
        AchievementStorage.PlayerData data = storage.getPlayerData(player.getUniqueId());
        if (data == null) return false;

        if (data.getStatus(achievementId) == AchievementStatus.CLAIMABLE) {
            AchievementDefinition def = getAchievement(achievementId);
            if (def != null) {
                data.setStatus(achievementId, AchievementStatus.COMPLETED);
                
                // Execute rewards
                for (String cmd : def.getRewards()) {
                    String parsedCmd = cmd.replace("%player%", player.getName());
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsedCmd);
                }
                
                playSound(player, "sounds.on-claim", "ENTITY_EXPERIENCE_ORB_PICKUP");
                
                storage.savePlayer(player); // Save immediately on claim
                return true;
            }
        }
        return false;
    }

    public void close() {
        // any cleanup
    }
}

package com.turtle.gmachivement.core;

import com.turtle.gmachivement.command.GMAchivementCommand;
import com.turtle.gmachivement.config.MessageManager;
import com.turtle.gmachivement.gui.GMAchivementMenu;
import com.turtle.gmachivement.listener.ExternalAchievementListener;
import com.turtle.gmachivement.listener.AchievementListener;
import com.turtle.gmachivement.manager.AchievementManager;
import com.turtle.gmachivement.storage.AchievementStorage;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class GMAchivementCore {

    private final JavaPlugin plugin;
    private MessageManager messageManager;
    private AchievementStorage storage;
    private AchievementManager achievementManager;
    private GMAchivementMenu achievementMenu;
    private AchievementListener achievementListener;
    private ExternalAchievementListener externalAchievementListener;

    public JavaPlugin getPlugin() { return plugin; }
    public MessageManager getMessageManager() { return messageManager; }
    public AchievementStorage getStorage() { return storage; }
    public AchievementManager getAchievementManager() { return achievementManager; }
    public GMAchivementMenu getAchievementMenu() { return achievementMenu; }
    public AchievementListener getAchievementListener() { return achievementListener; }
    public ExternalAchievementListener getExternalAchievementListener() { return externalAchievementListener; }

    public GMAchivementCore(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        plugin.saveDefaultConfig();

        this.messageManager = new MessageManager(plugin);
        this.messageManager.load();

        this.storage = new AchievementStorage(plugin);
        this.storage.initialize();

        this.achievementManager = new AchievementManager(plugin, storage, messageManager);
        this.achievementManager.load();

        this.achievementMenu = new GMAchivementMenu(achievementManager, messageManager);
        plugin.getServer().getPluginManager().registerEvents(achievementMenu, plugin);

        this.achievementListener = new AchievementListener(plugin, achievementManager);
        plugin.getServer().getPluginManager().registerEvents(achievementListener, plugin);

        this.externalAchievementListener = new ExternalAchievementListener(plugin, achievementManager);
        this.externalAchievementListener.register();

        PluginCommand command = plugin.getCommand("thanhtuu");
        if (command != null) {
            GMAchivementCommand handler = new GMAchivementCommand(achievementManager, achievementMenu, messageManager);
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }

        if (plugin.getServer().getPluginManager().getPlugin("SoloLevelingCore") != null) {
            plugin.getServer().getPluginManager().registerEvents(new com.turtle.gmachivement.listener.SoloLevelingListener(achievementManager), plugin);
            plugin.getLogger().info("Hooked into SoloLevelingCore directly!");
        }

        // Start Play Time Tracker (runs every 1 minute = 1200 ticks)
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                achievementManager.addProgress(p, com.turtle.gmachivement.api.AchievementType.PLAY_TIME, "", 1);
            }
        }, 1200L, 1200L);

        // Auto Save Task
        long autoSaveMinutes = plugin.getConfig().getLong("settings.auto-save-interval", 10);
        if (autoSaveMinutes > 0) {
            long ticks = autoSaveMinutes * 60 * 20;
            plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                storage.saveAll();
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("Auto-saved player achievement data.");
                }
            }, ticks, ticks);
        }

        // Pre-load any players already online (reload scenario).
        plugin.getServer().getOnlinePlayers().forEach(p -> storage.loadPlayer(p));
    }

    public void onDisable() {
        if (achievementManager != null) {
            achievementManager.close();
        }
        if (storage != null) {
            storage.close();
        }
    }
}

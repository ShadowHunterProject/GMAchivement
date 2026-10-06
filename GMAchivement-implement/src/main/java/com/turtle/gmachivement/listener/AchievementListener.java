package com.turtle.gmachivement.listener;

import com.turtle.gmachivement.api.AchievementType;
import com.turtle.gmachivement.manager.AchievementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AchievementListener implements Listener {

    private final JavaPlugin plugin;
    private final AchievementManager manager;

    public AchievementListener(JavaPlugin plugin, AchievementManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        manager.addProgress(event.getPlayer(), AchievementType.BLOCK_BREAK, event.getBlock().getType().name(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        manager.addProgress(event.getPlayer(), AchievementType.BLOCK_PLACE, event.getBlock().getType().name(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            manager.addProgress(killer, AchievementType.ENTITY_KILL, event.getEntity().getType().name(), 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            manager.addProgress(killer, AchievementType.PLAYER_KILL, "PLAYER", 1);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinData(PlayerJoinEvent event) {
        manager.loadPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        manager.addProgress(event.getPlayer(), AchievementType.PLAYER_JOIN, "", 1);
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        manager.unloadPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        manager.addProgress(event.getPlayer(), AchievementType.CHAT_MESSAGE, "", 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandExecute(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage().split(" ")[0].toLowerCase();
        manager.addProgress(event.getPlayer(), AchievementType.COMMAND_EXECUTE, cmd, 1);
    }
}

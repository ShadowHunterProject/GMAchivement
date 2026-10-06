package com.turtle.gmachivement.economy;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlayerPointsHook {

    private final JavaPlugin plugin;
    private PlayerPointsAPI api;

    public PlayerPointsHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            plugin.getLogger().info("PlayerPoints not found; pass purchases are disabled.");
            return false;
        }
        PlayerPoints playerPoints = PlayerPoints.getInstance();
        if (playerPoints == null) {
            plugin.getLogger().warning("PlayerPoints plugin is enabled but API is unavailable.");
            return false;
        }
        this.api = playerPoints.getAPI();
        if (api != null) {
            plugin.getLogger().info("PlayerPoints hooked.");
        }
        return api != null;
    }

    public boolean isEnabled() {
        return api != null;
    }

    public int getBalance(UUID playerId) {
        return api != null ? api.look(playerId) : 0;
    }

    public boolean has(UUID playerId, int amount) {
        return amount <= 0 || getBalance(playerId) >= amount;
    }

    public boolean take(UUID playerId, int amount) {
        return amount <= 0 || (api != null && api.take(playerId, amount));
    }
}

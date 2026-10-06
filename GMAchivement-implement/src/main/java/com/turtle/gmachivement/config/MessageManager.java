package com.turtle.gmachivement.config;

import com.turtle.gmachivement.api.AchievementType;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {
    private final JavaPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        messages.clear();
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadSection(config, "", config);
    }

    private void loadSection(FileConfiguration config, String pathPrefix, org.bukkit.configuration.ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            String fullPath = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
            if (section.isConfigurationSection(key)) {
                loadSection(config, fullPath, section.getConfigurationSection(key));
            } else {
                messages.put(fullPath, section.getString(key));
            }
        }
    }

    public String getMessage(String path) {
        String msg = messages.getOrDefault(path, path);
        String prefix = messages.getOrDefault("prefix", "");
        return colorize(prefix + msg);
    }

    public String getRawMessage(String path) {
        return messages.getOrDefault(path, path);
    }

    public String getTypeName(AchievementType type) {
        return colorize(messages.getOrDefault("achievement_types." + type.name(), type.name()));
    }

    public String colorize(String message) {
        if (message == null) return "";
        // Hex support for 1.16+
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }
            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

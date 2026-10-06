package com.turtle.gmachivement.command;

import com.turtle.gmachivement.config.MessageManager;
import com.turtle.gmachivement.gui.GMAchivementMenu;
import com.turtle.gmachivement.manager.AchievementManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class GMAchivementCommand implements CommandExecutor, TabCompleter {

    private final AchievementManager manager;
    private final GMAchivementMenu menu;
    private final MessageManager messageManager;

    public GMAchivementCommand(AchievementManager manager, GMAchivementMenu menu, MessageManager messageManager) {
        this.manager = manager;
        this.menu = menu;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messageManager.getMessage("commands.only_player"));
                return true;
            }
            menu.openCategories(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("gmachivement.admin")) {
                sender.sendMessage(messageManager.getMessage("commands.no_permission"));
                return true;
            }
            manager.getPlugin().reloadConfig();
            manager.load();
            messageManager.load();
            sender.sendMessage(messageManager.getMessage("commands.reload_success"));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("gmachivement.admin")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                return Collections.singletonList("reload");
            }
        }
        return Collections.emptyList();
    }
}

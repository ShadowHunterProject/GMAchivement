package com.turtle.gmachivement.gui;

import com.turtle.gmachivement.api.AchievementStatus;
import com.turtle.gmachivement.config.MessageManager;
import com.turtle.gmachivement.manager.AchievementDefinition;
import com.turtle.gmachivement.manager.AchievementManager;
import com.turtle.gmachivement.manager.CategoryDefinition;
import com.turtle.gmachivement.storage.AchievementStorage;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GMAchivementMenu implements Listener {

    private final AchievementManager manager;
    private final MessageManager messageManager;

    public GMAchivementMenu(AchievementManager manager, MessageManager messageManager) {
        this.manager = manager;
        this.messageManager = messageManager;
    }

    private String getGuiTitle() {
        return manager.getPlugin().getConfig().getString("gui.main-menu-title", "&8❖ &3DANH MỤC THÀNH TỰU &8❖");
    }

    private Material getGuiMaterial(String path, Material def) {
        try {
            return Material.valueOf(manager.getPlugin().getConfig().getString(path, def.name()));
        } catch (Exception e) {
            return def;
        }
    }

    public void openCategories(Player player) {
        MenuHolder holder = new MenuHolder("MAIN");
        Inventory inv = Bukkit.createInventory(holder, 54, messageManager.colorize(getGuiTitle()));
        
        Material filler = getGuiMaterial("gui.filler-item", Material.GRAY_STAINED_GLASS_PANE);
        Material border = getGuiMaterial("gui.border-item", Material.CYAN_STAINED_GLASS_PANE);
        
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, MenuItems.filler(border));
            } else {
                inv.setItem(i, MenuItems.filler(filler));
            }
        }

        // Info item
        String infoName = manager.getPlugin().getConfig().getString("gui.info-item.name", "&a&lHỆ THỐNG THÀNH TỰU");
        int infoSlot = manager.getPlugin().getConfig().getInt("gui.info-item.slot", 4);
        List<String> rawInfoLore = manager.getPlugin().getConfig().getStringList("gui.info-item.lore");
        List<String> infoLore = new ArrayList<>();
        if (rawInfoLore.isEmpty()) {
            infoLore.add(messageManager.colorize("&7Hoàn thành các nhiệm vụ để"));
            infoLore.add(messageManager.colorize("&7nhận phần thưởng hấp dẫn!"));
        } else {
            for (String line : rawInfoLore) {
                infoLore.add(messageManager.colorize(line));
            }
        }
        inv.setItem(infoSlot, MenuItems.item(Material.BOOK, messageManager.colorize(infoName), infoLore));

        // Close button
        String closeName = manager.getPlugin().getConfig().getString("gui.close-item.name", "&c&lĐóng Menu");
        int closeSlot = manager.getPlugin().getConfig().getInt("gui.close-item.slot", 49);
        inv.setItem(closeSlot, MenuItems.item(Material.BARRIER, messageManager.colorize(closeName), List.of()));

        for (CategoryDefinition cat : manager.getCategories()) {
            List<String> lore = new ArrayList<>();
            for (String line : cat.getLore()) {
                lore.add(messageManager.colorize(line));
            }
            lore.add("");
            lore.add(messageManager.colorize("&eNhấn để xem các thành tựu!"));
            ItemStack item = MenuItems.item(cat.getIcon(), messageManager.colorize(cat.getDisplayName()), lore);
            inv.setItem(cat.getSlot(), item);
        }

        player.openInventory(inv);
    }

    public void openCategory(Player player, String categoryId) {
        CategoryDefinition cat = null;
        for (CategoryDefinition c : manager.getCategories()) {
            if (c.getId().equals(categoryId)) {
                cat = c;
                break;
            }
        }
        if (cat == null) return;

        MenuHolder holder = new MenuHolder(categoryId);
        Inventory inv = Bukkit.createInventory(holder, 54, messageManager.colorize(cat.getDisplayName()));
        Material filler = getGuiMaterial("gui.filler-item", Material.GRAY_STAINED_GLASS_PANE);
        Material border = getGuiMaterial("gui.border-item", Material.CYAN_STAINED_GLASS_PANE);
        
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, MenuItems.filler(border));
            } else {
                inv.setItem(i, MenuItems.filler(filler));
            }
        }

        List<AchievementDefinition> achievements = manager.getAchievementsByCategory(categoryId);
        List<Integer> cfgSlots = manager.getPlugin().getConfig().getIntegerList("gui.achievement-slots");
        int[] slots;
        if (cfgSlots != null && !cfgSlots.isEmpty()) {
            slots = cfgSlots.stream().mapToInt(Integer::intValue).toArray();
        } else {
            slots = new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34};
        }
        
        AchievementStorage.PlayerData data = manager.getPlayerData(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§cĐang tải dữ liệu của bạn, vui lòng thử lại sau.");
            return;
        }
        
        Material matInProgress = getGuiMaterial("gui.status-items.in-progress", Material.PAPER);
        Material matClaimable = getGuiMaterial("gui.status-items.claimable", Material.CHEST);
        Material matCompleted = getGuiMaterial("gui.status-items.completed", Material.CHEST_MINECART);

        int slotIndex = 0;
        for (AchievementDefinition def : achievements) {
            if (slotIndex >= slots.length) break;
            
            int progress = data.getProgress(def.getId());
            AchievementStatus status = data.getStatus(def.getId());
            
            Material mat;
            String statusText;
            
            if (status == AchievementStatus.COMPLETED) {
                mat = matCompleted;
                statusText = "#00CC44✔ Đã Hoàn Thành";
            } else if (status == AchievementStatus.CLAIMABLE) {
                mat = matClaimable;
                statusText = "#FFD700🎁 Có Thể Nhận!";
            } else {
                mat = matInProgress;
                statusText = "#AAAAAA⏳ Đang thực hiện";
            }

            List<String> lore = new ArrayList<>();
            String typeName = messageManager.getTypeName(def.getType());
            lore.add(messageManager.colorize("#AAAAAA▶ #FFFFFFLoại nhiệm vụ: #FFD700") + typeName);
            lore.add("");
            
            if (def.getLore() != null) {
                for (String line : def.getLore()) {
                    lore.add(messageManager.colorize(line));
                }
            }
            
            lore.add("");
            String progressIcon = (progress >= def.getTarget()) ? "#00CC44✔" : "#FF4C4C❌";
            lore.add(messageManager.colorize("#AAAAAA▶ #FFFFFFTiến độ: #00E5FF" + progress + " #FFFFFF/ #00E5FF" + def.getTarget() + " " + progressIcon));
            lore.add(messageManager.colorize(statusText));

            int slot = slots[slotIndex++];
            inv.setItem(slot, MenuItems.item(mat, messageManager.colorize(def.getDisplayName()), lore));
            holder.getSlotMap().put(slot, def.getId());
        }

        // Back button
        String backName = manager.getPlugin().getConfig().getString("gui.back-item.name", "&eQuay lại");
        int backSlot = manager.getPlugin().getConfig().getInt("gui.back-item.slot", 45);
        inv.setItem(backSlot, MenuItems.item(Material.ARROW, messageManager.colorize(backName), List.of()));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        if (event.getInventory().getHolder() instanceof MenuHolder holder) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            String state = holder.getState();
            
            if (state.equals("MAIN")) {
                int closeSlot = manager.getPlugin().getConfig().getInt("gui.close-item.slot", 49);
                if (event.getSlot() == closeSlot) {
                    player.closeInventory();
                    return;
                }
                for (CategoryDefinition cat : manager.getCategories()) {
                    if (event.getSlot() == cat.getSlot()) {
                        openCategory(player, cat.getId());
                        return;
                    }
                }
            } else {
                int backSlot = manager.getPlugin().getConfig().getInt("gui.back-item.slot", 45);
                if (event.getSlot() == backSlot) {
                    openCategories(player);
                    return;
                }
                
                String achievementId = holder.getSlotMap().get(event.getSlot());
                if (achievementId != null) {
                    AchievementStorage.PlayerData data = manager.getPlayerData(player.getUniqueId());
                    if (data != null && data.getStatus(achievementId) == AchievementStatus.CLAIMABLE) {
                        boolean success = manager.claimReward(player, achievementId);
                        if (success) {
                            player.sendMessage(messageManager.getMessage("menu.reward_claimed"));
                            openCategory(player, state); // refresh
                        }
                    } else if (data != null && data.getStatus(achievementId) == AchievementStatus.COMPLETED) {
                        player.sendMessage(messageManager.getMessage("menu.reward_already_claimed"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }
}

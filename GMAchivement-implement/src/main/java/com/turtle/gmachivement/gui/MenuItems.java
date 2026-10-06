package com.turtle.gmachivement.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/** Helpers for crafting menu icons consistently. */
public final class MenuItems {

    private MenuItems() {}

    public static ItemStack item(Material material, String displayName, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack filler(Material material) {
        return item(material, ChatColor.RESET.toString(), List.of());
    }

    public static Material parseMaterial(String raw, Material fallback) {
        if (raw == null) return fallback;
        try {
            return Material.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}

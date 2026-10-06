package com.turtle.gmachivement.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class MenuHolder implements InventoryHolder {
    
    private final String state;
    private final Map<Integer, String> slotMap = new HashMap<>();

    public MenuHolder(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
    
    public Map<Integer, String> getSlotMap() {
        return slotMap;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}

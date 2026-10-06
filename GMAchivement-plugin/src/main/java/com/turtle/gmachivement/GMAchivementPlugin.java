package com.turtle.gmachivement;

import com.turtle.gmachivement.core.GMAchivementCore;

import org.bukkit.plugin.java.JavaPlugin;

public class GMAchivementPlugin extends JavaPlugin {

    private GMAchivementCore core;

    @Override
    public void onEnable() {
        core = new GMAchivementCore(this);
        core.onEnable();
    }

    @Override
    public void onDisable() {
        if (core != null) {
            core.onDisable();
        }
    }
}

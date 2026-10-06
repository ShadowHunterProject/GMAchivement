package com.turtle.gmachivement.listener;

import com.turtle.gmachivement.api.AchievementType;
import com.turtle.gmachivement.manager.AchievementManager;
import com.turtle.sololevelingcore.api.event.SoloLevelUpEvent;
import com.turtle.sololevelingcore.api.event.SoloRankUpEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SoloLevelingListener implements Listener {

    private final AchievementManager achievementManager;

    public SoloLevelingListener(AchievementManager achievementManager) {
        this.achievementManager = achievementManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSoloLevelUp(SoloLevelUpEvent event) {
        achievementManager.setProgress(event.getPlayer(), AchievementType.SOLO_LEVEL_REACH, "", event.getNewMaxLevel());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSoloRankUp(SoloRankUpEvent event) {
        // Set targetContext as rankId, progress as 1
        achievementManager.addProgress(event.getPlayer(), AchievementType.SOLO_RANK_REACH, event.getNewRankId(), 1);
    }
}

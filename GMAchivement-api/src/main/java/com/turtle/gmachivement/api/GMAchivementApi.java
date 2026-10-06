package com.turtle.gmachivement.api;

import java.util.UUID;

public interface GMAchivementApi {

    /**
     * Get the current progress of an achievement for a player.
     * @param playerId UUID of the player
     * @param achievementId The ID of the achievement
     * @return The current progress amount
     */
    int getProgress(UUID playerId, String achievementId);

    /**
     * Get the current status of an achievement for a player.
     * @param playerId UUID of the player
     * @param achievementId The ID of the achievement
     * @return AchievementStatus enum (IN_PROGRESS, CLAIMABLE, COMPLETED)
     */
    AchievementStatus getStatus(UUID playerId, String achievementId);

}

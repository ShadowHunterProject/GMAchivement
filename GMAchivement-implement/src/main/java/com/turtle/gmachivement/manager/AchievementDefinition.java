package com.turtle.gmachivement.manager;

import com.turtle.gmachivement.api.AchievementType;
import java.util.List;

public class AchievementDefinition {
    private final String id;
    private final String categoryId;
    private final String displayName;
    private final List<String> lore;
    private final AchievementType type;
    private final String targetFilter;
    private final List<String> parsedTargetFilter;
    private final long target;
    private final List<String> rewards;

    public AchievementDefinition(String id, String categoryId, String displayName, List<String> lore, AchievementType type, String targetFilter, long target, List<String> rewards) {
        this.id = id;
        this.categoryId = categoryId;
        this.displayName = displayName;
        this.lore = lore;
        this.type = type;
        this.targetFilter = targetFilter;
        
        java.util.List<String> parsed = new java.util.ArrayList<>();
        if (targetFilter != null && !targetFilter.isEmpty()) {
            for (String p : targetFilter.split(",")) {
                parsed.add(p.trim().toLowerCase());
            }
        }
        this.parsedTargetFilter = parsed;
        
        this.target = target;
        this.rewards = rewards;
    }

    public String getId() { return id; }
    public String getCategoryId() { return categoryId; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public AchievementType getType() { return type; }
    public String getTargetFilter() { return targetFilter; }
    public List<String> getParsedTargetFilter() { return parsedTargetFilter; }
    public long getTarget() { return target; }
    public List<String> getRewards() { return rewards; }
}

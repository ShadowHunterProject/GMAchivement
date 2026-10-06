package com.turtle.gmachivement.api;

public class AchievementProgress {
    private final String id;
    private int current;

    public AchievementProgress(String id, int current) {
        this.id = id;
        this.current = current;
    }

    public String getId() { return id; }
    public int getCurrent() { return current; }
    public void setCurrent(int current) { this.current = current; }
    public void add(int amount) { this.current += amount; }
}

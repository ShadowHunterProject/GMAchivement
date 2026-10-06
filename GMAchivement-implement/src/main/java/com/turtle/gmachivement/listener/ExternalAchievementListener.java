package com.turtle.gmachivement.listener;

import com.turtle.gmachivement.api.AchievementType;
import com.turtle.gmachivement.manager.AchievementManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ExternalAchievementListener implements Listener {

    private final JavaPlugin plugin;
    private final AchievementManager manager;

    private final Map<String, Method> methodCache = new HashMap<>();

    public ExternalAchievementListener(JavaPlugin plugin, AchievementManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void register() {
        registerEvent("ExcellentCrates",
            "su.nightexpress.excellentcrates.api.event.CrateOpenEvent",
            this::crateOpenEvent);
        registerEvent("MythicMobs",
            "io.lumine.mythic.bukkit.events.MythicMobDeathEvent",
            this::mythicMobDeathEvent);
        registerEvent("PlayerPoints",
            "org.black_ixx.playerpoints.event.PlayerPointsChangeEvent",
            this::playerPointsChangeEvent);
    }

    @SuppressWarnings("unchecked")
    private void registerEvent(String pluginName, String className, Function<Event, TrackedEvent> handler) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled(pluginName)) return;
        try {
            Class<? extends Event> clazz = (Class<? extends Event>) Class.forName(className);
            EventExecutor executor = (listener, event) -> {
                if (!clazz.isInstance(event)) return;
                TrackedEvent res = handler.apply(event);
                if (res != null) {
                    manager.addProgress(res.player, res.type, res.context, (int) res.amount);
                }
            };
            plugin.getServer().getPluginManager().registerEvent(
                clazz, this, EventPriority.MONITOR, executor, plugin, true
            );
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Could not hook into " + pluginName + " event " + className);
        }
    }

    private Object tryInvoke(Object obj, String method) {
        if (obj == null) return null;
        try {
            String key = obj.getClass().getName() + "#" + method;
            Method m = methodCache.computeIfAbsent(key, k -> {
                try {
                    Method found = obj.getClass().getMethod(method);
                    found.setAccessible(true);
                    return found;
                } catch (Exception e) { return null; }
            });
            if (m == null) return null;
            return m.invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private String stringValue(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    private Player asPlayer(Object obj) {
        if (obj instanceof Player) return (Player) obj;
        return null;
    }

    private long amountFrom(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        return 1L;
    }

    private TrackedEvent crateOpenEvent(Event event) {
        Player player = asPlayer(tryInvoke(event, "getPlayer"));
        Object crate = tryInvoke(event, "getCrate");
        String crateId = stringValue(tryInvoke(crate, "getId"));
        return new TrackedEvent(player, AchievementType.EXCELLENT_CRATE_OPEN, crateId, 1L);
    }

    private TrackedEvent playerPointsChangeEvent(Event event) {
        java.util.UUID uuid = (java.util.UUID) tryInvoke(event, "getPlayerId");
        if (uuid == null) return null;
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return null;
        
        long change = amountFrom(tryInvoke(event, "getChange"));
        if (change > 0) {
            return new TrackedEvent(player, AchievementType.PLAYER_POINTS_EARN, "", change);
        } else if (change < 0) {
            return new TrackedEvent(player, AchievementType.PLAYER_POINTS_SPEND, "", -change);
        }
        return null;
    }

    private TrackedEvent mythicMobDeathEvent(Event event) {
        Player player = asPlayer(tryInvoke(event, "getKiller"));
        if (player == null) return null;
        Object mob = tryInvoke(event, "getMobType");
        String internalName = stringValue(tryInvoke(mob, "getInternalName"));
        return new TrackedEvent(player, AchievementType.MYTHIC_MOB_KILL, internalName, 1L);
    }

    private record TrackedEvent(Player player, AchievementType type, String context, long amount) {}
}

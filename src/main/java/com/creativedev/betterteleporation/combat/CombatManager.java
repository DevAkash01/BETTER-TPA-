package com.creativedev.betterteleporation.combat;

import com.creativedev.betterteleporation.config.ConfigManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatManager {

    private final ConfigManager configManager;
    private final Map<UUID, Long> combatTimestamps = new ConcurrentHashMap<>();

    public CombatManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tagCombat(UUID uuid) {
        if (!configManager.isCombatEnabled()) {
            return;
        }
        combatTimestamps.put(uuid, System.currentTimeMillis());
    }

    public boolean isInCombat(Player player) {
        if (!configManager.isCombatEnabled() || player == null) {
            return false;
        }

        Long timestamp = combatTimestamps.get(player.getUniqueId());
        if (timestamp == null) {
            return false;
        }

        long elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000L;
        if (elapsedSeconds < configManager.getCombatDurationSeconds()) {
            return true;
        }

        combatTimestamps.remove(player.getUniqueId());
        return false;
    }

    public long getRemainingCombatSeconds(UUID uuid) {
        if (!configManager.isCombatEnabled()) {
            return 0L;
        }

        Long timestamp = combatTimestamps.get(uuid);
        if (timestamp == null) {
            return 0L;
        }

        long elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000L;
        long remaining = configManager.getCombatDurationSeconds() - elapsedSeconds;
        return Math.max(0L, remaining);
    }

    public void remove(UUID uuid) {
        combatTimestamps.remove(uuid);
    }

    public void clear() {
        combatTimestamps.clear();
    }
}

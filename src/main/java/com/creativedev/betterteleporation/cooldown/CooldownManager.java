package com.creativedev.betterteleporation.cooldown;

import com.creativedev.betterteleporation.util.Permissions;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public boolean isOnCooldown(Player player, String commandKey) {
        if (player.hasPermission(Permissions.BYPASS_COOLDOWN)) {
            return false;
        }

        Map<String, Long> userCooldowns = cooldowns.get(player.getUniqueId());
        if (userCooldowns == null) {
            return false;
        }

        Long expireAt = userCooldowns.get(commandKey.toLowerCase());
        if (expireAt == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expireAt) {
            userCooldowns.remove(commandKey.toLowerCase());
            return false;
        }

        return true;
    }

    public long getRemainingSeconds(UUID uuid, String commandKey) {
        Map<String, Long> userCooldowns = cooldowns.get(uuid);
        if (userCooldowns == null) {
            return 0L;
        }

        Long expireAt = userCooldowns.get(commandKey.toLowerCase());
        if (expireAt == null) {
            return 0L;
        }

        long remaining = (expireAt - System.currentTimeMillis()) / 1000L;
        return Math.max(0L, remaining);
    }

    public void setCooldown(UUID uuid, String commandKey, int seconds) {
        if (seconds <= 0) {
            return;
        }

        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(commandKey.toLowerCase(), System.currentTimeMillis() + (seconds * 1000L));
    }

    public void removeCooldowns(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public void clear() {
        cooldowns.clear();
    }
}

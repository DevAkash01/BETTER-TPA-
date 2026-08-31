package com.creativedev.betterteleporation.listener;

import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.teleport.TeleportCancelReason;
import com.creativedev.betterteleporation.teleport.TeleportManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class PlayerDeathListener implements Listener {

    private final ConfigManager configManager;
    private final TeleportManager teleportManager;

    public PlayerDeathListener(ConfigManager configManager, TeleportManager teleportManager) {
        this.configManager = configManager;
        this.teleportManager = teleportManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (configManager.isCancelOnDeath()) {
            teleportManager.cancelTask(event.getEntity().getUniqueId(), TeleportCancelReason.DEATH);
        }
    }
}

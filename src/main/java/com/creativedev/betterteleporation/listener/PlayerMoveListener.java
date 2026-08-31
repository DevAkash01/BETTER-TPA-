package com.creativedev.betterteleporation.listener;

import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.teleport.TeleportCancelReason;
import com.creativedev.betterteleporation.teleport.TeleportManager;
import com.creativedev.betterteleporation.teleport.TeleportTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class PlayerMoveListener implements Listener {

    private final ConfigManager configManager;
    private final TeleportManager teleportManager;

    public PlayerMoveListener(ConfigManager configManager, TeleportManager teleportManager) {
        this.configManager = configManager;
        this.teleportManager = teleportManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!configManager.isCancelOnMove()) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        Player player = event.getPlayer();
        TeleportTask task = teleportManager.getTask(player.getUniqueId());
        if (task == null) {
            return;
        }

        Location initial = task.getInitialLocation();
        if (initial.getWorld() == null || to.getWorld() == null) {
            return;
        }

        if (!initial.getWorld().equals(to.getWorld())) {
            teleportManager.cancelTask(player.getUniqueId(), TeleportCancelReason.MOVE);
            return;
        }

        double threshold = configManager.getMoveThreshold();
        if (threshold <= 0.0) {
            threshold = 0.5;
        }

        double dx = Math.abs(to.getX() - initial.getX());
        double dy = Math.abs(to.getY() - initial.getY());
        double dz = Math.abs(to.getZ() - initial.getZ());

        if (dx > threshold || dy > threshold || dz > threshold) {
            teleportManager.cancelTask(player.getUniqueId(), TeleportCancelReason.MOVE);
        }
    }
}

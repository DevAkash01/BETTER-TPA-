package com.creativedev.betterteleporation.teleport;

import com.creativedev.betterteleporation.BetterTeleporation;
import com.creativedev.betterteleporation.api.event.TeleportRequestTeleportEvent;
import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.request.TeleportRequest;
import com.creativedev.betterteleporation.request.TeleportRequestType;
import com.creativedev.betterteleporation.util.LocationUtil;
import com.creativedev.betterteleporation.util.MessageUtil;
import com.creativedev.betterteleporation.util.Permissions;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TeleportManager {

    private final BetterTeleporation plugin;
    private final ConfigManager configManager;
    private final Map<UUID, TeleportTask> activeTeleports = new ConcurrentHashMap<>();

    public TeleportManager(BetterTeleporation plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public boolean startTeleport(TeleportRequest request) {
        Player sender = Bukkit.getPlayer(request.getSenderUuid());
        Player target = Bukkit.getPlayer(request.getTargetUuid());

        if (sender == null || !sender.isOnline() || target == null || !target.isOnline()) {
            return false;
        }

        Player playerToTeleport = request.getType() == TeleportRequestType.TPA ? sender : target;
        Player destinationPlayer = request.getType() == TeleportRequestType.TPA ? target : sender;

        if (isTeleporting(playerToTeleport.getUniqueId())) {
            MessageUtil.sendMessage(playerToTeleport, "error.already-teleporting");
            return false;
        }

        if (configManager.isWorldDisabled(playerToTeleport.getWorld().getName()) ||
                configManager.isWorldDisabled(destinationPlayer.getWorld().getName())) {
            MessageUtil.sendMessage(playerToTeleport, "error.world-disabled");
            return false;
        }

        boolean bypass = playerToTeleport.isPermissionSet(Permissions.BYPASS_DELAY) && playerToTeleport.hasPermission(Permissions.BYPASS_DELAY);
        int delay = bypass ? 0 : configManager.getTeleportDelaySeconds();

        if (delay <= 0) {
            performDirectTeleport(request, playerToTeleport, destinationPlayer);
            return true;
        }

        MessageUtil.sendMessage(playerToTeleport, "teleport.warmup", Placeholder.parsed("seconds", String.valueOf(delay)));

        TeleportTask task = new TeleportTask(
                plugin,
                this,
                configManager,
                request,
                playerToTeleport,
                destinationPlayer.getUniqueId(),
                delay
        );

        activeTeleports.put(playerToTeleport.getUniqueId(), task);
        task.start();
        return true;
    }

    public void executeTeleport(TeleportTask task) {
        Player player = task.getPlayer();
        Player destPlayer = Bukkit.getPlayer(task.getDestinationPlayerUuid());

        if (player == null || !player.isOnline() || destPlayer == null || !destPlayer.isOnline()) {
            removeTask(task.getPlayer().getUniqueId());
            return;
        }

        Location dest = destPlayer.getLocation();

        if (!LocationUtil.isSafeLocation(dest)) {
            removeTask(player.getUniqueId());
            MessageUtil.sendMessage(player, "error.unsafe-destination");
            return;
        }

        TeleportRequestTeleportEvent event = new TeleportRequestTeleportEvent(task.getRequest(), player, dest);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            removeTask(player.getUniqueId());
            return;
        }

        MessageUtil.sendMessage(player, "teleport.teleporting");
        MessageUtil.sendActionBar(player, "teleport.teleporting-actionbar");

        player.teleportAsync(event.getDestination()).thenAccept(success -> {
            removeTask(player.getUniqueId());
            if (Boolean.TRUE.equals(success)) {
                MessageUtil.sendMessage(player, "teleport.success");
                MessageUtil.sendActionBar(player, "teleport.success-actionbar");
                configManager.getSoundManager().playTeleportSuccess(player);
                if (destPlayer.isOnline() && !destPlayer.getUniqueId().equals(player.getUniqueId())) {
                    configManager.getSoundManager().playTeleportSuccess(destPlayer);
                }
            } else {
                MessageUtil.sendMessage(player, "error.unsafe-destination");
            }
        });
    }

    private void performDirectTeleport(TeleportRequest request, Player player, Player destPlayer) {
        Location dest = destPlayer.getLocation();

        if (!LocationUtil.isSafeLocation(dest)) {
            MessageUtil.sendMessage(player, "error.unsafe-destination");
            return;
        }

        TeleportRequestTeleportEvent event = new TeleportRequestTeleportEvent(request, player, dest);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        MessageUtil.sendMessage(player, "teleport.teleporting");
        MessageUtil.sendActionBar(player, "teleport.teleporting-actionbar");

        player.teleportAsync(event.getDestination()).thenAccept(success -> {
            if (Boolean.TRUE.equals(success)) {
                MessageUtil.sendMessage(player, "teleport.success");
                MessageUtil.sendActionBar(player, "teleport.success-actionbar");
                configManager.getSoundManager().playTeleportSuccess(player);
                if (destPlayer.isOnline() && !destPlayer.getUniqueId().equals(player.getUniqueId())) {
                    configManager.getSoundManager().playTeleportSuccess(destPlayer);
                }
            } else {
                MessageUtil.sendMessage(player, "error.unsafe-destination");
            }
        });
    }

    public boolean isTeleporting(UUID uuid) {
        return activeTeleports.containsKey(uuid);
    }

    public TeleportTask getTask(UUID uuid) {
        return activeTeleports.get(uuid);
    }

    public void cancelTask(UUID uuid, TeleportCancelReason reason) {
        TeleportTask task = activeTeleports.remove(uuid);
        if (task != null) {
            task.cancel(reason);
        }
    }

    public void removeTask(UUID uuid) {
        activeTeleports.remove(uuid);
    }

    public void cancelAll() {
        for (TeleportTask task : activeTeleports.values()) {
            task.cancel(TeleportCancelReason.PLUGIN_DISABLE);
        }
        activeTeleports.clear();
    }
}

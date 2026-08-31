package com.creativedev.betterteleporation.teleport;

import com.creativedev.betterteleporation.BetterTeleporation;
import com.creativedev.betterteleporation.config.ConfigManager;
import com.creativedev.betterteleporation.request.TeleportRequest;
import com.creativedev.betterteleporation.util.MessageUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public final class TeleportTask implements Runnable {

    private final BetterTeleporation plugin;
    private final TeleportManager teleportManager;
    private final ConfigManager configManager;
    private final TeleportRequest request;
    private final Player player;
    private final UUID destinationPlayerUuid;
    private final Location initialLocation;
    private final int totalSeconds;
    private int secondsRemaining;
    private BukkitTask task;

    public TeleportTask(BetterTeleporation plugin, TeleportManager teleportManager, ConfigManager configManager,
                        TeleportRequest request, Player player, UUID destinationPlayerUuid, int seconds) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
        this.configManager = configManager;
        this.request = request;
        this.player = player;
        this.destinationPlayerUuid = destinationPlayerUuid;
        this.initialLocation = player.getLocation().clone();
        this.totalSeconds = Math.max(1, seconds);
        this.secondsRemaining = seconds;
    }

    public void start() {
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 20L);
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead()) {
            cancel(TeleportCancelReason.DEATH);
            return;
        }

        Player destPlayer = Bukkit.getPlayer(destinationPlayerUuid);
        if (destPlayer == null || !destPlayer.isOnline()) {
            cancel(TeleportCancelReason.DISCONNECT);
            return;
        }

        if (secondsRemaining <= 0) {
            stop();
            teleportManager.executeTeleport(this);
            return;
        }

        MessageUtil.sendActionBar(player, "teleport.countdown-actionbar", Placeholder.parsed("seconds", String.valueOf(secondsRemaining)));
        MessageUtil.sendActionBar(destPlayer, "teleport.target-actionbar",
                Placeholder.parsed("player", player.getName()),
                Placeholder.parsed("seconds", String.valueOf(secondsRemaining))
        );

        configManager.getSoundManager().playCountdownTick(player, secondsRemaining, totalSeconds);

        secondsRemaining--;
    }

    public void cancel(TeleportCancelReason reason) {
        stop();
        teleportManager.removeTask(player.getUniqueId());

        if (player.isOnline()) {
            switch (reason) {
                case MOVE -> {
                    MessageUtil.sendMessage(player, "teleport.cancelled-move");
                    MessageUtil.sendActionBar(player, "teleport.cancelled-move-actionbar");
                    configManager.getSoundManager().playTeleportCancel(player);
                }
                case DAMAGE -> {
                    MessageUtil.sendMessage(player, "teleport.cancelled-damage");
                    MessageUtil.sendActionBar(player, "teleport.cancelled-damage-actionbar");
                    configManager.getSoundManager().playTeleportCancel(player);
                }
                case DEATH -> MessageUtil.sendMessage(player, "teleport.cancelled-death");
                case DISCONNECT -> MessageUtil.sendMessage(player, "teleport.cancelled-disconnect");
                case PLUGIN_DISABLE -> {}
            }
        }
    }

    private void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getDestinationPlayerUuid() {
        return destinationPlayerUuid;
    }

    public Location getInitialLocation() {
        return initialLocation;
    }

    public TeleportRequest getRequest() {
        return request;
    }
}
